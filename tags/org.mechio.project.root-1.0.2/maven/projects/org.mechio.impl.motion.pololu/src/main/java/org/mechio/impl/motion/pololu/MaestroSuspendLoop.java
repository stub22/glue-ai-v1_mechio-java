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

import java.util.ArrayList;
import java.util.List;
import org.jflux.api.common.rk.utils.TimeUtils;

/**
 *
 * @author matt
 */
public class MaestroSuspendLoop {
    public final static int DEFAULT_LOOP_INTERVAL = 5000;
    public final static int DEFAULT_TIMEOUT = 5000;
    
    private int myLoopInterval;
    private boolean myRunFlag;
    private List<AutoDisableConfig> myDisableConfigs;
    
    public MaestroSuspendLoop(int loopInterval){
        myDisableConfigs = new ArrayList<AutoDisableConfig>();
        myLoopInterval = Math.max(loopInterval, 1);
        myRunFlag = false;
    }
    
    public void addServo(MaestroServo servo, int timeoutLength){
        if(servo == null){
            return;
        }
        AutoDisableConfig c = new AutoDisableConfig();
        c.myServo = servo;
        c.myDisableTimeoutLength = Math.max(timeoutLength, 1);
        myDisableConfigs.add(c);
    }
    
    public synchronized void startLoop(){
        if(myRunFlag){
            return;
        }
        myRunFlag = true;
        new Thread(new LoopWorker()).start();
    }
    
    public synchronized void stopLoop(){
        myRunFlag = false;
    }
    
    public void checkAndSuspend() {
        for(AutoDisableConfig c : myDisableConfigs){
            long changeTimestamp = c.myServo.getLastGoalChangeTime();
            long elapsed = TimeUtils.now() - changeTimestamp;
            if(elapsed > c.myDisableTimeoutLength){
                c.myServo.setSuspended(true);
            }
        }
    }
    
    class LoopWorker implements Runnable {
        @Override public void run() {
            while(myRunFlag){
                checkAndSuspend();
                TimeUtils.sleep(myLoopInterval);
            }
        }
    }
    
    class AutoDisableConfig {
        public MaestroServo myServo;
        public int myDisableTimeoutLength;
    }
}
