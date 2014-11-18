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

/**
 * A MotionFrame describes the start and goal PositionSets for a set of Joints \
 * over an interval of time.
 * 
 * @param <PosMap> Type of JointPositionMap used
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface MotionFrame<PosMap extends JointPositionMap> {
    /**
     * Sets the MotionFrame creation time. 
     * @param time create time
     */
    public void setTimestampMillisecUTC(long time);

    /**
     * Returns MotionFrame creation time.
     * @return MotionFrame creation time
     */
    public long getTimestampMillisecUTC();
    /**
     * Sets the time interval length for the MotionFrame.
     * The velocity is calculated as the difference of position divided by the
     * interval length.
     * @param interval length of time for the frame in milliseconds
     */
    public void setFrameLengthMillisec(long interval);
    
    /**
     * Returns the interval for the movements.
     * @return interval time interval for the movements
     */
    public long getFrameLengthMillisec();

    /**
     * Sets the MotionFrame's goal JointPositionMap.
     * @param pos goal JointPositionMap
     */
    public void setGoalPositions(PosMap pos);

    /**
     * Returns the MotionFrame's goal JointPositionMap.
     * @return MotionFrame's goal JointPositionMap
     */
    public PosMap getGoalPositions();

    /**
     * Set the MotionFrame's previous JointPositionMap.
     * @param pos previous JointPositionMap
     */
    public void setPreviousPositions(PosMap pos);

    /**
     * Returns the MotionFrame's previous JointPositionMap.
     * @return MotionFrame's previous JointPositionMap
     */
    public PosMap getPreviousPositions();
}
