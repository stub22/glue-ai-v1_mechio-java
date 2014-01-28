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
import java.util.Collection;
import java.util.List;
import org.jflux.api.common.rk.position.DoubleRange;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.JointProperty;
import org.mechio.api.motion.Robot;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
class SynchronizedJoint 
        extends PropertyChangeNotifier implements Joint, PropertyChangeListener{
    private Joint.Id myJointId;
    private String myName;
    private SynchronizedRobot myRobot;
    private NormalizedDouble myDefaultPosition;
    private NormalizedDouble myGoalPosition;
    private Joint myPrimaryJoint;
    private List<Joint> myJoints;
    private boolean myEnabledFlag;
    private NormalizableRange<Double> myRange;

    SynchronizedJoint(SynchronizedJointConfig config, SynchronizedRobot robot){
        this(config.getJointId(), robot, config.getName(), config.getDefaultPosition());
    }
    
    SynchronizedJoint(Joint.Id id, SynchronizedRobot robot){
        this(id, robot, 
                "Joint " + id.getLogicalJointNumber(), 
                new NormalizedDouble(0.5));
    }
    
    SynchronizedJoint(Joint.Id id, 
            SynchronizedRobot robot, String name, NormalizedDouble defPos){
        if(id == null || robot == null || name == null){
            throw new NullPointerException();
        }
        myRobot = robot;
        myJointId = id;
        myName = name;
        myDefaultPosition = defPos;
        if(myDefaultPosition == null){
            myDefaultPosition = new NormalizedDouble(0.5);
        }
        myGoalPosition = myDefaultPosition;
        myEnabledFlag = true;
        myJoints = new ArrayList<Joint>();
        myRange = new SyncRange();
        updateJointList();
    }
    
    final void updatePrimaryJoint(){
        if(myPrimaryJoint != null){
            myPrimaryJoint.removePropertyChangeListener(this);
        }
        Robot r = myRobot.getPrimaryRobot();
        if(r == null){
            myPrimaryJoint = null;
            return;
        }
        Robot.JointId jId = new Robot.JointId(r.getRobotId(), myJointId);
        Joint j = r.getJoint(jId);
        if(myJoints.contains(j)){
            myPrimaryJoint = j;
            myPrimaryJoint.addPropertyChangeListener(this);
        }else{
            myPrimaryJoint = null;
        }
    }
    
    final void updateJointList(){
        List<Robot> robots = myRobot.getRobots();
        myJoints.clear();
        for(Robot r : robots){
            Joint j = r.getJoint(new Robot.JointId(r.getRobotId(), myJointId));
            if(j != null){
                myJoints.add(j);
            }
        }
        updatePrimaryJoint();
    }
    
    @Override
    public Joint.Id getId(){
        return myJointId;
    }

    @Override
    public String getName(){
        if(myPrimaryJoint != null){
            return myPrimaryJoint.getName();
        }
        return myName;
    }

    @Override
    public NormalizedDouble getDefaultPosition(){
        if(myPrimaryJoint != null){
            return myPrimaryJoint.getDefaultPosition();
        }
        return myDefaultPosition;
    }

    @Override
    public NormalizedDouble getGoalPosition(){
        if(myPrimaryJoint != null){
            return myPrimaryJoint.getGoalPosition();
        }
        return myGoalPosition;
    }
    
    void setGoalPosition(NormalizedDouble pos){
        myGoalPosition = pos;
    }

    @Override
    public void setEnabled(Boolean enabled){
        for(Joint j : myJoints){
            j.setEnabled(enabled);
        }
        myEnabledFlag = enabled;
    }

    @Override
    public Boolean getEnabled(){
        return myEnabledFlag;
    }

    @Override
    public <T> JointProperty<T> getProperty(String name, Class<T> propertyType){
        if(myPrimaryJoint == null){
                return null;
        }
        return myPrimaryJoint.getProperty(name, propertyType);
    }

    @Override
    public JointProperty getProperty(String name){
        if(myPrimaryJoint == null){
                return null;
        }
        return myPrimaryJoint.getProperty(name);
    }
    
    @Override
    public Collection<JointProperty> getProperties() {
        return myPrimaryJoint.getProperties();
    }

    /**
     * Used to broadcast PropertyChangeEvents from myPrimaryJoint
     * @param pce internal PropertyChangeEvent to pass
     */
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        firePropertyChange(pce);
    }

    @Override
    public NormalizableRange getPositionRange() {
        return myRange;
    }
    
    private final static NormalizableRange<Double> theDefaultRange = new DoubleRange(0.0, 1.0);

    private class SyncRange implements NormalizableRange<Double> {
        private NormalizableRange<Double> getRange(){
            NormalizableRange<Double> range = myPrimaryJoint.getPositionRange();
            if(range == null){
                return theDefaultRange;
            }
            return range;
        }

        @Override
        public boolean isValid(Double t) {
            return getRange().isValid(t);
        }

        @Override
        public NormalizedDouble normalizeValue(Double t) {
            return getRange().normalizeValue(t);
        }

        @Override
        public Double denormalizeValue(NormalizedDouble v) {
            return getRange().denormalizeValue(v);
        }

        @Override
        public Double getMin() {
            return getRange().getMin();
        }

        @Override
        public Double getMax() {
            return getRange().getMax();
        }
        
    }
}
