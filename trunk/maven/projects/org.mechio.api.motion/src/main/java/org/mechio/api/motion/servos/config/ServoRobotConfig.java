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

package org.mechio.api.motion.servos.config;

import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.servos.ServoController;
import org.mechio.api.motion.servos.ServoRobot;
import org.mechio.api.motion.servos.ServoRobot.ServoControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configurations for a ServoRobot.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ServoRobotConfig {
	private static final Logger theLogger = LoggerFactory.getLogger(ServoRobotConfig.class);

	private Robot.Id myRobotId;
	private Map<Joint.Id, ServoController.ServoId> myIdMap;
	private List<ServoRobot.ServoControllerContext> myControllerContexts;

	/**
	 * Creates an empty RobotConfig.
	 */
	public ServoRobotConfig() {
		myIdMap = new HashMap();
		myControllerContexts = new ArrayList();
	}

	/**
	 * Returns the Id.
	 *
	 * @return the Id
	 */
	public Robot.Id getRobotId() {
		return myRobotId;
	}

	/**
	 * Sets the Id to use with the Robot
	 *
	 * @param robotId Robot.Id to use
	 */
	public void setRobotId(Robot.Id robotId) {
		myRobotId = robotId;
	}

	/**
	 * Add a Joint to the RobotConfig with the given id and name.
	 *
	 * @param id Joint logical id
	 */
	public void addServoJoint(Joint.Id id, ServoController.ServoId servoId) {
		if (myIdMap.containsKey(id)) {
			theLogger.warn("Unable to add entry (jointId={}, servoId={}).  "
							+ "JointId already exists.",
					id, servoId);
		}
		myIdMap.put(id, servoId);
	}

	/**
	 * Returns a Map of JointIds and their Corresponding ServoIds.
	 */
	public Map<Joint.Id, ServoController.ServoId> getIdMap() {
		return myIdMap;
	}

	/**
	 * Adds a ServoControllerConfig to the RobotConfig.
	 *
	 * @param context context to set
	 */
	public void addControllerContext(ServoControllerContext context) {
		if (context == null) {
			throw new NullPointerException();
		}
		myControllerContexts.add(context);
	}

	/**
	 * Returns the RobotConfig's JointControllerConnections.
	 *
	 * @return RobotConfig's JointControllerConnections
	 */
	public List<ServoControllerContext> getControllerContexts() {
		return Collections.unmodifiableList(myControllerContexts);
	}
}
