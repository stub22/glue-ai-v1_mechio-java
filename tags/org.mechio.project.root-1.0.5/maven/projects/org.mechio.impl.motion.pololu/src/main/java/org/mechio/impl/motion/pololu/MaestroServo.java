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

package org.mechio.impl.motion.pololu;

import org.jflux.api.common.rk.position.BooleanRange;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.impl.services.rk.utils.HashCodeUtil;
import org.jflux.impl.services.rk.utils.LocalIdentifier;
import org.mechio.api.motion.joint_properties.EnableMovement;
import org.mechio.api.motion.joint_properties.EnableTorque;
import org.mechio.api.motion.joint_properties.ReadCurrentPosition;
import org.mechio.api.motion.servos.AbstractServo;
import org.mechio.api.motion.servos.config.ServoConfig;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class MaestroServo extends 
        AbstractServo<
                MaestroServo.Id,
                ServoConfig<MaestroServo.Id>,
                MaestroController>{
    private MaestroServo.Id myPhysicalId;
    private Boolean myEnabledFlag;
    private int myMinPosition;
    private int myMaxPosition;
    private NormalizedDouble myDefaultPosition;
    private String myName;
    private boolean mySuspendedFlag;
    private long myLastGoalChangeTimestamp;
    /**
     * Creates a new MaestroServo from the given ServoConfig and controller.
     * @param params ServoConfig for the new Servo
     * @param controller the Servo's controller
     */
    protected MaestroServo(
            ServoConfig<MaestroServo.Id> params, MaestroController controller){
        super(params,controller);
        myEnabledFlag = true;
        myPhysicalId = myConfig.getServoId();
        myMinPosition = params.getMinPosition();
        myMaxPosition = params.getMaxPosition();
        double defInt = params.getDefaultPosition() - myMinPosition;
        double range = myMaxPosition - myMinPosition;
        double val = defInt/range;
        if(!NormalizedDouble.isValid(val)){
            throw new IllegalArgumentException("Default Position invalid: " + 
                    params.getDefaultPosition());
        }
        myDefaultPosition = new NormalizedDouble(val);
        myName = params.getName();
        myGoalPosition = myDefaultPosition;
        myLastGoalChangeTimestamp = TimeUtils.now();
        mySuspendedFlag = false;
    }
    
    public MaestroServo.Id getPhysicalId(){
        return myPhysicalId;
    }

    @Override
    public void setGoalPosition(NormalizedDouble pos) {
        NormalizedDouble oldPos = myGoalPosition;
        super.setGoalPosition(pos);
        if(pos != oldPos){
            myLastGoalChangeTimestamp = TimeUtils.now();
            setSuspended(false);
        }
    }
    
    public long getLastGoalChangeTime(){
        return myLastGoalChangeTimestamp;
    }
    
    public synchronized void setSuspended(boolean val){
        if(mySuspendedFlag == val){
            return;
        }
        mySuspendedFlag = val;
        if(val){
            myController.disableServoPWM(getPhysicalId());
        }else if(!getEnabled()){
            myController.enableServoPWM(getPhysicalId());
        }
    }

    @Override
    public void setEnabled(Boolean enabled){
        Boolean old = myEnabledFlag;
        myEnabledFlag = enabled;
        if(enabled){
            myController.enableServoPWM(getPhysicalId());
        }else{
            myController.disableServoPWM(getPhysicalId());
        }
        firePropertyChange(PROP_ENABLED, old, enabled);
    }

    @Override
    public Boolean getEnabled(){
        return myEnabledFlag;
    }

    @Override
    public String getName() {
        return myName;
    }

    @Override
    public int getMinPosition() {
        return myMinPosition;
    }

    @Override
    public int getMaxPosition() {
        return myMaxPosition;
    }

    @Override
    public NormalizedDouble getDefaultPosition() {
        return myDefaultPosition;
    }
    
    public final static class Id implements LocalIdentifier, Comparable{
        private final static int theIdCount = 12;
        private byte myServoNumber;        
        /**
         * Creates a MaestroServo.Id from the given servo number.
         * @param num the servo number
         */
        public Id(byte num){
            if(!isValidId(num)){
                throw new IllegalArgumentException("PhysicalId out of range.");
            }
            myServoNumber = num;
        }
        
        /**
         * Returns the byte value of the Id.
         * @return the byte value of the Id
         */
        final byte getServoNumber(){
            return myServoNumber;
        }
        
        @Override
        public boolean equals(Object obj){
            if(obj == null || obj.getClass() != this.getClass()){
                return false;
            }
            return myServoNumber == 
                    ((Id)obj).myServoNumber;
        }
        
        boolean isConsecutive(Id id){
            return id != null 
                    && (myServoNumber+1) == id.myServoNumber;
        }
        
        Id getOffsetId(byte offset){
            int id = myServoNumber + offset;
            if(!isValidId(id)){
                return null;
            }
            return new Id((byte)id);
        }
        
        /**
         * Returns true if the given id is valid for a PhysicalId.
         * @param id value to check
         * @return true if the given id is valid for a PhysicalId.
         */
        public static boolean isValidId(int id){
            return id >= 0 && id < theIdCount;
        }
        
        @Override
        public int hashCode() {
            return HashCodeUtil.hash(HashCodeUtil.SEED, myServoNumber);
        }

        @Override
        public String toString() {
            return "" + myServoNumber;
        }
        
        @Override
        public int compareTo(Object o) {
            if(o == null || !MaestroServo.Id.class.isAssignableFrom(o.getClass())){
                return 1;
            }
            Byte a = myServoNumber;
            Byte b = ((MaestroServo.Id)o).myServoNumber;
            int compare = a.compareTo(b);

            if(compare == 0 && o.getClass() != this.getClass()){
                return 1;
            }
            return compare;
        }
    }
    
    class MaestroCurrentPosition extends ReadCurrentPosition {
        @Override
        public NormalizedDouble getValue() {
            return getGoalPosition();
        }

        @Override
        public NormalizableRange<NormalizedDouble> getNormalizableRange() {
            return NormalizableRange.NORMALIZED_RANGE;
        }
    }
    
    class MaestroEnabled extends EnableMovement {
        @Override
        public Boolean getValue() {
            return getEnabled();
        }
        @Override
        public void setValue(Boolean val) {
            setEnabled(val);
        }

        @Override
        public NormalizableRange<Boolean> getNormalizableRange() {
            return BooleanRange.DEFAULT_RANGE;
        }

        @Override
        public boolean getWriteable() {
            return true;
        }
    }
    
    class MaestroTorque extends EnableTorque {
        @Override
        public Boolean getValue() {
            return getEnabled();
        }
        @Override
        public void setValue(Boolean val) {
            setEnabled(val);
        }

        @Override
        public NormalizableRange<Boolean> getNormalizableRange() {
            return BooleanRange.DEFAULT_RANGE;
        }

        @Override
        public boolean getWriteable() {
            return true;
        }
    }
}
