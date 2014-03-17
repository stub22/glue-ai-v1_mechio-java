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

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Listener;
import org.mechio.api.motion.servos.ServoController.ServoId;
import org.mechio.impl.motion.dynamixel.DynamixelController;
import org.mechio.impl.motion.dynamixel.DynamixelMover;
import org.mechio.impl.motion.dynamixel.DynamixelMultiReader;
import org.mechio.impl.motion.dynamixel.DynamixelPacket;
import org.mechio.impl.motion.dynamixel.DynamixelServo;
import org.mechio.impl.motion.openservo.feedback.OpenServoControlLoop;
import org.mechio.impl.motion.rxtx.serial.RXTXSerialPort;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DynamixelControlLoop {
    private final static Logger theLogger = Logger.getLogger(DynamixelControlLoop.class.getName());
    private ConcurrentDynamixelCache myCache;
    private DynamixelController myController;
    private List<DynamixelServo.Id> myServoIds;
    private Queue<DynamixelCommand> myCommandQueue;
    private DynamixelControlSettings mySettings;
    private int myReadIndex;
    private boolean myReadOSFlag;
    private boolean myRunFlag;
    private boolean myCooldownFlag;
    private TemperatureMonitor myTemperatureMonitor;
    private OpenServoControlLoop myOSLoop;
    
    public DynamixelControlLoop(
            DynamixelController controller, 
            DynamixelControlSettings settings){
        if(controller == null ||settings == null){
            throw new NullPointerException();
        }
        mySettings = settings;
        myController = controller;
        myCache = new ConcurrentDynamixelCache();
        myReadIndex = 0;
        myRunFlag = false;
        myCooldownFlag = false;
        myReadOSFlag = false;
        myCommandQueue = new ConcurrentLinkedQueue<DynamixelCommand>();
        myTemperatureMonitor = 
                new TemperatureMonitor(myController, mySettings, myCache);
    }
    
    public void setOpenServoLoop(OpenServoControlLoop loop){
        myOSLoop = loop;
    }
    
    public DynamixelControlSettings getSettings(){
        return mySettings;
    }
    
    public void setGoalPositions(Collection<GoalUpdateValues<DynamixelServo.Id>> goals){
        myCache.setGoalPositions(goals);
    }
    
    public void start(List<DynamixelServo.Id> ids){
        if(myRunFlag || ids == null){
            return;
        }
        mySettings.setRunFlag(true);
        myRunFlag = true;
        myServoIds = ids;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    commandLoop();
                }catch(Throwable t){
                }
            }
        }).start();
    }
    
    public void stop(){
        myRunFlag = false;
        mySettings.setRunFlag(false);
    }
        
    private void commandLoop(){
        while(myRunFlag){
            try{
                if(!mySettings.getRunFlag()){
                    TimeUtils.sleep(10);
                    continue;
                }
                if(cooldown()){
                    if(mySettings.getCommandFlag()){
                        commandBoth();
                    }
                    update();
                }else if(mySettings.getMoveFlag() && 
                        (myCache.getMoveFlag() || 
                                (myOSLoop != null && myOSLoop.getMoveFlag()))){
                    if(myCache.getMoveFlag()){
                        move();
                    }
                    if(myOSLoop != null && myOSLoop.getMoveFlag()){
                        myOSLoop.move();
                    }
                }else if(mySettings.getCommandFlag()
                        && commandBoth()){
                }else if(mySettings.getUpdateFlag()){
                    update();
                }
            }catch(Throwable t){
                theLogger.log(Level.WARNING, 
                        "Recovering from error in Dynamixel control loop: ", t);
                TimeUtils.sleep(5);
            }
        }
    }
    
    private synchronized void move(){
        Collection<MoveParams<DynamixelServo.Id>> params = 
                myCache.acquireMoveParams();
        try{
            if(params == null || params.isEmpty()){
                return;
            }
            if(!DynamixelMover.moveServos(myController, params, mySettings)){
                theLogger.warning("There was an error moving the dynamixels.");
                myController.getPort().clearErrors();
            }else{
                myCache.setMoveFlag(false);
            }
        }finally{
            myCache.releaseMoveParams();
        }
    }
    
    private boolean cooldown(){
        myTemperatureMonitor.disableHotServos(
                (int)mySettings.getMaxRunTemperature());
        return false;
    }
    
    private synchronized void update(){
        if(myReadOSFlag){
            if(myOSLoop == null){
                myReadOSFlag = false;
            }else{
                myReadOSFlag = myOSLoop.update();
            }
            return;
        }
        int from = myReadIndex;
        int to = Math.min(
                myReadIndex + mySettings.getReadCount(), 
                myServoIds.size());
        List<DynamixelServo.Id> ids = myServoIds.subList(from, to);
        List<FeedbackUpdateValues> feedback = 
                DynamixelMultiReader.getFeedback(myController, ids);
        if(feedback == null){
            clearControllerErrors();
            return;
        }else if(feedback.size() < ids.size()){
            clearControllerErrors();
        }
        myCache.addFeedbackValues(feedback);
        myReadIndex += mySettings.getReadCount();
        if(myReadIndex >= myServoIds.size()){
            myReadIndex = 0;
            myReadOSFlag = true;
        }
        updateServoValues(feedback);
    }
    
    private void clearControllerErrors(){
            TimeUtils.sleep(1);
            myController.getPort().clearErrors();
            myController.getPort().clearReader();
    }
    
    private void updateServoValues(List<FeedbackUpdateValues> feedbackVals){
        for(FeedbackUpdateValues val : feedbackVals){
            if(val == null || val.getCurrentTemperature() == 0
                    || val.getCurrentVoltage() == 0){
                continue;
            }
            ServoId<DynamixelServo.Id> id = 
                    new ServoId<DynamixelServo.Id>(
                            myController.getId(), val.getServoId());
            DynamixelServo servo = myController.getServo(id);
            if(servo != null){
                servo.setFeedbackVals(val);
            }
        }
    }
    
    public void queueCommand(DynamixelCommand cmd){
        if(cmd == null){
            return;
        }
        myCommandQueue.add(cmd);
        if(!myRunFlag){
            command();
        }
    }
    
    public synchronized boolean commandBoth(){
        boolean ret = command();
        if(myOSLoop != null){
            ret = ret || myOSLoop.command();
        }
        return ret;
    }
    public synchronized boolean command(){
        if(myCommandQueue.isEmpty()){
            return false;
        }
        DynamixelCommand cmd = myCommandQueue.poll();
        if(cmd == null){
            return false;
        }
        RXTXSerialPort port = myController.getPort();
        if(port == null){
            cmd.myPacketCallback.handleEvent(null);
            return false;
        }
        boolean write = port.write(cmd.myCommandBytes) && port.flushWriter();
        if(!write){
            cmd.myPacketCallback.handleEvent(null);
            return false;
        }
        if(cmd.myPacketCount > 0){
            if(!read(cmd.myPacketCount, 
                    cmd.myPacketDataSize, 
                    cmd.myPacketCallback)){
                return false;
            }
        }
        return true;
    }
    
    private boolean read(int i, byte packetSize, 
            final Listener<DynamixelPacket[]> callback){
        final DynamixelPacket[] packets = 
                DynamixelMultiReader.readPackets(myController, i, packetSize);
        if(callback != null){
            callback.handleEvent(packets);
        }
        if(packets == null || packets.length != i){
            return false;
        }
        return true;
    }
    
    public static class DynamixelCommand{
        public byte[] myCommandBytes;
        public int myPacketCount;
        public byte myPacketDataSize;
        public PacketCallback myPacketCallback;
        
        public DynamixelCommand(
                byte[] cmdBytes, 
                int packetCount, 
                byte packetDataSize, 
                PacketCallback packetCallback){
            myCommandBytes = cmdBytes;
            myPacketCount = packetCount;
            myPacketDataSize = packetDataSize;
            myPacketCallback = packetCallback;
        }
        
        public int getPacketReturnCount(){
            return myPacketCount;
        }
        
        public PacketCallback getCallback(){
            return myPacketCallback;
        }
    }
    
    public static class PacketCallback implements Listener<DynamixelPacket[]>{
        private DynamixelPacket[] myPackets;
        private long myStartTime;
        private boolean myReceivedFlag;

        public PacketCallback() {
            myReceivedFlag = false;
        }
        
        @Override
        public void handleEvent(DynamixelPacket[] packets){
            myPackets = packets;
            myReceivedFlag = true;
        }
        
        public DynamixelPacket[] waitForPackets(long timeout){
            myStartTime = TimeUtils.now();
            while(!myReceivedFlag){
                if(TimeUtils.now() >= myStartTime+timeout){
                    return null;
                }
                TimeUtils.sleep(1);
            }
            return myPackets;
        }
        
        public boolean packetsReceived(){
            return myReceivedFlag;
        }
    }
}
