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

import org.mechio.api.motion.Robot.RobotPositionMap;

/**
 * A MotionFrame with messaging metadata.
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface MotionFrameEvent {
    /**
     * Returns the id String for the source of the MotionFrameEvent.
     * @return id String for the source of the MotionFrameEvent
     */
    public String getSourceId();
    /**
     * Returns the id String for the destination of the MotionFrameEvent.
     * @return id String for the destination of the MotionFrameEvent
     */
    public String getDestinationId();
    /**
     * Returns the timestamp of the MotionFrameEvent.
     * @return timestamp of the MotionFrameEvent
     */
    public long getTimestampMillisecUTC();
    /**
     * Returns the MotionFrame associated with this MotionFrameEvent.
     * @return MotionFrame associated with this MotionFrameEvent
     */
    public MotionFrame<RobotPositionMap> getMotionFrame();
    
    public static interface MotionFrameEventFactory {
        public MotionFrameEvent createMotionFrameEvent(
                String sourceId, String destId, 
                MotionFrame<RobotPositionMap> motionFrame);
    }
}
