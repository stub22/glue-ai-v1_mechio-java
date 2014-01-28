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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A DefaultMotionFrame describes the start and goal PositionSets for a set of \
 * Joints over an interval of time.
 * 
 * @param <PosMap> Type of JointPositionMap used
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DefaultMotionFrame<PosMap extends JointPositionMap> implements 
        MotionFrame<PosMap>{
    private final static Logger theLogger = Logger.getLogger(DefaultMotionFrame.class.getName());
    private long myTime;
    private long myInterval;
    private PosMap myGoalPositions;
    private PosMap myPreviousPositions;

    /**
     * Creates an empty MotionFrame.
     */
    public DefaultMotionFrame(){
        myTime = 0;
        myInterval = 0;
    }

    /**
     * Sets the MotionFrame creation time. 
     * @param time create time
     */
    @Override
    public void setTimestampMillisecUTC(long time){
        if(time <= 0){
            theLogger.log(Level.WARNING, "Frame start time must be greater than 0.");
            return;
        }
        myTime = time;
    }

    /**
     * Returns MotionFrame creation time.
     * @return MotionFrame creation time
     */
    @Override
    public long getTimestampMillisecUTC(){
        return myTime;
    }
    /**
     * Sets the time interval length for the MotionFrame.
     * The velocity is calculated as the difference of position divided by the
     * interval length.
     * @param interval length of time for the frame in milliseconds
     */
    @Override
    public void setFrameLengthMillisec(long interval){
        if(interval <= 0){
            theLogger.log(Level.WARNING, "Frame interval length must be greater than 0.");
            return;
        }
        myInterval = interval;
    }
    
    /**
     * Returns the interval for the movements.
     * @return interval time interval for the movements
     */
    @Override
    public long getFrameLengthMillisec(){
        return myInterval;
    }

    /**
     * Sets the MotionFrame's goal JointPositionMap.
     * @param pos goal JointPositionMap
     */
    @Override
    public void setGoalPositions(PosMap pos){
        myGoalPositions = pos;
    }

    /**
     * Returns the MotionFrame's goal JointPositionMap.
     * @return MotionFrame's goal JointPositionMap
     */
    @Override
    public PosMap getGoalPositions(){
        return myGoalPositions;
    }

    /**
     * Set the MotionFrame's previous JointPositionMap.
     * @param pos previous JointPositionMap
     */
    @Override
    public void setPreviousPositions(PosMap pos){
        myPreviousPositions = pos;
    }

    /**
     * Returns the MotionFrame's previous JointPositionMap.
     * @return MotionFrame's previous JointPositionMap
     */
    @Override
    public PosMap getPreviousPositions(){
        return myPreviousPositions;
    }
}
