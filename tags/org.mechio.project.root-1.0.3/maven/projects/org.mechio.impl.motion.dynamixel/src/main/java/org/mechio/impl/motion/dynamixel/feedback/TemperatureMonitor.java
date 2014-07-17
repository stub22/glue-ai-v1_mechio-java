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

import java.util.ArrayList;
import java.util.List;
import org.mechio.api.motion.servos.ServoController.ServoId;
import org.mechio.impl.motion.dynamixel.DynamixelController;
import org.mechio.impl.motion.dynamixel.DynamixelServo;
import org.mechio.impl.motion.dynamixel.feedback.utils.FeedbackUtils;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class TemperatureMonitor {
    private List<ServoId<DynamixelServo.Id>> myHotServoIds;
    private boolean myCooldownFlag;
    private DynamixelController myController;
    private DynamixelControlSettings mySettings;
    private ConcurrentDynamixelCache myCache;
    
    public TemperatureMonitor(DynamixelController controller, DynamixelControlSettings settings, ConcurrentDynamixelCache cache){
        if(controller == null || settings == null || cache == null){
            throw new NullPointerException();
        }
        myController = controller;
        mySettings = settings;
        myCache = cache;
        myHotServoIds = new ArrayList<ServoId<DynamixelServo.Id>>();
    }
    
    public void cooldown(){
        disableHotServos((int)mySettings.getMaxRunTemperature());
        enableCoolServos();
    }
    
    /**
     * Disables hot servos.  Returns true if one or more servos are hot.
     * @param maxTemp
     * @return 
     */
    public boolean disableHotServos(int maxTemp){
        List<DynamixelServo.Id> hotServos = FeedbackUtils.getHotServos(
                myCache, (int)mySettings.getMaxRunTemperature());
        if(hotServos == null || hotServos.isEmpty()){
            return false;
        }
        myCooldownFlag = true;
        for(DynamixelServo.Id id : hotServos){
            ServoId<DynamixelServo.Id> globalId = 
                    new ServoId(myController.getId(), id);
            if(!myHotServoIds.contains(globalId)){
                myHotServoIds.add(globalId);
                DynamixelServo servo = myController.getServo(globalId);
                servo.setEnabled(false);
            }
        }
        return true;
    }
    
    public void enableCoolServos(){
        List<DynamixelServo.Id> coolServos = FeedbackUtils.getOverLimit(
                myCache, FeedbackUpdateValues.TEMPERATURE,
                (int)mySettings.getCooldownTemperature(), -1);
        if(coolServos == null || coolServos.isEmpty()){
            return;
        }
        for(DynamixelServo.Id id : coolServos){
            ServoId<DynamixelServo.Id> globalId = 
                    new ServoId(myController.getId(), id);
            if(myHotServoIds.contains(globalId)){
                DynamixelServo servo = myController.getServo(globalId);
                servo.setEnabled(true);
                myHotServoIds.remove(globalId);
            }
        }
    }
}
