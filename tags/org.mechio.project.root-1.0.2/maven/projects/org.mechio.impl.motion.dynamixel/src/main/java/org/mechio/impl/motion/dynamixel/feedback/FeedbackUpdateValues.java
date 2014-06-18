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
public class FeedbackUpdateValues {
    public final static int POSITION = 0;
    public final static int SPEED = 1;
    public final static int LOAD = 2;
    public final static int VOLTAGE = 3;
    public final static int TEMPERATURE = 4;
    
    private DynamixelServo.Id myId;
    private int[] myFeedbackValues;
    private long myFeedbackTimestamp;
    
    public FeedbackUpdateValues(DynamixelServo.Id id, int[] vals, long timestamp){
        myId = id;
        myFeedbackValues = vals;
        vals[SPEED] = setDirection(vals[SPEED]);
        vals[LOAD] = setDirection(vals[LOAD]);
        myFeedbackTimestamp = timestamp;
    }
    
    private int setDirection(int val){
        //  bit 10 indicates the direction
        if ((val & 0x400) != 0){
            return -(val & 0x3FF);
        }
        return val;
    }
    
    public DynamixelServo.Id getServoId(){
        return myId;
    }

    public int getCurrentPosition() {
        return myFeedbackValues[POSITION];
    }

    public int getCurrentSpeed() {
        return myFeedbackValues[SPEED];
    }

    public int getCurrentLoad() {
        return myFeedbackValues[LOAD];
    }

    public int getCurrentVoltage() {
        return myFeedbackValues[VOLTAGE];
    }

    public int getCurrentTemperature() {
        return myFeedbackValues[TEMPERATURE];
    }

    public long getUpdateTimestamp() {
        return myFeedbackTimestamp;
    }
    
    public int[] getValues(){
        return myFeedbackValues;
    }
}
