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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.mechio.api.motion.servos.AbstractServoController;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.api.motion.servos.utils.ConnectionStatus;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlLoop.DynamixelCommand;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlLoop.PacketCallback;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlSettings;
import org.mechio.impl.motion.dynamixel.feedback.GoalUpdateValues;
import org.mechio.impl.motion.openservo.OpenServoCommandSet.Command;
import org.mechio.impl.motion.openservo.OpenServoCommandSet.Register;
import org.mechio.impl.motion.openservo.feedback.ConcurrentOpenServoCache;
import org.mechio.impl.motion.openservo.feedback.DynamixelTracker;
import org.mechio.impl.motion.openservo.feedback.OpenServoControlLoop;
import org.mechio.impl.motion.openservo.utils.OpenServoControllerConfig;

/**
 *
 * @author matt
 */
public class OpenServoController extends AbstractServoController<
        OpenServo.Id, 
        ServoConfig<OpenServo.Id>,
        OpenServo,
        OpenServoControllerConfig> {
    private final static Logger theLogger = Logger.getLogger(OpenServoController.class.getName());
    /**
     * Controller type version name.
     */
    public final static String VERSION_NAME = "OpenServo";
    /**
     * Controller type version number.
     */
    public final static String VERSION_NUMBER = "1.0";
    /**
     * Controller type VersionProperty.
     */
    public final static VersionProperty VERSION = new VersionProperty(VERSION_NAME, VERSION_NUMBER);
    private boolean myEnabledFlag;
    private ConcurrentOpenServoCache myCache;
    private OpenServoControlLoop myControlLoop;
    
    public OpenServoController(OpenServoControllerConfig config){
        super(config);
        myEnabledFlag = true;
        DynamixelControlSettings settings = 
                new DynamixelControlSettings(1, 0, 77, 69, 0, 100);
        myCache = new ConcurrentOpenServoCache();
        myControlLoop = new OpenServoControlLoop(this, settings);
        myControlLoop.setPortSource(new DynamixelTracker(myControlLoop));
        setServos();
    }
    
    public ConcurrentOpenServoCache getCache(){
        return myCache;
    }
    
    private synchronized boolean setServos(){
        myServos.clear();
        myServoMap.clear();
        for(ServoConfig<OpenServo.Id> param : myConfig.getServoConfigs().values()){
            OpenServo servo = new OpenServo(param, this);
            myServos.add(servo);
            OpenServo.Id sId = servo.getId();
            ServoId<OpenServo.Id> servoId = 
                    new ServoId<OpenServo.Id>(getId(), sId);
            myServoMap.put(servoId, servo);
            initServo(servo);
        }
        return true;        
    }
    
    private void initServo(OpenServo servo){
        servo.setEnabled(myEnabledFlag);
        NormalizedDouble def = servo.getDefaultPosition();
        servo.setGoalPosition(def);
    }

    @Override
    protected OpenServo connectServo(ServoConfig<OpenServo.Id> config) {
        return new OpenServo(config, this);
    }

    @Override
    protected boolean disconnectServo(ServoId<OpenServo.Id> id) {
        return true;
    }

    @Override
    public boolean connect() {
        if(ConnectionStatus.DISCONNECTED != myConnectionStatus){
            theLogger.log(Level.WARNING, "Error: Port must be disconnected before connecting.");
            return false;
        }
        List<OpenServo.Id> ids = new ArrayList<OpenServo.Id>(myServos.size());
        for(OpenServo s : myServos){
            OpenServo.Id id = s.getId();
            ids.add(id);
        }
        myControlLoop.start(ids);
//        for(OpenServo s : myServos){
//            configureServo(s.getId(), 2000);
//        }
        setConnectStatus(ConnectionStatus.CONNECTED);
        return true;
    }

    @Override
    public boolean disconnect() {
        setConnectStatus(ConnectionStatus.DISCONNECTED);
        return true;
    }

    @Override
    public boolean moveServo(ServoId<OpenServo.Id> id, long lenMillisec) {
        OpenServo servo = myServoMap.get(id);
        if(servo == null){
            return true;
        }
        long goalTime = TimeUtils.now() + lenMillisec;
        Integer goalVal = servo.getAbsoluteGoalPosition();
        if(goalVal == null){
            return true;
        }
        GoalUpdateValues<OpenServo.Id> goal = 
                new GoalUpdateValues(id.getServoId(), goalVal, goalTime);
        myCache.setGoalPositions(Arrays.asList(goal));
        return true;
    }

    @Override
    public boolean moveServos(ServoId<OpenServo.Id>[] ids, int len, int offset, long lenMillisec) {
        boolean ret = true;
        for(int i=offset; i<offset+len; i++){
            ret = moveServo(ids[i], lenMillisec) && ret;
        }
        return ret;
    }

    @Override
    public boolean moveAllServos(long lenMillisec) {
        ServoId[] ids = myServoMap.keySet().toArray(new ServoId[0]);
        return moveServos(ids, ids.length, 0, lenMillisec);
    }

    @Override
    public List<String> getErrorMessages() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        myEnabledFlag = enabled;
    }

    @Override
    public Boolean getEnabled() {
        return myEnabledFlag;
    }

    @Override
    public Class<OpenServo.Id> getServoIdClass() {
        return OpenServo.Id.class;
    }
    
    public void configureServo(OpenServo.Id id, int pGain){
        sendCommands(id, Command.WRITE_ENABLE);
        writeRegisters(id, Register.PULSE_CONTROL_ENABLED, (byte)0);
        writeRegisterWords(id, Register.PID_PGAIN_HI, pGain);
        sendCommands(id, Command.REGISTERS_SAVE);
    }
    
    public void enableServo(OpenServo.Id id){
        sendCommands(id, Command.PWM_ENABLE);
    }
    
    public void disableServo(OpenServo.Id id){
        sendCommands(id, Command.PWM_DISABLE);
    }
    
    public void sendCommands(
            OpenServo.Id id, OpenServoCommandSet.Command...cmds){
        byte rs485Addr = (byte)id.getRS485Addr();
        byte i2cAddr = (byte)id.getI2CAddr();
        byte[] bytes = 
                OpenServoCommandSet.sendCommands(rs485Addr, i2cAddr, cmds);
        DynamixelCommand dcmd = new DynamixelCommand(bytes, 0, (byte)0, new PacketCallback());
        myControlLoop.queueCommand(dcmd);
    }
    
    public void writeRegisters(
            OpenServo.Id id, OpenServoCommandSet.Register firstRegister, byte...data){
        byte rs485Addr = (byte)id.getRS485Addr();
        byte i2cAddr = (byte)id.getI2CAddr();
        byte[] bytes = 
                OpenServoCommandSet.writeRegisters(rs485Addr, i2cAddr, firstRegister, data);
        DynamixelCommand dcmd = new DynamixelCommand(bytes, 0, (byte)0, new PacketCallback());
        myControlLoop.queueCommand(dcmd);
    }
    
    public void writeRegisterWords(
            OpenServo.Id id, OpenServoCommandSet.Register firstRegister, int...data){
        byte[] byteData = new byte[data.length*2];
        for(int i=0; i<data.length; i++){
            int val = data[i];
            byte hi = (byte)((val >> 8) & 0xFF);
            byte lo = (byte)(val & 0xFF);
            byteData[i*2] = hi;
            byteData[i*2+1] = lo;
        }
        byte rs485Addr = (byte)id.getRS485Addr();
        byte i2cAddr = (byte)id.getI2CAddr();
        byte[] bytes = 
                OpenServoCommandSet.writeRegisters(rs485Addr, i2cAddr, firstRegister, byteData);
        DynamixelCommand dcmd = new DynamixelCommand(bytes, 0, (byte)0, new PacketCallback());
        myControlLoop.queueCommand(dcmd);
    }
    
}
