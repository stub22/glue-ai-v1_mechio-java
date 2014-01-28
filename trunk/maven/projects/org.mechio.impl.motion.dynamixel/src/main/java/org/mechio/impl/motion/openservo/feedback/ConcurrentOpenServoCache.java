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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.mechio.impl.motion.dynamixel.feedback.GoalUpdateValues;
import org.mechio.impl.motion.dynamixel.feedback.MoveParams;
import org.mechio.impl.motion.openservo.OpenServo;

/**
 * Provides concurrency for reading and updating values used by the 
 * DynamixelControlLoop.  This will accept new values, both dynamixel feedback 
 * values and new goal positions, merging them in a thread-safe manner.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ConcurrentOpenServoCache {
    private Map<OpenServo.Id,OpenServoCache> myValueMap;
    private Map<OpenServo.Id,OpenServoFeedbackValues> myFeedbackQueue;
    private Map<OpenServo.Id,GoalUpdateValues<OpenServo.Id>> myGoalParamQueue;
    private Lock myValueLock;
    private boolean myMoveFlag;
    
    public ConcurrentOpenServoCache(){
        myValueMap = new HashMap<OpenServo.Id, OpenServoCache>();
        myFeedbackQueue = new HashMap<OpenServo.Id, OpenServoFeedbackValues>();
        myGoalParamQueue = new HashMap<OpenServo.Id, GoalUpdateValues<OpenServo.Id>>();
        myValueLock = new ReentrantLock();
        myMoveFlag = false;
    }
    
    /**
     * Locks the cache and returns the MoveParams.
     * releaseMoveParams() must be called when done using the params.
     * @return 
     */
    public synchronized Collection<MoveParams<OpenServo.Id>> acquireMoveParams(){
        myValueLock.lock();
        return (Collection)myValueMap.values();
    }
    
    public synchronized void releaseMoveParams(){
        try{
            mergeFeedback(myFeedbackQueue.values());
            mergeGoals(myGoalParamQueue.values());
            myFeedbackQueue.clear();
            myGoalParamQueue.clear();
        }finally{
            myValueLock.unlock();
        }
    }
    
    /**
     * Attempts to merge the given values into the MoveParam cache.
     * If the params are in use and locked, the values are queued to be merged
     * when unlocked.
     */
    public synchronized void addFeedbackValues(Collection<OpenServoFeedbackValues> values){
        if(myValueLock.tryLock()){
            try{
                mergeFeedback(values);
            }finally{
                myValueLock.unlock();
            }
        }else{
            queueFeedback(values);
        }
    }
    
    public synchronized void setGoalPositions(Collection<GoalUpdateValues<OpenServo.Id>> goals){
        if(myValueLock.tryLock()){
            try{
                mergeGoals(goals);
            }finally{
                myValueLock.unlock();
            }
        }else{
            queueGoals(goals);
        }
    }
    
    private void queueFeedback(Collection<OpenServoFeedbackValues> vals){
        for(OpenServoFeedbackValues val : vals){
            if(val.getCurrentVoltage() == 0){
                continue;
            }
            myFeedbackQueue.put(val.getServoId(), val);
        }
    }
    
    private void mergeFeedback(Collection<OpenServoFeedbackValues> vals){
        for(OpenServoFeedbackValues val : vals){
            OpenServo.Id id = val.getServoId();
            OpenServoCache cache = myValueMap.get(id);
            if(cache == null){
                cache = new OpenServoCache(id);
                cache.setGoalVals(new GoalUpdateValues(id, -1, 0));
                cache.goalsSent();
                myValueMap.put(id, cache);
            }
            cache.setFeedbackVals(val);
        }
    }
    
    private void queueGoals(Collection<GoalUpdateValues<OpenServo.Id>> vals){
        for(GoalUpdateValues<OpenServo.Id> val : vals){
            myGoalParamQueue.put(val.getServoId(), val);
        }
    }
    
    private void mergeGoals(Collection<GoalUpdateValues<OpenServo.Id>> goals){
        for(GoalUpdateValues<OpenServo.Id> val : goals){
            OpenServo.Id id = val.getServoId();
            OpenServoCache cache = myValueMap.get(id);
            if(cache == null){
                cache = new OpenServoCache(id);
                cache.setFeedbackVals(
                        new OpenServoFeedbackValues(id, new int[5], 0));
                myValueMap.put(id, cache);
            }
            cache.setGoalVals(val);
        }
        if(!goals.isEmpty()){
            setMoveFlag(true);
        }
    }
    
    public synchronized boolean getMoveFlag(){
        return myMoveFlag;
    }
    
    public synchronized void setMoveFlag(boolean val){
        myMoveFlag = val;
    }
    
    public Map<OpenServo.Id,OpenServoCache> getValueMap(){
        return myValueMap;
    }
}
