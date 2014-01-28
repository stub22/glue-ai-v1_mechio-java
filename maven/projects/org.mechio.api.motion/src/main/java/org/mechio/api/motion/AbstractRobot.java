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
package org.mechio.api.motion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.mechio.api.motion.joint_properties.ReadCurrentPosition;

/**
 * Provides common functionality for Robot implementations
 * @param <J> Type of Joint used by this Robot
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class AbstractRobot<J extends Joint> 
        extends PropertyChangeNotifier implements Robot<J> {
    private Robot.Id myRobotId;
    private boolean myEnabledFlag;
    
    /**
     * Map of the Robot's JointIds and Joints.
     */
    protected Map<Robot.JointId,J> myJointMap;
    /**
     * List of Joints to preserve order
     */
    protected List<J> myJointList;
    
    /**
     * Creates an empty Robot with the given RobotId
     * @param robotId unique RobotId
     */
    public AbstractRobot(Robot.Id robotId){
        if(robotId == null){
            throw new NullPointerException();
        }
        myRobotId = robotId;
        myJointMap = new HashMap<Robot.JointId, J>();
        myJointList = new ArrayList<J>();
    }

    @Override
    public Robot.Id getRobotId() {
        return myRobotId;
    }

    @Override
    public J getJoint(Robot.JointId id) {
        if(id == null || myJointMap == null){
            throw new NullPointerException();
        }
        return myJointMap.get(id);
    }

    @Override
    public Set<Robot.JointId> getJointIds() {
        if(myJointMap == null){
            throw new NullPointerException();
        }
        return Collections.unmodifiableSet(myJointMap.keySet());
    }

    @Override
    public String getJointName(Robot.JointId id) {
        if(id == null || myJointMap == null){
            throw new NullPointerException();
        }
        J joint = myJointMap.get(id);
        if(joint == null){
            return null;
        }
        return joint.getName();
    }

    /**
     * Returns true if the Robot is enabled and accepting commands.
     * @return true if the Robot is enabled and accepting commands
     */
    @Override
    public boolean isEnabled() {
        return myEnabledFlag;
    }

    /**
     * Sets the enabled status of the Robot.  The Robot only accepts new 
     * movements when Enabled is set to true.
     * @param val enabled value
     */
    @Override
    public void setEnabled(boolean val) {
        boolean oldVal = myEnabledFlag;
        myEnabledFlag = val;
        firePropertyChange(PROP_ENABLED, oldVal, val);
    }

    @Override
    public RobotPositionMap getDefaultPositions() {
        if(myJointMap == null){
            throw new NullPointerException();
        }
        RobotPositionMap posMap = new RobotPositionHashMap(myJointMap.size());
        for(Entry<Robot.JointId, J> e : myJointMap.entrySet()){
            Robot.JointId id = e.getKey();
            if(id == null){
                throw new NullPointerException();
            }
            J joint = e.getValue();
            if(joint == null){
                throw new NullPointerException();
            }
            posMap.put(id, joint.getDefaultPosition());
        }
        return posMap;
    }

    @Override
    public RobotPositionMap getCurrentPositions() {
        if(myJointMap == null){
            throw new NullPointerException();
        }
        RobotPositionMap posMap = new RobotPositionHashMap(myJointMap.size());
        for(Entry<Robot.JointId, J> e : myJointMap.entrySet()){
            Robot.JointId id = e.getKey();
            if(id == null){
                throw new NullPointerException();
            }
            J joint = e.getValue();
            if(joint == null){
                throw new NullPointerException();
            }
            NormalizedDouble val;
            JointProperty curPos = 
                    joint.getProperty(ReadCurrentPosition.PROPERTY_NAME);
            if(curPos != null){
                NormalizableRange range = curPos.getNormalizableRange();
                Object obj = curPos.getValue();
                val = range.normalizeValue(obj);
            }else{
                val = joint.getGoalPosition();
            }
            posMap.put(id, val);
        }
        return posMap;
    }

    @Override
    public RobotPositionMap getGoalPositions() {
        if(myJointMap == null){
            throw new NullPointerException();
        }
        RobotPositionMap posMap = new RobotPositionHashMap(myJointMap.size());
        for(Entry<Robot.JointId, J> e : myJointMap.entrySet()){
            Robot.JointId id = e.getKey();
            if(id == null){
                throw new NullPointerException();
            }
            J joint = e.getValue();
            if(joint == null){
                throw new NullPointerException();
            }
            posMap.put(id, joint.getGoalPosition());
        }
        return posMap;
    }
    /**
     * Adds the given Joint to the Robot.
     * @param j Joint to add
     * @return true if successful
     */
    protected boolean addJoint(J j){
        Joint.Id jId = j.getId();
        Robot.JointId jointId = new Robot.JointId(getRobotId(), jId);
        if(myJointMap.containsKey(jointId) || myJointList.contains(j)){
            return false;
        }
        myJointMap.put(jointId, j);
        myJointList.add(j);
        return true;
    }
    /**
     * Removes the given Joint from the Robot.
     * @param j Joint to remove
     * @return true if successful
     */
    protected boolean removeJoint(J j){
        Joint.Id jId = j.getId();
        Robot.JointId jointId = new Robot.JointId(getRobotId(), jId);
        if(!myJointMap.containsKey(jointId) || !myJointList.contains(j)){
            return false;
        }
        myJointMap.remove(jointId);
        myJointList.remove(j);
        return true;
    }
    /**
     * Removes all Joints from the Robot.
     */
    protected void clearJoints(){
        myJointMap.clear();
        myJointList.clear();
    }

    @Override
    public List<J> getJointList() {
        return myJointList;
    }
}
