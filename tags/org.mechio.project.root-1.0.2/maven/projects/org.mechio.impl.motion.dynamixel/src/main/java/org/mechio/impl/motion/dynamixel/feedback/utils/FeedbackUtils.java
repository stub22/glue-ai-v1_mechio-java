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
package org.mechio.impl.motion.dynamixel.feedback.utils;

import java.util.ArrayList;
import java.util.List;
import org.mechio.impl.motion.dynamixel.DynamixelServo;
import org.mechio.impl.motion.dynamixel.feedback.ConcurrentDynamixelCache;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelServoCache;
import org.mechio.impl.motion.dynamixel.feedback.FeedbackUpdateValues;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class FeedbackUtils {
    public static List<DynamixelServo.Id> getHotServos(
            ConcurrentDynamixelCache cache, int maxTemp){
        List<DynamixelServo.Id> ids = null;
        for(DynamixelServoCache c : cache.getValueMap().values()){
            int temp = c.getFeedback().getCurrentTemperature();
            c.getFeedback().getCurrentVoltage();
            if(temp >= maxTemp){
                if(ids == null){
                    ids = new ArrayList<DynamixelServo.Id>();
                }
                ids.add(c.getServoId());
            }
        }
        return ids;
    }
    
    public static List<DynamixelServo.Id> getOverLimit(
            ConcurrentDynamixelCache cache, int index, int valLimit, int dir){
        List<DynamixelServo.Id> ids = null;
        for(DynamixelServoCache c : cache.getValueMap().values()){
            if(checkValue(c.getFeedback(), index, valLimit, dir)){
                if(ids == null){
                    ids = new ArrayList<DynamixelServo.Id>();
                }
                ids.add(c.getServoId());
            }
        }
        return ids;
    }
    
    public static boolean isOverLimit(
            ConcurrentDynamixelCache cache, int index, int valLimit, int dir){
        for(DynamixelServoCache c : cache.getValueMap().values()){
            if(checkValue(c.getFeedback(), index, valLimit, dir)){
                return true;
            }
        }
        return false;
    }
    
    public static boolean checkValue(
            FeedbackUpdateValues feedback, int index, int valLimit, int dir){
        if(feedback == null){
            return false;
        }
        int[] vals = feedback.getValues();
        if(vals == null || vals.length <= index){
            return false;
        }
        if(dir > 0){
            return vals[index] >= valLimit;
        }else if(dir == 0){
            return vals[index] == valLimit;
        }else{
            return vals[index] <= valLimit;
        }
    }
}
