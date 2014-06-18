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

package org.mechio.api.motion.utils;

import java.util.Map.Entry;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.osgi.framework.BundleContext;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.JointId;
import org.mechio.api.motion.Robot.RobotPositionHashMap;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.protocol.DefaultMotionFrame;
import org.mechio.api.motion.protocol.MotionFrame;

/**
 * A FrameSource which moves the Joints of a Robot towards a set of target
 * positions with a limited velocity.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PositionTargetFrameSource implements RobotFrameSource{
    private RobotPositionMap myTargetPositions;
    private BundleContext myContext;
    private double myVelocity;
    private boolean myEnabledFlag;
    private boolean myStopOnGoalFlag;
    private Robot myRobot;
    
    /**
     * Creates a new MotionTargetFrameSource
     * @param context BundleContext used to retrieve the Robot
     * @param robotId Id of the Robot to move
     * @param velocity maximum allowed velocity.  The velocity is measured in 
     * terms of (change in NormalizedDouble)/millisecond
     * @param targetPositions initial target positions to move towards.  
     * Positions for Joints not belonging to the Robot with the given robotId
     * are ignored
     */
    public PositionTargetFrameSource(
            double velocity, RobotPositionMap targetPositions){
        myTargetPositions = new RobotPositionHashMap();
        if(targetPositions != null){
            myTargetPositions.putAll(targetPositions);
        }
        myVelocity = velocity;
        myEnabledFlag = true;
        myStopOnGoalFlag = true;
    }

    @Override
    public void setRobot(Robot robot) {
        myRobot = robot;
    }

    @Override
    public Robot getRobot() {
        return myRobot;
    }
    
    /**
     * If false, this MotionTargetFrameSource will return null when queried for
     * Movements.
     * A MotionTargetFrameSource is automatically enabled when target positions
     * are changed, and disabled if they are cleared.
     * @param enabled
     */
    public void setEnabled(boolean enabled){
        myEnabledFlag = enabled;
    }
    
    /**
     * Returns true if enabled
     * @return true if enabled
     */
    public boolean getEnabled(){
        return myEnabledFlag;
    }
    
    /**
     * Set the max velocity, measured as (change in NormalizedDouble)/millisecond.
     * A velocity of 0.0005 moves a joint across its full range in 2 seconds.
     * @param velocity max velocity, measured as 
     * (change in NormalizedDouble)/millisecond
     */
    public void setVelocity(double velocity){
        if(velocity < 0){
            return;
        }
        myVelocity = velocity;
    }
    
    /**
     * Returns the velocity, measured as (change in NormalizedDouble)/millisecond
     * @return the velocity, measured as (change in NormalizedDouble)/millisecond
     */
    public double getVelocity(){
        return myVelocity;
    }
    
    /**
     * When StopOnGoal is set true, the MotionTargetFrameSource will disable 
     * itself after reaching the target positions.  Target positions are assumed
     * to be reached when getMovements is called and all current positions are
     * equal to the target positions.  When this happens Enabled is set to 
     * false.  The MotionTargetFrameSource can be re-enabled by setting Enabled
     * to true.
     * @param val value for StopOnGoal
     */
    public void setStopOnGoal(boolean val){
        myStopOnGoalFlag = val;
    }
    
    /**
     * Returns the StopOnGoalFlag.
     * @return the StopOnGoalFlag
     */
    public boolean getStopOnGoalFlag(){
        return myStopOnGoalFlag;
    }
    
    /**
     * Puts the given positions in the target position map and enables this
     * MotionTargetFrameSource.
     * @param targetPositions positions to add
     */
    public void putPositions(RobotPositionMap targetPositions){
        if(targetPositions == null){
            return;
        }
        if(positionsEqual(targetPositions)){
            return;
        }
        myTargetPositions.putAll(targetPositions);
        setEnabled(true);
    }
    
    private boolean positionsEqual(RobotPositionMap targetPositions){
        if(myTargetPositions.size() < targetPositions.size()){
            return false;
        }
        for(Entry<JointId,NormalizedDouble> e : targetPositions.entrySet()){
            JointId id = e.getKey();
            NormalizedDouble val = e.getValue();
            if(id == null || val == null){
                continue;
            }
            NormalizedDouble newVal = myTargetPositions.get(id);
            if(!val.equals(newVal)){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Set the target position for the given JointId and enables this
     * MotionTargetFrameSource.
     * @param id JointId to set
     * @param position new position to set
     */
    public void putPosition(Robot.JointId id, NormalizedDouble position){
        myTargetPositions.put(id, position);
        setEnabled(true);
    }
    
    /**
     * Clears all target positions and disables this MotionTargetFrameSource.
     */
    public void clearPositions(){
        myTargetPositions.clear();
        setEnabled(false);
    }

    /**
     * Creates a MotionFrame starting at the Robot's goal positions, and moving
     * towards the target positions as much as the velocity will allow in the 
     * given time interval.
     * @param time the time of this movement, often the current time
     * @param interval time length of the movement in milliseconds
     * @return MotionFrame starting at the Robot's goal positions, and moving
     * towards the target positions as much as the velocity will allow in the 
     * given time interval
     */
    @Override
    public MotionFrame getMovements(long time, long interval) {
        if(myRobot == null || myTargetPositions == null || 
                !myEnabledFlag || myVelocity <= 0){
            return null;
        }
        RobotPositionMap currentPos = myRobot.getCurrentPositions();
        if(currentPos == null || currentPos.isEmpty()){
            return null;
        }
        RobotPositionMap goals = new RobotPositionHashMap();
        MotionFrame frame = new DefaultMotionFrame();
        frame.setTimestampMillisecUTC(time);
        frame.setFrameLengthMillisec(interval);
        for(Entry<Robot.JointId,NormalizedDouble> e : 
                currentPos.entrySet()){
            Robot.JointId id = e.getKey();
            NormalizedDouble cur = e.getValue();
            NormalizedDouble goal = myTargetPositions.get(id);
            if(goal == null || cur == null){
                continue;
            }
            int sign = goal.compareTo(cur) >= 0 ? 1 : -1;
            Double newPos = cur.getValue() + interval*myVelocity*sign;
            if(!NormalizedDouble.isValid(newPos)){
                continue;
            }
            NormalizedDouble pos = new NormalizedDouble(newPos);
            if(sign > 0){
                pos = pos.compareTo(goal) < 0 ? pos : goal;
            }else{
                pos = pos.compareTo(goal) > 0 ? pos : goal;
            }
            goals.put(id, pos);
        }
        if(goals.isEmpty()){
            return null;
        }
        if(myStopOnGoalFlag){
            disableAtGoal();
        }
        frame.setGoalPositions(goals);
        return frame;
    }
    
    protected void disableAtGoal(){
        if(isAtGoal()){
            setEnabled(false);
        }
        
    }
    
    protected boolean isAtGoal(){
        RobotPositionMap currentGoals = myRobot.getGoalPositions();
        if(currentGoals == null || currentGoals.isEmpty()){
            return true;
        }
        boolean atGoal = true;
        for(Entry<Robot.JointId,NormalizedDouble> e : currentGoals.entrySet()){
            NormalizedDouble curGoal = e.getValue();
            NormalizedDouble target = myTargetPositions.get(e.getKey());
            if(target == null || curGoal == null){
                continue;
            }
            atGoal = atGoal && (target.equals(curGoal));
        }
        return atGoal;
    }
    
}
