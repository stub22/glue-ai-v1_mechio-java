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
package org.mechio.impl.motion.dynamixel.feedback;

import org.mechio.impl.motion.dynamixel.DynamixelServo;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DynamixelServoCache implements MoveParams<DynamixelServo.Id>{    
    private DynamixelServo.Id myId;
    private FeedbackUpdateValues myFeedbackVals;
    private GoalUpdateValues myGoalValues;
    private GoalUpdateValues myPrevGoalValues;
    private long myCommandDelayMiiilsec;

    public DynamixelServoCache(DynamixelServo.Id id){
        if(id == null){
            throw new NullPointerException();
        }
        myId = id;
        myPrevGoalValues = new GoalUpdateValues(id, 0, 0);
        myGoalValues = new GoalUpdateValues(id, 0, 0);
        myFeedbackVals = new FeedbackUpdateValues(id, new int[5], 0);
        myCommandDelayMiiilsec = 2; 
    }
    
    public void setFeedbackVals(FeedbackUpdateValues vals){
        myFeedbackVals = vals;
    }
    
    public void setGoalVals(GoalUpdateValues vals){
        myGoalValues = vals;
    }
    
    @Override
    public void goalsSent(){
        myPrevGoalValues = myGoalValues;
    }
    
    @Override
    public DynamixelServo.Id getServoId(){
        return myId;
    }

    @Override
    public int getCurrentPosition() {
        return myFeedbackVals.getCurrentPosition();
    }

    @Override
    public int getCurrentSpeed() {
        return myFeedbackVals.getCurrentSpeed();
    }

    @Override
    public int getCurrentVoltage() {
        return myFeedbackVals.getCurrentVoltage();
    }

    @Override
    public int getCurrentTemperature() {
        return myFeedbackVals.getCurrentTemperature();
    }

    @Override
    public int getCurrentLoad() {
        return myFeedbackVals.getCurrentLoad();
    }

    @Override
    public long getCommandDelayMillisec() {
        return myCommandDelayMiiilsec;
    }

    @Override
    public long getCurPosTimestampUTC() {
        return myFeedbackVals.getUpdateTimestamp();
    }

    @Override
    public long getPrevGoalTargetTimeUTC() {
        return myPrevGoalValues.getGoalTargetTimeUTC();
    }

    @Override
    public int getPrevGoalPosition() {
        return myPrevGoalValues.getGoalPosition();
    }

    @Override
    public long getGoalTargetTimeUTC() {
        return myGoalValues.getGoalTargetTimeUTC();
    }

    @Override
    public int getGoalPosition() {
        return myGoalValues.getGoalPosition();
    }
    
    public FeedbackUpdateValues getFeedback(){
        return myFeedbackVals;
    }
    
    /**
     * Command Delay is the estimated number of milliseconds between sending a
     * move command and when the servo responds to the command.
     * @param msec 
     */
    public void setCommandDelayMillisec(long msec){
        myCommandDelayMiiilsec = msec;
    }
}
