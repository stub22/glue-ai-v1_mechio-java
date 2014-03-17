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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.mechio.impl.motion.dynamixel.DynamixelServo;

/**
 * Provides concurrency for reading and updating values used by the 
 * DynamixelControlLoop.  This will accept new values, both dynamixel feedback 
 * values and new goal positions, merging them in a thread-safe manner.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ConcurrentDynamixelCache {
    private Map<DynamixelServo.Id,DynamixelServoCache> myValueMap;
    private Map<DynamixelServo.Id,FeedbackUpdateValues> myFeedbackQueue;
    private Map<DynamixelServo.Id,GoalUpdateValues<DynamixelServo.Id>> myGoalParamQueue;
    private Lock myValueLock;
    private boolean myMoveFlag;
    
    public ConcurrentDynamixelCache(){
        myValueMap = new HashMap<DynamixelServo.Id, DynamixelServoCache>();
        myFeedbackQueue = new HashMap<DynamixelServo.Id, FeedbackUpdateValues>();
        myGoalParamQueue = new HashMap<DynamixelServo.Id, GoalUpdateValues<DynamixelServo.Id>>();
        myValueLock = new ReentrantLock();
        myMoveFlag = false;
    }
    
    /**
     * Locks the cache and returns the MoveParams.
     * releaseMoveParams() must be called when done using the params.
     * @return 
     */
    synchronized Collection<MoveParams<DynamixelServo.Id>> acquireMoveParams(){
        myValueLock.lock();
        return (Collection)myValueMap.values();
    }
    
    synchronized void releaseMoveParams(){
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
    synchronized void addFeedbackValues(Collection<FeedbackUpdateValues> values){
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
    
    synchronized void setGoalPositions(Collection<GoalUpdateValues<DynamixelServo.Id>> goals){
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
    
    private void queueFeedback(Collection<FeedbackUpdateValues> vals){
        for(FeedbackUpdateValues val : vals){
            if(val.getCurrentTemperature() == 0 
                    || val.getCurrentVoltage() == 0){
                continue;
            }
            myFeedbackQueue.put(val.getServoId(), val);
        }
    }
    
    private void mergeFeedback(Collection<FeedbackUpdateValues> vals){
        for(FeedbackUpdateValues val : vals){
            DynamixelServo.Id id = val.getServoId();
            DynamixelServoCache cache = myValueMap.get(id);
            if(cache == null){
                cache = new DynamixelServoCache(id);
                cache.setGoalVals(new GoalUpdateValues(id, -1, 0));
                cache.goalsSent();
                myValueMap.put(id, cache);
            }
            cache.setFeedbackVals(val);
        }
    }
    
    private void queueGoals(Collection<GoalUpdateValues<DynamixelServo.Id>> vals){
        for(GoalUpdateValues<DynamixelServo.Id> val : vals){
            myGoalParamQueue.put(val.getServoId(), val);
        }
    }
    
    private void mergeGoals(Collection<GoalUpdateValues<DynamixelServo.Id>> goals){
        for(GoalUpdateValues<DynamixelServo.Id> val : goals){
            DynamixelServo.Id id = val.getServoId();
            DynamixelServoCache cache = myValueMap.get(id);
            if(cache == null){
                cache = new DynamixelServoCache(id);
                cache.setFeedbackVals(
                        new FeedbackUpdateValues(id, new int[5], 0));
                myValueMap.put(id, cache);
            }
            cache.setGoalVals(val);
        }
        if(!goals.isEmpty()){
            setMoveFlag(true);
        }
    }
    
    synchronized boolean getMoveFlag(){
        return myMoveFlag;
    }
    
    synchronized void setMoveFlag(boolean val){
        myMoveFlag = val;
    }
    
    public Map<DynamixelServo.Id,DynamixelServoCache> getValueMap(){
        return myValueMap;
    }
}
