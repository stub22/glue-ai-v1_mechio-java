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
package org.mechio.api.motion.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jflux.api.common.rk.position.DoubleRange;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.JointProperty;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.protocol.RobotDefinitionResponse.JointDefinition;
import org.mechio.api.motion.protocol.RobotDefinitionResponse.JointPropDefinition;

/**
 * RemoteJoint represents a Joint on a RemoteRobot.
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteJoint extends PropertyChangeNotifier implements Joint {
    private Joint.Id myJointId;
    private Robot.JointId myRobotJointId;
    private String myName;
    private NormalizedDouble myDefaultPosition;
    private NormalizedDouble myCachedGoalPosition;
    private Boolean myEnabledFlag;
    private RemoteRobot myRobot;
    private NormalizableRange<Double> myRange;
    Map<String,JointProperty> myProperties;
    
    /**
     * Creates a RemoteJoint with the given values.
     * @param robot RemoteRobot this RemoteJoint belongs to
     * @param def JointDefinition for initializing this Joint
     */
    public RemoteJoint(RemoteRobot robot, JointDefinition def){
        if(robot == null || def == null){
            throw new NullPointerException();
        }
        myRobot = robot;
        myJointId = def.getJointId();
        myRobotJointId = new Robot.JointId(myRobot.getRobotId(), myJointId);
        myName = def.getName();
        myDefaultPosition = def.getDefaultPosition();
        myCachedGoalPosition = def.getGoalPosition();
        myEnabledFlag = def.getEnabled();
        myRange = new DoubleRange(0.0, 1.0);
        myProperties = new HashMap<String, JointProperty>();
        for(JointPropDefinition p : def.getJointProperties()){
            JointProperty<Double> prop = new RemoteJointProperty<Double>(
                    p.getPropertyName(), p.getDisplayName(), 
                    Double.class, p.getInitialValue(), 
                    new DoubleRange(p.getMinValue(), p.getMaxValue()));
            myProperties.put(p.getPropertyName(), prop);
        }
    }
    
    @Override
    public Joint.Id getId() {
        return myJointId;
    }

    @Override
    public String getName() {
        return myName;
    }

    @Override
    public NormalizedDouble getDefaultPosition() {
        return myDefaultPosition;
    }

    /**
     * 
     * @param goal
     */
    protected void setGoalPosition(NormalizedDouble goal){
        NormalizedDouble old = myCachedGoalPosition;
        myCachedGoalPosition = goal;
        firePropertyChange(PROP_GOAL_POSITION, old, goal);
    }
    
    @Override
    public NormalizedDouble getGoalPosition() {
        return myCachedGoalPosition;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        Boolean old = myEnabledFlag;
        if(!myRobot.setJointEnabled(myRobotJointId, enabled)){
            return;
        }
        myEnabledFlag = enabled;
        firePropertyChange(PROP_ENABLED, old, myEnabledFlag);
    }

    @Override
    public Boolean getEnabled() {
        return myEnabledFlag;
    }
    /**
     * Makes a request to the remote joint for the actual enabled status, and 
     * updates this joint, firing a property change event.
     * Returns true if the request was successful and false if it timed out.
     * @return true if the request was successful and false if it timed out
     */
    public Boolean updateEnabledStatus(){
        Boolean enabled = myRobot.getJointEnabled(myRobotJointId);
        if(enabled == null){
            return false;
        }
        Boolean old = myEnabledFlag;
        myEnabledFlag = enabled;
        firePropertyChange(PROP_ENABLED, old, myEnabledFlag);
        return true;
    }

    @Override
    public <T> JointProperty<T> getProperty(String name, Class<T> propertyType) {
        if(propertyType != Double.class){
            return null;
        }
        return getProperty(name);
    }

    @Override
    public JointProperty getProperty(String name) {
        return myProperties.get(name);
    }

    @Override
    public NormalizableRange<Double> getPositionRange() {
        return myRange;
    }

    @Override
    public Collection<JointProperty> getProperties() {
        return myProperties.values();
    }
}
