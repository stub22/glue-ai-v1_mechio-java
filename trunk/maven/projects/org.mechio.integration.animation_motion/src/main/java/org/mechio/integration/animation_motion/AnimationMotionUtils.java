/*
 * Copyright 2014 the MechIO Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mechio.integration.animation_motion;

import org.jflux.api.core.config.Configuration;
import org.jflux.impl.messaging.rk.config.ConnectionConfigUtils;
import org.jflux.impl.messaging.rk.config.RKMessagingConfigUtils;
import org.jflux.impl.messaging.rk.lifecycle.JMSAvroAsyncReceiverLifecycle;
import org.jflux.impl.messaging.rk.lifecycle.JMSAvroMessageSenderLifecycle;
import org.jflux.impl.messaging.rk.utils.ConnectionManager;
import org.jflux.impl.messaging.rk.utils.ConnectionUtils;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.mechio.api.animation.lifecycle.AnimationPlayerClientLifecycle;
import org.mechio.api.animation.messaging.RemoteAnimationPlayerClient;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.protocol.AnimationEvent;
import org.mechio.api.animation.protocol.AnimationEvent.AnimationEventFactory;
import org.mechio.api.animation.protocol.AnimationSignal;
import org.mechio.api.animation.utils.AnimationEditListener;
import org.mechio.api.animation.utils.PositionAdder;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.blending.FrameSource;
import org.mechio.api.motion.messaging.RemoteRobot;
import org.mechio.api.motion.utils.RobotUtils;
import org.mechio.impl.animation.messaging.AnimationRecord;
import org.mechio.impl.animation.messaging.AnimationSignallingRecord;
import org.mechio.impl.animation.messaging.PortableAnimationEvent;
import org.mechio.impl.animation.messaging.PortableAnimationSignal;
import org.mechio.integration.animation_motion.lifecycle.RobotChannelsLifecycle;
import org.mechio.integration.animation_motion.osgi.AnimationServices;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.JMSException;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationMotionUtils {
	private static final Logger theLogger = LoggerFactory.getLogger(AnimationMotionUtils.class);

	public final static long DEFAULT_BLENDER_INTERVAL = 40L;
	public final static int DEFAULT_MAX_RAMP_TIME_MILLISEC = 800;

	public static ServiceRegistration registerRobotAnimationPlayer(
			BundleContext context, Robot.Id robotId) {
		Boolean exists = OSGiUtils.serviceExists(
				context, AnimationPlayer.class.getName(),
				RobotUtils.getRobotFilter(robotId));
		if (exists == null || exists) {
			return null;
		}
		return registerAnimationPlayer(context, robotId);
	}

	public static AnimationServices registerAnimationEditingComponents(
			BundleContext context, Robot robot) {
//        String[] classes = new String[]{
//                ChannelsParameterSource.class.getName(), 
//                AnimationEditListener.class.getName(), 
//                PositionAdder.class.getName()};
//        Boolean exists = OSGiUtils.serviceExists(context, classes, null);
//        if(exists == null || exists){
//            return null;
//        }
		Robot.Id robotId = robot.getRobotId();
		AnimationServices services = new AnimationServices();
		//boolean player = OSGiUtils.serviceExists(
		//        context, AnimationPlayer.class, null);
		services.addServiceRegistration(
				registerChannelRobotParams(context, robotId));
		services.addServiceRegistration(
				registerAnimationEditListener(context, robot));
		services.addServiceRegistration(
				registerJointSnapshotAdder(context, robotId));
		if (!OSGiUtils.serviceExists(context, RemoteAnimationPlayerClient.class,
				OSGiUtils.createFilter(
						AnimationPlayer.PROP_PLAYER_ID,
						robot.getRobotId().getRobtIdString()))) {
			if (!(robot instanceof RemoteRobot)) {
				services.addServiceRegistration(
						registerAnimationPlayer(context, robotId));
			} else {
				AnimationServices moreServices =
						registerRemoteAnimPlayer(
								context, "robotReceiverConnectionConfig");
				services.addManagedServices(moreServices.getManagedServices());
				services.addServiceRegistrations(
						moreServices.getServiceRegistrations());
				services.addOSGiComponents(moreServices.getOSGiComponents());
			}
		}
		return services;
	}

	private static ServiceRegistration registerAnimationPlayer(
			BundleContext context, Robot.Id robotId) {
		AnimationPlayer player = new RampedAnimationPlayer(
				context, robotId, DEFAULT_MAX_RAMP_TIME_MILLISEC);
		Properties props = new Properties();
		String playerId = player.getAnimationPlayerId();
		props.put(Robot.PROP_ID, robotId.toString());
		ServiceRegistration reg = OSGiUtils.registerUniqueService(context,
				AnimationPlayer.class.getName(),
				AnimationPlayer.PROP_PLAYER_ID,
				playerId, player, props);
		theLogger.info("OSGiAnimationPlayer Service Registered Successfully.");
		return reg;
	}

	private static synchronized AnimationServices registerRemoteAnimPlayer(
			BundleContext context, String connectionConfigId) {
		ServiceReference[] confRefs;
		AnimationServices services = new AnimationServices();
		try {
			confRefs = context.getAllServiceReferences(
					Configuration.class.getName(),
					OSGiUtils.createFilter(
							RKMessagingConfigUtils.JMS_CONNECTION_CONFIG,
							connectionConfigId));
		} catch (InvalidSyntaxException ex) {
			theLogger.warn("Unable to find connection information to create remote animation player");
			return services;
		}
		if (confRefs == null || confRefs.length == 0) {
			theLogger.warn("Unable to find connection information to create remote animation player");
			return services;
		}
		ServiceReference conConfRef = confRefs[0];
		Configuration<String> conConf =
				OSGiUtils.getService(Configuration.class, context, conConfRef);
		String ip = conConf.getPropertyValue(String.class, ConnectionConfigUtils.CONF_BROKER_IP);
		Connection con = ConnectionManager.createConnection(
				ConnectionUtils.getUsername(), ConnectionUtils.getPassword(),
				"client1", "test", "tcp://" + ip + ":5672");
		try {
			con.start();
		} catch (JMSException ex) {
			theLogger.warn("Unable to connect to {}", ip);
			return services;
		}
		ManagedService myConnectionService = new OSGiComponent(context, new SimpleLifecycle(con, Connection.class));
		myConnectionService.start();
		services.addManagedService(myConnectionService);
		services.addServiceRegistration(ConnectionUtils.ensureSession(
				context, "remoteAnimConnection", con, null));
		services.addServiceRegistrations(ConnectionUtils.ensureDestinations(
				context, "remoteAnimationRequest", "animationRequest",
				ConnectionUtils.TOPIC, null));
		JMSAvroMessageSenderLifecycle senderLife =
				new JMSAvroMessageSenderLifecycle(
						new PortableAnimationEvent.MessageRecordAdapter(),
						AnimationEvent.class, AnimationRecord.class,
						"remoteAnimSender", "remoteAnimConnection",
						"remoteAnimationRequest");
		services.addOSGiComponent(registerEventFactory(context));
		ManagedService mySenderService = new OSGiComponent(context, senderLife);
		mySenderService.start();
		services.addManagedService(mySenderService);
		services.addServiceRegistration(ConnectionUtils.ensureSession(
				context, "remoteSignalConnection", con, null));
		services.addServiceRegistrations(ConnectionUtils.ensureDestinations(
				context, "remoteAnimationSignal", "animationSignal",
				ConnectionUtils.TOPIC, null));
		JMSAvroAsyncReceiverLifecycle receiverLife =
				new JMSAvroAsyncReceiverLifecycle(
						new PortableAnimationSignal.RecordMessageAdapter(),
						AnimationSignal.class, AnimationSignallingRecord.class,
						AnimationSignallingRecord.SCHEMA$, "remoteSignalReceiver",
						"remoteSignalConnection", "remoteAnimationSignal");
		ManagedService myReceiverService = new OSGiComponent(context, receiverLife);
		myReceiverService.start();
		services.addManagedService(myReceiverService);
		AnimationPlayerClientLifecycle myLifecycle =
				new AnimationPlayerClientLifecycle(
						"remotePlayer", "remotePlayer", "remoteAnimSender",
						"remoteSignalReceiver", context);
		ManagedService myPlayerService = new OSGiComponent(context, myLifecycle);
		myPlayerService.start();
		services.addManagedService(myPlayerService);

		return services;
	}

	private static OSGiComponent registerEventFactory(BundleContext context) {
		if (OSGiUtils.serviceExists(context, AnimationEvent.AnimationEventFactory.class, null)) {
			return null;
		}
		OSGiComponent eventFactory = new OSGiComponent(context,
				new SimpleLifecycle(
						new PortableAnimationEvent.Factory(),
						AnimationEventFactory.class)
		);
		eventFactory.start();
		return eventFactory;
	}

	private static ServiceRegistration registerChannelRobotParams(
			BundleContext context, Robot.Id robotId) {
		RobotChannelsLifecycle lifecycle = new RobotChannelsLifecycle(robotId);
		OSGiComponent comp = new OSGiComponent(context, lifecycle);
		comp.start();
		return null;
	}

	private static ServiceRegistration registerAnimationEditListener(
			BundleContext context, Robot robot) {
		AnimationEditFrameSource aefs =
				new AnimationEditFrameSource(robot);
		String[] classes = {
				AnimationEditListener.class.getName(),
				FrameSource.class.getName()};
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(Robot.PROP_ID, robot.getRobotId().toString());

		ServiceRegistration reg = context.registerService(classes, aefs, props);
		theLogger.info("AnimationEditFrameSource Service Registered Successfully.");
		return reg;
	}

	private static ServiceRegistration registerJointSnapshotAdder(
			BundleContext context, Robot.Id robotId) {
		JointSnapshot js = new JointSnapshot(context, robotId);
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(Robot.PROP_ID, robotId.toString());
		ServiceRegistration reg = context.registerService(
				PositionAdder.class.getName(), js, props);
		theLogger.info("JointSnapshot Service Registered Successfully.");
		return reg;
	}
}
