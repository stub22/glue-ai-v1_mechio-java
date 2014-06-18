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
package org.mechio.api.motion.sync;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.api.motion.AbstractRobot;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;

/**
 * Holds multiple Robots and synchronizes their movements for the given 
 * JointIds.
 * Events and JointProperties from the primary Robot are forwarded.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class SynchronizedRobot extends AbstractRobot<SynchronizedJoint> {
    /**
     * Robot type version name.
     */
    public final static String VERSION_NAME = "SynchronizedRobot";
    /**
     * Robot type version number.
     */
    public final static String VERSION_NUMBER = "1.0";
    /**
     * Robot type VersionProperty.
     */
    public final static VersionProperty VERSION = new VersionProperty(VERSION_NAME, VERSION_NUMBER);
    
    /**
     * Property change event name for adding a Robot.
     */
    public final static String PROP_ADD_ROBOT = "addRobot";
    /**
     * Property change event name for removing a Robot.
     */
    public final static String PROP_REMOVE_ROBOT = "removeRobot";
        
    private List<Robot> myRobots;
    private Robot myPrimaryRobot;
    private RobotPropertyChangeForwarder myPropertyChangeHandler;
    
    /**
     * Creates a new SynchronizedRobot from the given configuration.
     * @param config configuration for initializing the SynchronizedRobot
     */
    public SynchronizedRobot(SynchronizedRobotConfig config){
        super(config.getRobotId());
        myRobots = new ArrayList<Robot>();
        for(SynchronizedJointConfig conf : config.getJointConfigs()){
            addJoint(new SynchronizedJoint(conf, this));
        }
        myPropertyChangeHandler = new RobotPropertyChangeForwarder();
    }
    
    /**
     * Creates a new SynchronizedRobot from the given values
     * @param robotId unique Robot.Id for the SynchronizedRobot to use
     * @param jointIds local ids of the joints to synchronize
     */
    public SynchronizedRobot(Robot.Id robotId, Set<Joint.Id> jointIds){
        super(robotId);
        myRobots = new ArrayList<Robot>();
        for(Joint.Id jId : jointIds){
            addJoint(new SynchronizedJoint(jId, this));
        }
    }
    
    @Override
    public boolean connect() {
        return true;
    }

    @Override
    public void disconnect() {}

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void move(RobotPositionMap positions, long lenMillisec) {
        for(Robot r : myRobots){
            RobotPositionMap newPos = changeId(positions, r.getRobotId());
            r.move(newPos, lenMillisec);
        }
        for(Entry<Robot.JointId,NormalizedDouble> e : positions.entrySet()){
            Robot.JointId jId = e.getKey();
            NormalizedDouble d = e.getValue();
            SynchronizedJoint j = getJoint(jId);
            j.setGoalPosition(d);
        }
    }
    
    private RobotPositionMap changeId(RobotPositionMap pos, Robot.Id newId){
        RobotPositionMap newPos = new RobotPositionHashMap(pos.size());
        for(Entry<Robot.JointId,NormalizedDouble> e : pos.entrySet()){
            Robot.JointId oldJId = e.getKey();
            if(!myJointMap.containsKey(oldJId)){
                continue;
            }
            Robot.JointId jId = new JointId(newId, oldJId.getJointId());
            newPos.put(jId, e.getValue());
        }
        return newPos;
    }
    
    /**
     * Returns the primary Robot being synchronized.  PropertyChangeEvents from
     * the primary Robot are forwarded as PropertyChangeEvents from this Robot.
     * @return primary Robot being synchronized
     */
    public Robot getPrimaryRobot(){
        return myPrimaryRobot;
    }
    
    /**
     * Returns a List of all Robots being Synchronized.
     * @return List of all Robots being Synchronized
     */
    public List<Robot> getRobots(){
        return myRobots;
    }
    
    /**
     * Adds a Robot to be Synchronized.
     * @param robot Robot to add
     */
    public void addRobot(Robot robot){
        if(robot == null){
            throw new NullPointerException();
        }
        if(robot instanceof SynchronizedRobot){
            throw new IllegalArgumentException("Cannot add SynchronizedRobot.");
        }
        if(!myRobots.contains(robot)){
           return;
        }
        myRobots.add(robot);
        updateJoints();
        firePropertyChange(PROP_ADD_ROBOT, null, robot);
    }
    
    /**
     * Removes a Robot from being Synchronized.
     * @param robotId robot to remove
     */
    public void removeRobot(Robot.Id robotId){
        if(robotId == null){
            throw new NullPointerException();
        }
        Robot remove = null;
        for(Robot r : myRobots){
            if(robotId.equals(r.getRobotId())){
                remove = r;
            }
        }
        if(remove == null){
            return;
        }
        if(remove.equals(myPrimaryRobot)){
            myPrimaryRobot = null;
        }
        myRobots.remove(remove);
        updateJoints();
        firePropertyChange(PROP_REMOVE_ROBOT, null, remove);
    }
    
    /**
     * Sets the primary Robot.  The PropertyChangeEvents from the primary robot 
     * are forwarded as PropertyChangeEvents from this Robot.
     * @param robotId
     */
    public void setPrimaryRobot(Robot.Id robotId){
        if(robotId == null){
            throw new NullPointerException();
        }
        for(Robot r : myRobots){
            if(robotId.equals(r.getRobotId())){
                myPrimaryRobot = r;
                updatePrimaryJoints();
                myPropertyChangeHandler.setRobot(myPrimaryRobot);
                return;
            }
        }
    }
    
    private void updateJoints(){
        for(SynchronizedJoint j : myJointList){
            j.updateJointList();
        }
    }
    
    private void updatePrimaryJoints(){
        for(SynchronizedJoint j : myJointList){
            j.updatePrimaryJoint();
        }
    }
    
    class RobotPropertyChangeForwarder implements PropertyChangeListener{
        private Robot myRobot;
        
        public void setRobot(Robot robot){
            if(myRobot != null){
                myRobot.removePropertyChangeListener(this);
            }
            myRobot = robot;
            if(myRobot != null){
                myRobot.addPropertyChangeListener(this);
            }
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            firePropertyChange(evt);
        }
        
    }
}
