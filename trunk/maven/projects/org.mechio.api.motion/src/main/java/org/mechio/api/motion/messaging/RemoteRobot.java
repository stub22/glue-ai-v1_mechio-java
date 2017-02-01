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
package org.mechio.api.motion.messaging;

import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Listener;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.mechio.api.motion.AbstractRobot;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.JointProperty;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.protocol.DefaultMotionFrame;
import org.mechio.api.motion.protocol.MotionFrame;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;
import org.mechio.api.motion.protocol.RobotDefinitionResponse.JointDefinition;
import org.mechio.api.motion.protocol.RobotDefinitionResponse.JointPropDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;

/**
 * RemoteRobot is a facade for controlling a remotely connected Robot.
 * The RemoteRobot controls the Robot using a RemoteRobotClient.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteRobot extends AbstractRobot<RemoteJoint> {
	private static final Logger theLogger = LoggerFactory.getLogger(RemoteRobot.class);
	private RemoteRobotClient myRobotClient;
	private RobotPositionMap myPreviousPositions;
	private MessageAsyncReceiver<RobotDefinitionResponse> myDefinitionReceiver;
	private Listener<RobotDefinitionResponse> myDefinitionListener;

	/**
	 * Creates a RemoteRobot which uses the given RemoteRobotClient.
	 *
	 * @param client             client for the remote Robot
	 * @param definitionReceiver MessageReceiver to retrieve robot data
	 */
	public RemoteRobot(
			RemoteRobotClient client,
			MessageAsyncReceiver<RobotDefinitionResponse> definitionReceiver) {
		super(client.getRobotId());
		myRobotClient = client;
		myDefinitionReceiver = definitionReceiver;
		updateRobotDefinition();

		myDefinitionListener = new Listener<RobotDefinitionResponse>() {
			@Override
			public void handleEvent(RobotDefinitionResponse t) {
				syncRobotDefinition(t);
			}
		};
		myDefinitionReceiver.addListener(myDefinitionListener);
	}

	private void updateRobotDefinition() {
		RobotDefinitionResponse robotDef =
				myRobotClient.requestRobotDefinition();
		updateRobotDefinition(robotDef);
	}

	private void updateRobotDefinition(RobotDefinitionResponse robotDef) {
		if (robotDef == null) {
			theLogger.warn("RobotRequest timed out.  "
					+ "Unable to update definition.");
			throw new NullPointerException();
		}
		for (JointDefinition def : robotDef.getJointDefinitions()) {
			RemoteJoint rj = new RemoteJoint(this, def);
			addJoint(rj);
		}
	}

	/**
	 * Adds the given Joint to the Robot.
	 *
	 * @param j Joint to add
	 * @return true if successful
	 */
	private boolean updateJoint(JointDefinition j) {
		boolean found = false;
		RemoteJoint joint = null;
		Joint.Id jId = j.getJointId();
		Robot.JointId jointId = new Robot.JointId(getRobotId(), jId);

		if (myJointMap.containsKey(jointId)) {
			joint = myJointMap.get(jointId);
			found = true;
		}

		if (!found || joint == null) {
			return false;
		}

		boolean enabled = j.getEnabled();
		if (enabled != joint.getEnabled()) {
			joint.setEnabled(enabled);
		}

		NormalizedDouble goalPosition = j.getGoalPosition();
		if (!goalPosition.equals(joint.getGoalPosition())) {
			joint.setGoalPosition(goalPosition);
		}

		for (JointPropDefinition prop : j.getJointProperties()) {
			JointProperty jointProp = joint.getProperty(prop.getPropertyName());
			Double initialValue = prop.getInitialValue();
			if (!initialValue.equals(jointProp.getValue())) {
				jointProp.setValue(initialValue);
			}
		}

		return true;
	}

	private void syncRobotDefinition(RobotDefinitionResponse robotDef) {
		if (robotDef == null) {
			theLogger.warn("RobotRequest timed out.  "
					+ "Unable to update definition.");
			throw new NullPointerException();
		}
		for (JointDefinition def : robotDef.getJointDefinitions()) {
			updateJoint(def);
		}
	}

	/**
	 * Updates all cached values.
	 *
	 * @return true if successful
	 */
	public boolean updateRobot() {
		try {
			updateRobotDefinition();
		} catch (NullPointerException ex) {
			return false;
		}
		return true;
	}

	/**
	 * Sends a command for the remote robot to connect.
	 *
	 * @return true if successful
	 */
	@Override
	public boolean connect() {
		Boolean ret = myRobotClient.sendConnect();
		if (ret == null) {
			theLogger.warn("RobotRequest timed out.  Unable to connect.");
			return false;
		}

		return ret;
	}

	/**
	 * Sends a command for the remote robot to disconnect.
	 */
	@Override
	public void disconnect() {
		Boolean ret = myRobotClient.sendDisconnect();
		if (ret == null) {
			theLogger.warn("RobotRequest timed out.  Unable to disconnect.");
		} else {
			myDefinitionReceiver.removeListener(myDefinitionListener);
		}
	}

	/**
	 * Returns the remote robot's connection status.
	 *
	 * @return true if successful
	 */
	@Override
	public boolean isConnected() {
		Boolean ret = myRobotClient.getConnected();
		if (ret == null) {
			theLogger.warn("RobotRequest timed out.  "
					+ "Unable to get connection status.");
			return false;
		}
		return ret;
	}

	@Override
	public void setEnabled(boolean val) {
		Boolean ret =
				val ? myRobotClient.sendEnable() : myRobotClient.sendDisable();
		if (ret == null) {
			theLogger.warn("RobotRequest timed out.  "
					+ "Unable to set enabled value.");
		}
	}

	@Override
	public boolean isEnabled() {
		Boolean ret = myRobotClient.getEnabled();
		if (ret == null) {
			theLogger.warn("RobotRequest timed out.  "
					+ "Unable to get enabled status.");
			return false;
		}
		return ret;
	}

	boolean setJointEnabled(JointId jId, boolean val) {
		Boolean ret = val ?
				myRobotClient.sendJointEnable(jId) :
				myRobotClient.sendJointDisable(jId);
		if (ret == null) {
			theLogger.warn("RobotRequest timed out.  "
					+ "Unable to get joint enabled confirmation.");
			return false;
		}
		return ret;
	}

	boolean getJointEnabled(JointId jId) {
		Boolean ret = myRobotClient.getJointEnabled(jId);
		if (ret == null) {
			theLogger.warn("RobotRequest timed out.  "
					+ "Unable to get joint enabled status.");
		}
		return ret;
	}

	@Override
	public void move(RobotPositionMap positions, long lenMillisec) {
		MotionFrame frame = new DefaultMotionFrame();
		frame.setFrameLengthMillisec(lenMillisec);
		frame.setGoalPositions(positions);
		frame.setPreviousPositions(myPreviousPositions);
		frame.setTimestampMillisecUTC(TimeUtils.now());
		myRobotClient.sendMovement(frame);
		myPreviousPositions = positions;
		setGoals(myPreviousPositions);
	}

	private void setGoals(RobotPositionMap goals) {
		for (Entry<JointId, NormalizedDouble> e : goals.entrySet()) {
			JointId jId = e.getKey();
			NormalizedDouble val = e.getValue();
			RemoteJoint j = getJoint(jId);
			if (j == null) {
				continue;
			}
			j.setGoalPosition(val);
		}
	}

	public void setDefinitionReceiver(
			MessageAsyncReceiver<RobotDefinitionResponse> receiver) {
		if (isConnected()) {
			myDefinitionReceiver.removeListener(myDefinitionListener);
		}

		myDefinitionReceiver = receiver;

		if (isConnected() && myDefinitionReceiver != null) {
			myDefinitionReceiver.addListener(myDefinitionListener);
		}
	}
}
