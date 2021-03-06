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
package org.mechio.api.motion.lifecycle;

import org.jflux.api.common.rk.utils.ListenerConstants;
import org.jflux.api.core.Listener;
import org.jflux.api.messaging.rk.Constants;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.jflux.spec.discovery.SerialNumberSpec;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.messaging.RemoteRobotHost;
import org.mechio.api.motion.messaging.RobotResponseFactory;
import org.mechio.api.motion.protocol.MotionFrameEvent;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;
import org.mechio.api.motion.protocol.RobotRequest;
import org.mechio.api.motion.protocol.RobotResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
@Deprecated
public class RemoteRobotHostLifecycle extends
		AbstractLifecycleProvider<RemoteRobotHost, RemoteRobotHost> {
	private static final Logger theLogger = LoggerFactory.getLogger(RemoteRobotHostLifecycle.class);
	private final static String theRobot = "robot";
	private final static String theRequestReceiver = "requestReceiver";
	private final static String theResponseSender = "responseSender";
	private final static String theFrameReceiver = "frameReceiver";
	private final static String theMoveHandler = "moveHandler";
	private final static String theResponseFactory = "responseFactory";
	private final static String theDefSender = "definitionSender";

	private String mySourceId;
	private String myDestId;
	private String mySerial;
	private int mySendDataIntervalMillisec = RemoteRobotHost.DEFAULT_SEND_DATA_INTERVAL;

	public RemoteRobotHostLifecycle(String sourceId, String destId,
									Robot.Id robotId, String reqReceiverId, String respSenderId,
									String frameReceiverId, String moveHandlerId, String defSenderId,
									String serial, int sendDataIntervalMillisec) {
		super(new DescriptorListBuilder()
				.dependency(theRobot, Robot.class)
				.with(Robot.PROP_ID, robotId.getRobtIdString())
				.dependency(theRequestReceiver, MessageAsyncReceiver.class)
				.with(Constants.PROP_MESSAGE_RECEIVER_ID, reqReceiverId)
				.with(Constants.PROP_MESSAGE_TYPE,
						RobotRequest.class.getName())
				.dependency(theResponseSender, MessageSender.class)
				.with(Constants.PROP_MESSAGE_SENDER_ID, respSenderId)
				.with(Constants.PROP_MESSAGE_TYPE,
						RobotResponse.class.getName())
				.dependency(theFrameReceiver, MessageAsyncReceiver.class)
				.with(Constants.PROP_MESSAGE_RECEIVER_ID, frameReceiverId)
				.with(Constants.PROP_MESSAGE_TYPE,
						MotionFrameEvent.class.getName())
				.dependency(theMoveHandler, Listener.class)
				.with(ListenerConstants.PROP_LISTENER_ID, moveHandlerId)
				.with(ListenerConstants.PROP_LISTENER_TYPE,
						MotionFrameEvent.class.getName())
				.dependency(theResponseFactory, RobotResponseFactory.class)
				.dependency(theDefSender, MessageSender.class)
				.with(Constants.PROP_MESSAGE_SENDER_ID, defSenderId)
				.with(Constants.PROP_MESSAGE_TYPE,
						RobotDefinitionResponse.class.getName())
				.getDescriptors());
		if (sourceId == null || destId == null) {
			throw new NullPointerException();
		}
		mySourceId = sourceId;
		myDestId = destId;
		mySerial = serial;
		mySendDataIntervalMillisec = sendDataIntervalMillisec;
	}

	@Override
	protected RemoteRobotHost create(Map<String, Object> services) {
		Robot robot = (Robot) services.get(theRobot);
		MessageAsyncReceiver<RobotRequest> reqReceiver =
				(MessageAsyncReceiver) services.get(theRequestReceiver);
		MessageSender<RobotResponse> respSender =
				(MessageSender<RobotResponse>) services.get(theResponseSender);
		MessageAsyncReceiver<MotionFrameEvent> frameReceiver =
				(MessageAsyncReceiver) services.get(theFrameReceiver);
		Listener<MotionFrameEvent> moveHandler =
				(Listener<MotionFrameEvent>) services.get(theMoveHandler);
		RobotResponseFactory respFact =
				(RobotResponseFactory) services.get(theResponseFactory);
		MessageSender<RobotDefinitionResponse> defSender =
				(MessageSender<RobotDefinitionResponse>) services.get(
						theDefSender);
		SerialNumberSpec spec = new SerialNumberSpec();
		spec.setSerialNumber(mySerial);
		RemoteRobotHost host = new RemoteRobotHost(
				robot, mySourceId, myDestId, respSender, reqReceiver, respFact,
				frameReceiver, moveHandler, defSender, spec, mySendDataIntervalMillisec);
		try {
			reqReceiver.start();
			respSender.start();
			defSender.start();
			frameReceiver.start();
		} catch (Exception ex) {
			theLogger.warn("Error starting RemoteRobotHost "
					+ "messaging components.", ex);
		}
		return host;
	}

	@Override
	protected void handleChange(
			String serviceId, Object service, Map<String, Object> dependencies) {
		if (myService == null) {
			if (isSatisfied()) {
				myService = create(dependencies);
			}
			return;
		}
		if (theRobot.equals(serviceId)) {
			myService.setRobot((Robot) service);
		} else if (theResponseSender.equals(serviceId)) {
			myService.setResponseSender((MessageSender<RobotResponse>) service);
		} else if (theRequestReceiver.equals(serviceId)) {
			myService.setRequestReceiver(
					(MessageAsyncReceiver<RobotRequest>) service);
		} else if (theFrameReceiver.equals(serviceId)) {
			myService.setMotionFrameReceiver(
					(MessageAsyncReceiver<MotionFrameEvent>) service);
		} else if (theMoveHandler.equals(serviceId)) {
			myService.setMoveHandler((Listener<MotionFrameEvent>) service);
		} else if (theResponseFactory.equals(serviceId)) {
			myService.setResponseFactory((RobotResponseFactory) service);
		} else if (theDefSender.equals(serviceId)) {
			myService.setDefSender(
					(MessageSender<RobotDefinitionResponse>) service);
		}
	}

	@Override
	public Class<RemoteRobotHost> getServiceClass() {
		return RemoteRobotHost.class;
	}
}
