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
import org.mechio.api.motion.Robot.RobotPositionMap;

/**
 * Base interface for a response to a RobotRequest.
 * RobotResponse should be extended to add the fields needed to respond to a 
 * given RobotRequest.  Sending a simple RobotResponse can act as an 
 * acknowledgment of a RobotRequest.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface RobotResponse {
    /**
     * Returns the response header, which contains response meta-data.
     * @return response header
     */
    public RobotResponseHeader getResponseHeader();
    
    /**
     * Contains response meta-data.
     */
    public static interface RobotResponseHeader{
        /**
         * Returns the id of the Robot which is responding.
         * @return id of the Robot which is responding
         */
        public Robot.Id getRobotId();
        /**
         * Returns a String identifying the host.  Currently unused.
         * @return
         */
        public String getSourceId();
        /**
         * Returns a String identifying the host.  Currently unused.
         * @return
         */
        public String getDestinationId();
        /**
         * Returns the creation timestamp of the request being responded to.
         * @return creation timestamp of the request being responded to
         */
        public long getRequestTimestampMillisecUTC();
        /**
         * Returns the creation timestamp of the response.
         * @return creation timestamp of the response
         */
        public long getResponseTimestampMillisecUTC();
        /**
         * Returns the type of Request being responded to.
         * @return type of Request being responded to
         */
        public String getRequestType();
    }
    
    /**
     * RobotResponse to indicate a boolean status.
     */
    public static interface RobotStatusResponse extends RobotResponse {
        /**
         * Returns the status value being sent.
         * @return status value being sent
         */
        public boolean getStatusResponse();
    }
    
    /**
     * RobotResponse with a RobotPositionMap.
     */
    public static interface RobotPositionResponse extends RobotResponse {
        /**
         * Returns the RobotPositionMap being sent.
         * @return RobotPositionMap being sent
         */
        public RobotPositionMap getPositionMap();
    }
}
