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

import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;
import org.mechio.api.motion.protocol.RobotResponse.RobotPositionResponse;
import org.mechio.api.motion.protocol.RobotResponse.RobotResponseHeader;
import org.mechio.api.motion.protocol.RobotResponse.RobotStatusResponse;

/**
 * Factory for creating the default types of RobotResponses.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface RobotResponseFactory{
    /**
     * Creates a response header.
     * @param robotId responding robot
     * @param sourceId response source
     * @param destinationId response destination
     * @param requestType type of request being responded to 
     * @param requestTimestamp timestamp of the request being responded to
     * @return new response header
     */
    public RobotResponseHeader createHeader(Robot.Id robotId, String sourceId, String destinationId, String requestType, long requestTimestamp);
    /**
     * Creates a new RobotDefinitionResponse.
     * @param header response header to use
     * @param robot robot to define
     * @return new RobotDefinitionResponse
     */
    public RobotDefinitionResponse createDefinitionResponse(RobotResponseHeader header, Robot robot);
    /**
     * Creates a new RobotStatusResponse.
     * @param header response header to use
     * @param status response status value
     * @return new RobotStatusResponse
     */
    public RobotStatusResponse createStatusResponse(RobotResponseHeader header, boolean status);
    /**
     * Creates a new RobotPositionResponse.
     * @param header response header to use
     * @param positions RobotPositionMap to respond with
     * @return new RobotPositionResponse
     */
    public RobotPositionResponse createPositionResponse(RobotResponseHeader header, RobotPositionMap positions);
}
