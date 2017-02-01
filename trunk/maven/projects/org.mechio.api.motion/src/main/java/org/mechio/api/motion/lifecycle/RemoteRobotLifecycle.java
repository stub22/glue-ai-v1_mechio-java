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

import org.jflux.api.messaging.rk.Constants;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.messaging.RemoteRobot;
import org.mechio.api.motion.messaging.RemoteRobotClient;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteRobotLifecycle extends
		AbstractLifecycleProvider<Robot, RemoteRobot> {
	private static final Logger theLogger = LoggerFactory.getLogger(RemoteRobotLifecycle.class);
	private final static String theRobotClient = "remoteRobotClient";
	private final static String theDefReceiver = "robotDefinitionReceiver";

	public RemoteRobotLifecycle(Robot.Id robotId, String receiverId) {
		super(new DescriptorListBuilder()
				.dependency(theRobotClient, RemoteRobotClient.class)
				.with(Robot.PROP_ID, robotId.getRobtIdString())
				.dependency(theDefReceiver, MessageAsyncReceiver.class)
				.with(Constants.PROP_MESSAGE_RECEIVER_ID, receiverId)
				.with(Constants.PROP_MESSAGE_TYPE,
						RobotDefinitionResponse.class.getName())
				.getDescriptors());
		if (robotId == null) {
			throw new NullPointerException();
		}
		if (myRegistrationProperties == null) {
			myRegistrationProperties = new Properties();
		}
		myRegistrationProperties.put(Robot.PROP_ID, robotId.getRobtIdString());
		myServiceClassNames = new String[]{
				Robot.class.getName(), RemoteRobot.class.getName()
		};
	}

	@Override
	protected RemoteRobot create(Map<String, Object> services) {
		RemoteRobotClient client =
				(RemoteRobotClient) services.get(theRobotClient);
		MessageAsyncReceiver receiver =
				(MessageAsyncReceiver) services.get(theDefReceiver);
		try {
			receiver.start();
			return new RemoteRobot(client, receiver);
		} catch (Exception ex) {
			theLogger.warn("Error starting RemoteRobot "
					+ "messaging components.", ex);
		}
		return null;
	}

	@Override
	protected void handleChange(
			String serviceId, Object service, Map<String, Object> dependencies) {
		if (myService == null) {
			return;
		}
		if (theRobotClient.equals(serviceId)) {
			if (service != null) {
				myService = create(dependencies);
			} else {
				myService = null;
			}
		} else if (theDefReceiver.equals(serviceId)) {
			myService.setDefinitionReceiver((MessageAsyncReceiver) service);
		}
	}

	@Override
	public Class<Robot> getServiceClass() {
		return Robot.class;
	}
}
