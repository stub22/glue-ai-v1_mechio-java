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
package org.mechio.api.motion.protocol;

import org.mechio.api.motion.Robot;

/**
 * Request message for remote robot commands.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface RobotRequest {
    /**
     * Request type for requesting a robot definition.
     */
    public final static String CMD_GET_ROBOT_DEFINITION = "getRobotDefinition";
    /**
     * Request type for requesting a robot to connect.
     */
    public final static String CMD_CONNECT_ROBOT = "connectRobot";
    /**
     * Request type for requesting a robot to disconnect.
     */
    public final static String CMD_DISCONNECT_ROBOT = "disconnectRobot";
    /**
     * Request type for requesting a robot's connection status.
     */
    public final static String CMD_GET_CONNECTION_STATUS = "getConnectionStatus";
    /**
     * Request type for requesting a robot to be enabled.
     */
    public final static String CMD_ENABLE_ROBOT = "enableRobot";
    /**
     * Request type for requesting a robot to be disabled.
     */
    public final static String CMD_DISABLE_ROBOT = "disableRobot";
    /**
     * Request type for requesting a robot's enabled status.
     */
    public final static String CMD_GET_ENABLED_STATUS = "getEnabledStatus";
    /**
     * Request type for requesting a joint be enable.
     */
    public final static String CMD_ENABLE_JOINT = "enableJoint";
    /**
     * Request type for requesting a joint be disabled.
     */
    public final static String CMD_DISABLE_JOINT = "disableJoint";
    /**
     * Request type for requesting a joint's enabled status.
     */
    public final static String CMD_GET_JOINT_ENABLED_STATUS = "getJointEnabledStatus";
    /**
     * Request type for requesting a robot's default positions.
     */
    public final static String CMD_GET_DEFAULT_POSITIONS = "getDefaultPositions";
    /**
     * Request type for requesting a robot's goal positions.
     */
    public final static String CMD_GET_GOAL_POSITIONS = "getGoalPositions";
    /**
     * Request type for requesting a robot's current positions.
     */
    public final static String CMD_GET_CURRENT_POSITIONS = "getGoalPositions";
    
    /**
     * Returns the id of the robot to receive the request.
     * @return id of the robot to receive the request
     */
    public Robot.Id getRobotId();
    /**
     * Returns a String identifying the request source.  Currently unused.
     * @return String identifying the request source
     */
    public String getSourceId();
    /**
     * Returns a String identifying the request destination.  Currently unused.
     * @return String identifying the request destination
     */
    public String getDestinationId();
    /**
     * RequestType identifies what is being requested of the Robot.
     * @return String identifying what is being requested of the Robot
     */
    public String getRequestType();
    /**
     * Returns the Joint id number if the request is intended for a Joint,
     * otherwise returns null.
     * @return Joint id number if the request is intended for a Joint,
     * otherwise returns null
     */
    public Integer getRequestIndex();
    /**
     * Returns the timestamp the request was made.
     * @return timestamp the request was made
     */
    public long getTimestampMillisecUTC();
}
