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

package org.mechio.impl.motion.dynamixel;

import org.mechio.impl.motion.dynamixel.enums.DynamixelBaudRate;
import org.mechio.impl.motion.dynamixel.enums.ErrorStatus;
import org.mechio.impl.motion.dynamixel.enums.Register;
import java.util.HashMap;
import java.util.Map;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.common.rk.utils.Utils;
import org.jflux.impl.services.rk.utils.HashCodeUtil;
import org.jflux.impl.services.rk.utils.LocalIdentifier;
import org.mechio.api.motion.servos.AbstractServo;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.impl.motion.dynamixel.feedback.FeedbackUpdateValues;

/**
 * Represents a Dynamixel servo in a Dynamixel chain.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DynamixelServo extends AbstractServo<
        DynamixelServo.Id,
        ServoConfig<DynamixelServo.Id>,
        DynamixelController> {
	private final Map<Register, Integer> myCache;
	private Boolean myIsChanged;
    private DynamixelServo.Id myPhysicalId;
    private int myMinPosition;
    private int myMaxPosition;
    private int myPreviousPosition;
    private NormalizedDouble myDefaultPosition;
    private String myName;
    private FeedbackUpdateValues myFeedbackUpdateVals;

    /**
     * Creates a new DynamixelServo from the given JointConfig and 
     * DynamixelController.
     * @param params JointConfig for the new Joint
     * @param controller DynamixelController for the new Joint
     */
    public DynamixelServo(ServoConfig<DynamixelServo.Id> params, DynamixelController controller){
        super(params, controller);
        myPhysicalId = params.getServoId();
        
        myIsChanged = false;
        myCache = new HashMap();
		int[] vals = myController.readRegisters(getPhysicalId(), Register.CurrentPosition, Register.CurrentTemperature);
        myFeedbackUpdateVals =
                new FeedbackUpdateValues(
                        myPhysicalId, vals, TimeUtils.now());
        myPreviousPosition = vals[0];
		myCache.put(Register.GoalPosition, vals[0]);
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
	}
    
    public final DynamixelServo.Id getPhysicalId(){
        return myPhysicalId;
    }
    
    @Override
    public void setEnabled(Boolean enabled){
        Boolean old = getEnabled();
        setTorqueEnabled(enabled);
        firePropertyChange(PROP_ENABLED, old, enabled);
    }

    @Override
    public Boolean getEnabled(){
        return getTorqueEnable();
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
    
    protected int getPreviousPosition(){
        return myPreviousPosition;
    }
    
    protected void setPreviousPosition(int pos){
        myPreviousPosition = pos;
    }

    @Override
    public NormalizedDouble getDefaultPosition() {
        return myDefaultPosition;
    }

    /**
     * Returns the value at the given register.
     * @param reg register to read
     * @return value at the given register
     */
    public Integer get(Register reg){
		if(!myCache.containsKey(reg))
			return -1;
		return myCache.get(reg);
	}
    /**
     * Sets the value of a given register.
     * @param reg register to set
     * @param value value to set
     */
    protected void put(Register reg, Integer value){
        myCache.put(reg, value);
    }

    @Override
    public void setGoalPosition(NormalizedDouble value){
        super.setGoalPosition(value);
        Integer goal = getAbsoluteGoalPosition();
        if(goal == null){
            return;
        }
        SetRegisterValue(Register.GoalPosition, goal);
    }

    /**
     * Returns the moving speed as a value 1(slow) - 1023(fast), with 0 being
     * full speed.
     * @return moving speed as a value 1(slow) - 1023(fast), with 0 being
     * full speed
     */
    public Integer getMovingSpeed(){
		return GetRegisterValue(Register.MovingSpeed);
	}
    /**
     * Sets the moving speed with a value 1(slow) - 1023(fast), and 0 being
     * full speed.
     * @param value moving speed value; 1(slow) - 1023(fast), and 0 being
     * full speed
     */
    public void setMovingSpeed(Integer value){
		SetRegisterValue(Register.MovingSpeed, value);
	}
    //TODO support multiple Alarm LED ErrorStatuses
    /**
     * Returns the ErrorStatus which triggers the Alarm LED.
     * @return ErrorStatus which triggers the Alarm LED
     */
    public ErrorStatus getAlarmLed(){
		return ErrorStatus.getStatus(GetRegisterValue(Register.AlarmLed));
	}
    /**
     * Sets the ErrorStatus which triggers the Alarm LED.
     * @param value ErrorStatus to triggers the Alarm LED
     */
    public void setAlarmLed(ErrorStatus value){
		SetRegisterValue(Register.AlarmLed, (int)value.getByte());
	}
    //TODO support multiple Alarm Shutdown ErrorStatuses
    /**
     * Returns the ErrorStatus which triggers a servo shutdown.
     * @return ErrorStatus which triggers a servo shutdown
     */
    public ErrorStatus getAlarmShutdown(){
		return ErrorStatus.getStatus(GetRegisterValue(Register.AlarmShutdown));
	}
    /**
     * Sets the ErrorStatus which triggers a servo shutdown.
     * @param value ErrorStatus to triggers a servo shutdown
     */
    public void setAlarmShutdown(ErrorStatus value){
		SetRegisterValue(Register.AlarmShutdown, (int)value.getByte());
	}

    /**
     * Returns the BaudRate expected by the DynamixelServo.
     * @return BaudRate expected by the DynamixelServo
     */
    public DynamixelBaudRate getBaudRate(){
		return DynamixelBaudRate.get((byte)GetRegisterValue(Register.BaudRate).intValue());
	}
    /**
     * Sets the BaudRate expected by the DynamixelServo
     * @param value BaudRate to be expected by the DynamixelServo
     */
    public void setBaudRate(DynamixelBaudRate value){
		SetRegisterValue(Register.BaudRate, (int)value.getByte());
	}

    /**
     *
     * @return
     */
    public Integer getCcwAngleLimit() {
		return GetRegisterValue(Register.CcwAngleLimit);
	}

    /**
     *
     * @param value
     */
    public void setCcwAngleLimit(Integer value) {
		SetRegisterValue(Register.CcwAngleLimit, value);
	}

    /**
     *
     * @return
     */
    public Integer getCwAngleLimit() {
		return GetRegisterValue(Register.CwAngleLimit);
	}

    /**
     *
     * @param value
     */
    public void setCwAngleLimit(Integer value) {
		SetRegisterValue(Register.CwAngleLimit, value);
	}

    /**
     *
     * @return
     */
    public Integer getCcwComplianceMargin() {
		return GetRegisterValue(Register.CcwComplianceMargin);
	}

    /**
     *
     * @param value
     */
    public void setCcwComplianceMargin(Integer value) {
		SetRegisterValue(Register.CcwComplianceMargin, value);
	}

    /**
     *
     * @return
     */
    public Integer getCwComplianceMargin() {
		return GetRegisterValue(Register.CwComplianceMargin);
	}

    /**
     *
     * @param value
     */
    public void setCwComplianceMargin(Integer value) {
		SetRegisterValue(Register.CwComplianceMargin, value);
	}

    /**
     *
     * @return
     */
    public Integer getCcwComplianceSlope() {
		return GetRegisterValue(Register.CcwComplianceSlope);
	}

    /**
     *
     * @param value
     */
    public void setCcwComplianceSlope(Integer value) {
		SetRegisterValue(Register.CcwComplianceSlope, value);
	}

    /**
     *
     * @return
     */
    public Integer getCwComplianceSlope() {
		return GetRegisterValue(Register.CwComplianceSlope);
	}

    /**
     *
     * @param value
     */
    public void setCwComplianceSlope(Integer value) {
		SetRegisterValue(Register.CwComplianceSlope, value);
	}

    /**
     *
     * @return
     */
    public Integer getCurrentLoad() {
		return myFeedbackUpdateVals.getCurrentLoad();
	}

    /**
     *
     * @return
     */
    public NormalizedDouble getCurrentPosition() {
        double min = myConfig.getMinPosition();
        double max = myConfig.getMaxPosition();
        Double range = max - min;
        Double abs = getCurrentPositionAbs() - min;
        Double pos = Utils.bound(abs/range, 0.0, 1.0);
        return new NormalizedDouble(pos);
	}
    /**
     * Returns the absolute position of the Dynamixel between 0-1023
     * @return absolute position of the Dynamixel between 0-1023
     */
    public Integer getCurrentPositionAbs() {
		return myFeedbackUpdateVals.getCurrentPosition();
	}

    /**
     *
     * @return
     */
    public Integer getCurrentSpeed() {
		return myFeedbackUpdateVals.getCurrentSpeed();
	}

    /**
     *
     * @return
     */
    public Integer getCurrentTemperature() {
		return myFeedbackUpdateVals.getCurrentTemperature();
	}

    /**
     *
     * @return
     */
    public Integer getCurrentVoltage() {
		return myFeedbackUpdateVals.getCurrentVoltage();
	}

    /**
     *
     * @return
     */
    public Boolean getTorqueEnable() {
		return GetRegisterValue(Register.TorqueEnable) != 0;
	}

    /**
     *
     * @param value
     */
    public void setTorqueEnabled(Boolean value) {
		SetRegisterValue(Register.TorqueEnable, value ? 1 : 0);
	}

    /**
     *
     * @return
     */
    public Integer getFirmwareVersion() {
		return GetRegisterValue(Register.FirmwareVersion);
	}

     /*public void setPhysicalId(Integer value) throws Throwable {
		if ( ( value < 0 || value >= DynamixelController.BROADCAST_ID ) ) {
			throw new Exception("Value must be in the range 0 to 253");
		}
		if ( value.equals(getPhysicalId()) ) {
			return;
		}
		myController.writeRegister(getPhysicalId(), Register.Id, value, false);
	}*/

    /**
     *
     * @return
     */
    public Boolean getLed() {
		return GetRegisterValue(Register.Led) != 0;
	}

    /**
     *
     * @param value
     */
    public void setLed(Boolean value) {
		SetRegisterValue(Register.Led, value ? 1 : 0);
	}

    /**
     *
     * @return
     */
    public Boolean getLock() {
		return GetRegisterValue(Register.Lock) != 0;
	}

    /**
     *
     * @return
     */
    public Integer getTemperatureLimit() {
		return GetRegisterValue(Register.TemperatureLimit);
	}

    /**
     *
     * @param value
     */
    public void setTemperatureLimit(Integer value) {
		SetRegisterValue(Register.TemperatureLimit, value);
	}

    /**
     *
     * @return
     */
    public Integer getMaxTorque() {
		return GetRegisterValue(Register.MaxTorque);
	}

    /**
     *
     * @param value
     */
    public void setMaxTorque(Integer value) {
		SetRegisterValue(Register.MaxTorque, value);
	}

    /**
     *
     * @return
     */
    public float getHighVoltageLimit() {
		return (float) (GetRegisterValue(Register.HighVoltageLimit)/10.0);
	}

    /**
     *
     * @param value
     */
    public void setHighVoltageLimit(float value) {
		SetRegisterValue(Register.HighVoltageLimit, (int)Math.round(value*10.0));
	}

    /**
     *
     * @return
     */
    public float getLowVoltageLimit() {
		return (float) (GetRegisterValue(Register.LowVoltageLimit)/10.0);
	}

    /**
     *
     * @param value
     */
    public void setLowVoltageLimit(float value) {
		SetRegisterValue(Register.LowVoltageLimit, (int)Math.round(value*10.0));
	}

    /**
     *
     * @return
     */
    public Integer getModelNumber() {
		return GetRegisterValue(Register.ModelNumber);
	}

    /**
     *
     * @return
     */
    public Boolean getMoving() {
		return ( myIsChanged || myController.readRegister(getPhysicalId(), Register.Moving) != 0 );
	}

    /**
     *
     * @return
     */
    public Integer getPunch() {
		return GetRegisterValue(Register.Punch);
	}

    /**
     *
     * @param value
     */
    public void setPunch(Integer value) {
		SetRegisterValue(Register.Punch, value);
	}

    /**
     *
     * @return
     */
    public Boolean getRegisteredInstruction() {
		return myController.readRegister(getPhysicalId(), Register.RegisteredInstruction) != 0;
	}

    /**
     *
     * @param value
     */
    public void setRegisteredInstruction(Boolean value) {
		SetRegisterValue(Register.RegisteredInstruction, value ? 1 : 0);
	}

    /**
     *
     * @return
     */
    public Integer getReturnDelay() {
		return GetRegisterValue(Register.ReturnDelay)*2;
	}

    /**
     *
     * @param value
     */
    public void setReturnDelay(Integer value) {
		SetRegisterValue(Register.ReturnDelay, value/2);
	}

    /**
     *
     * @return
     */
    public Integer getStatusReturnLevel() {
		return (Integer) GetRegisterValue(Register.StatusReturnLevel);
	}

    /**
     *
     * @param value
     */
    public void setStatusReturnLevel(Integer value) {
		SetRegisterValue(Register.StatusReturnLevel, (Integer) value);
	}

    /**
     *
     * @return
     */
    public Integer getTorqueLimit() {
		return GetRegisterValue(Register.TorqueLimit);
	}

    /**
     *
     * @param value
     */
    public void setTorqueLimit(Integer value) {
		SetRegisterValue(Register.TorqueLimit, value);
	}

    /**
     * Resets the DynamixelServo to the default factory settings.
     * WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! 
     * USE THIS METHOD WITH CAUTION.  THIS WILL CAUSE THE JOINT'S
     * PHYSICAL ID AND BAUD RATE TO CHANGE.  
     * This method should not be used with any robot you care about as it will 
     * cause the Joint to stop functioning properly.  This is included only to 
     * provide complete Dynamixel functionality for use in other applications.
     * @return true if successful
     */
    protected synchronized boolean reset(){
//		myController.writeInstruction(getPhysicalId(), Instruction.Reset, null);
//		return !myController.readPacket((byte)0).hasError();
        return false;
	}

    /**
     *
     * @param reg
     * @return
     */
    protected Integer GetRegisterValue(Register reg){
		switch (reg){
			case GoalPosition:
			case MovingSpeed:
				return get(reg);
		}

		if (!reg.isCached()){
			return myController.readRegister(getPhysicalId(), reg);
        }

		Integer v = get(reg);
		if (v == -1){
			put(reg, myController.readRegister(getPhysicalId(), reg));
			v = get(reg);
		}
		return v;
	}

    /**
     * Sets the value for the given Register.
     * @param reg Register to set
     * @param value value to set
     */
    protected synchronized void SetRegisterValue(Register reg, Integer value){
		if(reg.isSynchronized()){
            put(reg, value);
            myIsChanged = true;
            return;
		}else if (!reg.isCached()){
			myController.writeRegister(getPhysicalId(), reg, value, false);
			return;
		}
		Integer v = get(reg);
		if (v == value)
			return;

		myController.writeRegister(getPhysicalId(), reg, value, false);
		put(reg, value);
	}

    /**
     * Reads all Registers for the DynamixelServo and updates the cache.
     */
    public synchronized void cacheValues(){
		Register[] regs = Register.values();
		int[] values = myController.readRegisters(getPhysicalId(), Register.ModelNumber, Register.Punch);
		for (Integer i = 0; i < regs.length; i++){
			put(regs[i], values[i]);
		}
	}
    
    //TODO fix Dynamixel register caching updates.
    /**
     * Returns true if Register values have been changed.
     * @return true if Register values have been changed
     */
    public boolean changed(){
        return myIsChanged;
    }
    
    public final static class Id implements LocalIdentifier{
        private final static int theIdCount = 254;
        private int myPhysicalJointId;        
        
        /**
         * Creates the Broadcast Id
         */
        Id(){
            myPhysicalJointId = theIdCount;
        }
        /**
         * Creates a JointId from the given integer id.
         * @param id the jointId
         */
        public Id(int id){
            if(!isValidId(id)){
                throw new IllegalArgumentException("PhysicalId out of range.");
            }
            myPhysicalJointId = id;
        }
        
        /**
         * Returns the integer value of the Id.
         * @return the integer value of the Id
         */
        final public int getIntValue(){
            return myPhysicalJointId;
        }
        
        @Override
        public boolean equals(Object obj){
            if(obj == null || obj.getClass() != this.getClass()){
                return false;
            }
            return myPhysicalJointId == 
                    ((Id)obj).myPhysicalJointId;
        }
        
        /**
         * Returns true is the id would make a valid Id.
         * @param id value to check
         * @return true is the id would make a valid Id.
         */
        public static boolean isValidId(int id){
            return id >= 0 && id < theIdCount;
        }
        
        @Override
        public int hashCode() {
            return HashCodeUtil.hash(HashCodeUtil.SEED, myPhysicalJointId);
        }

        @Override
        public String toString() {
            return "" + myPhysicalJointId;
        }
    }
    
    public void setFeedbackVals(FeedbackUpdateValues vals){
        if(vals == null){
            return;
        }
        myFeedbackUpdateVals = vals;
    }
}