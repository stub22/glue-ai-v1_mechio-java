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
package org.mechio.impl.motion.openservo.feedback;

import org.mechio.impl.motion.openservo.OpenServo;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class OpenServoFeedbackValues {
    public final static int POSITION = 0;
    public final static int SPEED = 1;
    public final static int LOAD = 2;
    public final static int PWM_CW_CCW = 3;
    public final static int GOAL_POSITION = 4;
    public final static int GOAL_SPEED = 5;
    public final static int VOLTAGE = 6;
    
    private OpenServo.Id myId;
    private int[] myFeedbackValues;
    private long myFeedbackTimestamp;
    
    public OpenServoFeedbackValues(OpenServo.Id id, int[] vals, long timestamp){
        myId = id;
        myFeedbackValues = vals;
        myFeedbackTimestamp = timestamp;
    }
    
    public OpenServo.Id getServoId(){
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

    public long getUpdateTimestamp() {
        return myFeedbackTimestamp;
    }
    
    public int[] getValues(){
        return myFeedbackValues;
    }
}
