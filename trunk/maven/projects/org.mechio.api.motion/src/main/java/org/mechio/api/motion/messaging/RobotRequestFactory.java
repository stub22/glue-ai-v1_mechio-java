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
import org.mechio.api.motion.Robot.JointId;
import org.mechio.api.motion.protocol.RobotRequest;

/**
 * Factory for creating new RobotRequest Messages.
 * 
 * @param <Req> type of RobotRequest built by this factory
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface RobotRequestFactory<Req extends RobotRequest> {
    /**
     * Creates a new RobotRequest with the given values.
     * @param robotId requested robot
     * @param sourceId request source id
     * @param destId request destination id
     * @param requestType request type
     * @param timestampMillisecUTC request timestamp
     * @return new RobotRequest
     */
    public Req buildRobotRequest(
            Robot.Id robotId, String sourceId, String destId, 
            String requestType, long timestampMillisecUTC);
    
    /**
     * Creates a new RobotRequest for the given Joint.
     * @param jointId global JointId of the requested Joint
     * @param sourceId request source id
     * @param destId request destination id
     * @param requestType request type
     * @param timestampMillisecUTC request timestamp
     * @return new RobotRequest for the given Joint
     */
    public Req buildJointRequest(
            JointId jointId, String sourceId, String destId, 
            String requestType, long timestampMillisecUTC);
}
