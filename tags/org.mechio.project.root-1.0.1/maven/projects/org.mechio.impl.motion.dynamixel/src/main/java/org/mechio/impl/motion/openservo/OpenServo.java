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
package org.mechio.impl.motion.openservo;

import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.utils.Utils;
import org.jflux.impl.services.rk.utils.LocalIdentifier;
import org.mechio.api.motion.servos.AbstractServo;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.impl.motion.openservo.feedback.OpenServoFeedbackValues;

/**
 *
 * @author matt
 */
public class OpenServo extends AbstractServo<
        OpenServo.Id,
        ServoConfig<OpenServo.Id>,
        OpenServoController> {
    private NormalizedDouble myDefaultPosition;
    private boolean myEnabledFlag;
    private OpenServoFeedbackValues myFeedbackVals;
    
    public OpenServo(
            ServoConfig<OpenServo.Id> config, OpenServoController controller){
        super(config, controller);
        myEnabledFlag = false;
        double range = myConfig.getMaxPosition() - myConfig.getMinPosition();
        double defVal = myConfig.getDefaultPosition() - myConfig.getMinPosition();
        double def = defVal/range;
        myDefaultPosition = new NormalizedDouble(def);
    }
    
    public void setFeedbackVals(OpenServoFeedbackValues vals){
        myFeedbackVals = vals;
    }
    
    public NormalizedDouble getCurrentPosition(){
        if(myFeedbackVals == null){
            return myDefaultPosition;
        }
        double min = myConfig.getMinPosition();
        double max = myConfig.getMaxPosition();
        Double range = max - min;
        Double abs = myFeedbackVals.getCurrentPosition() - min;
        Double pos = Utils.bound(abs/range, 0.0, 1.0);
        return new NormalizedDouble(pos);
    }
    
    public Integer getCurrentLoad(){
        if(myFeedbackVals == null){
            return 0;
        }
        return myFeedbackVals.getCurrentLoad();
    }
    
    public Integer getCurrentVoltage(){
        if(myFeedbackVals == null){
            return 0;
        }
        return myFeedbackVals.getCurrentVoltage();
    }

    @Override
    public Boolean getEnabled() {
        return myEnabledFlag;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        if(setPWMEnabledRegister(enabled)){
            myEnabledFlag = enabled;
        }
    }
    
    private boolean setPWMEnabledRegister(Boolean enabled){
        if(enabled){
            myController.enableServo(getId());
        }else{
            myController.disableServo(getId());
        }
        return true;
    }

    @Override
    public String getName() {
        return myConfig.getName();
    }

    @Override
    public int getMinPosition() {
        return myConfig.getMinPosition();
    }

    @Override
    public int getMaxPosition() {
        return myConfig.getMaxPosition();
    }

    @Override
    public NormalizedDouble getDefaultPosition() {
        return myDefaultPosition;
    }
    
    public final static class Id implements LocalIdentifier{
        public final static int MAX_RS485_ADDR = 254;
        public final static int MAX_I2C_ADDR = 127;
        private int myRS485Addr;
        private int myI2CAddr;
        /**
         * Creates a JointId from the given integer id.
         * @param id the jointId
         */
        public Id(int rs485Addr, int i2cAddr){
            if(!isValidId(rs485Addr, i2cAddr)){
                throw new IllegalArgumentException("PhysicalId out of range.");
            }
            myRS485Addr = rs485Addr;
            myI2CAddr = i2cAddr;
        }
        
        public int getRS485Addr(){
            return myRS485Addr;
        }
        
        public int getI2CAddr(){
            return myI2CAddr;
        }
        
        @Override
        public boolean equals(Object obj){
            if(obj == null || obj.getClass() != this.getClass()){
                return false;
            }
            return (myRS485Addr == ((Id)obj).myRS485Addr)
                    && (myI2CAddr == ((Id)obj).myI2CAddr);
        }
        
        /**
         * Returns true is the id would make a valid Id.
         * @param id value to check
         * @return true is the id would make a valid Id.
         */
        public static boolean isValidId(int rs485Addr, int i2cAddr){
            return (rs485Addr >= 0 && rs485Addr < MAX_RS485_ADDR)
                    && (i2cAddr >= 0 && i2cAddr < MAX_I2C_ADDR);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + this.myRS485Addr;
            hash = 71 * hash + this.myI2CAddr;
            return hash;
        }
        

        @Override
        public String toString() {
            return "" + myRS485Addr + "::" + myI2CAddr;
        }
    }
}
