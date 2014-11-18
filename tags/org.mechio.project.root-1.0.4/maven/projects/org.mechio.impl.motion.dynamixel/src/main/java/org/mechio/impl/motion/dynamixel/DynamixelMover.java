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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.common.rk.utils.Utils;
import org.mechio.api.motion.servos.ServoController.ServoId;
import org.mechio.impl.motion.dynamixel.enums.Instruction;
import org.mechio.impl.motion.dynamixel.enums.Register;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlSettings;
import org.mechio.impl.motion.dynamixel.feedback.MoveParams;
import org.mechio.impl.motion.rxtx.serial.RXTXSerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DynamixelMover {
    private final static Logger theLogger = LoggerFactory.getLogger(DynamixelMover.class);
    /**
     * Number of bytes sent to each DynamixelServo when moving multiple Servos.
     */
    private final static int theSyncCount = 5;
    private final static double theRXMaxPosition = 1023.0;
    private final static double theRXRotationRange = 300.0;
    private final static double theRXRPMConversion = 0.111;
    
    private final static double theMXMaxPosition = 4095.0;
    private final static double theMXRotationRange = 360.0;
    private final static double theMXRPMConversion = 0.114;
    
    public static boolean moveServos(
            DynamixelController controller, Collection<MoveParams<DynamixelServo.Id>> params,
            DynamixelControlSettings settings){
        if(controller == null || params == null){
            throw new NullPointerException();
        }
        RXTXSerialPort myPort = controller.getPort();
        if(myPort == null){
            return false;
        }
        List<MoveParams<DynamixelServo.Id>> enabled = new ArrayList();
        for(MoveParams<DynamixelServo.Id> p : params){
            if(p == null){
                continue;
            }
            DynamixelServo.Id id = p.getServoId();
            ServoId<DynamixelServo.Id> sId =
                    new ServoId<DynamixelServo.Id>(controller.getId(), id);
            DynamixelServo servo = controller.getServo(sId);
            if(servo == null || !servo.getEnabled()){
                continue;
            }
            enabled.add(p);
        }
        byte[] cmd = buildMoveCommand(enabled, settings);
		if(!myPort.write(cmd) || !myPort.flushWriter()){
            return false;
        }
        for(MoveParams p : enabled){
            p.goalsSent();
        }
        return true;
    }
    
    public static byte[] buildMoveCommand(Collection<MoveParams<DynamixelServo.Id>> params,
            DynamixelControlSettings settings){
        int jointCount = params.size();
        int paramLen = jointCount*theSyncCount;
        int dataSize = paramLen + theMoveCount;
        byte[] cmd = new byte[dataSize];
        addMoveCommand(params, cmd, 0, settings);
        return cmd;
    }
    
    private final static int theMoveCount = 8;
    private final static byte[] theHeaderData = new byte[]{
        (byte)0xff,
        (byte)0xff,
        (byte)DynamixelController.BROADCAST_ID.getIntValue(),
        0,
        Instruction.SyncWrite.getByte(),
        Register.GoalPosition.getByte(),
        (byte)theSyncCount-1
    };
    
    private static void addMoveCommand(
            Collection<MoveParams<DynamixelServo.Id>> params, byte[] cmd, int offset,
            DynamixelControlSettings settings){
        int jointCount = params.size();
        int paramLen = jointCount*theSyncCount;
        int dataSize = paramLen + theMoveCount;
        if(cmd.length < offset + dataSize){
			throw new IllegalArgumentException(
                    "cmd byte array too short.  expected: " 
                    + (offset+dataSize) + ", found: " + cmd.length);
        }
        System.arraycopy(theHeaderData, 0, cmd, offset, theHeaderData.length);
        cmd[offset+3] = (byte)(paramLen+4);
        addServosParams(params, cmd, offset+theHeaderData.length, settings);
		cmd[offset+dataSize - 1] = Utils.checksum(cmd, offset+2, paramLen+5, true);
    }
    
    private static void addServosParams(
            Collection<MoveParams<DynamixelServo.Id>> params, byte[] cmd, int offset,
            DynamixelControlSettings settings){
        long now = TimeUtils.now();
        for(MoveParams<DynamixelServo.Id> p : params){
            int speed = calculateSpeed(p, now, settings);
            cmd[offset] = (byte)p.getServoId().getIntValue();
            cmd[offset+1] = (byte)(p.getGoalPosition() & 0xff);
            cmd[offset+2] = (byte)(p.getGoalPosition() >> 8);
            cmd[offset+3] = (byte)(speed & 0xff);
            cmd[offset+4] = (byte)(speed >> 8);
            offset += theSyncCount;
        }
        if(theLogger.isTraceEnabled()){
            for(MoveParams<DynamixelServo.Id> p : params){
                logMoveParam(p);
            }
        }
    }
    private static int calculateSpeed(MoveParams params, long now,
            DynamixelControlSettings settings){
        int cur = getPositionEstimate(params, now, settings);
        long time = params.getGoalTargetTimeUTC() - now;
        time -= settings.getCommandSendDelay();
        return calculateSpeed(cur, params.getGoalPosition(), time);
    }
    
    /**
     * Estimated the position at the time the command reaches the servo
     * @param params
     * @return 
     */
    public static int getPositionEstimate(MoveParams params, long now,
            DynamixelControlSettings settings){
        long time = now - params.getCurPosTimestampUTC();
        //time += params.getCommandDelayMillisec();
        int cur = params.getCurrentPosition()
                + dist(params.getCurrentSpeed(), time);
//        if(cur != params.getCurrentPosition()){
//            System.out.println(
//                    "time: " + time + 
//                    ", prev:" + params.getCurrentPosition() + 
//                    ", est: " + cur + 
//                    ", speed: " + params.getCurrentSpeed());
//        }
        if(params.getCurrentPosition() < params.getPrevGoalPosition()){
            cur = Math.min(cur, params.getPrevGoalPosition());
        }else{
            cur = Math.max(cur, params.getPrevGoalPosition());
        }
        return cur;
    }
    
    //TODO: take voltage into account and find the max speed
    private static int calculateSpeed(int cur, int goal, long time){
        //time is in milliseconds
        //cur and goal are dynamixel values, [0, 1023]
        int dist = Math.abs(goal - cur);
        //dynamixel range of motion is 300 degrees
        double theta = dist*theRXRotationRange/theRXMaxPosition;
        time = Math.max(time, 1);
        
        //60000ms/minute, 360 deg/rotation
        double rpm = theta * (60000.0/time) / 360.0; 
        double speed = rpm/theRXRPMConversion;
        //make sure the speed is between 1 and 1023
        //0 is max speed, so minimum is 1
        return Utils.bound((int)speed, 1, 1023);
    }
    
    //TODO: take voltage into account and find the max speed
    private static int dist(int speed, long time){
        speed = Utils.bound(speed, -1023, 1023);
        time = Math.max(time, 1);
        double rpm = (double)speed*theRXRPMConversion;
        double theta = (rpm*360.0)/(60000.0/time);
        double dist = (theta/theRXRotationRange)*theRXMaxPosition;
        return (int) dist;
    }
    
    private static void logMoveParam(MoveParams<DynamixelServo.Id> param){
        theLogger.trace("{},{},{},{},{},{},{},{},{},{},{},{},{}", 
                new Object[]{
                        param.getServoId(),
                        param.getGoalPosition(),
                        param.getGoalTargetTimeUTC(),
                        param.getCurrentPosition(),
                        param.getCurrentSpeed(),
                        param.getCurrentVoltage(),
                        param.getCurrentTemperature(),
                        param.getCurrentLoad(),
                        param.getCurPosTimestampUTC(),
                        param.getPrevGoalPosition(),
                        param.getPrevGoalTargetTimeUTC(),
                        param.getCommandDelayMillisec(),
                        TimeUtils.now()});
    }
}
