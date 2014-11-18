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
package org.mechio.api.motion.servos;

import java.util.HashMap;
import java.util.List;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.api.motion.AbstractJoint;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.JointProperty;

/**
 *
 * @param <S> Servo Type to use
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ServoJoint<S extends Servo> extends AbstractJoint{
    /**
     * The ServoJoint's Servo
     */
    protected S myServo;

    /**
     * Creates a new ServoJoint
     * @param jointId Joint.Id to use
     * @param servo Servo to use
     * @param properties JointProperties for this Joint
     */
    public ServoJoint(
            Joint.Id jointId, S servo, List<JointProperty> properties){
        super(jointId);
        if(servo == null){
            throw new NullPointerException();
        }
        myServo = servo;
        myServo.addPropertyChangeListener(this);
        if(properties == null){
            myProperties = new HashMap<String, JointProperty>();
        }else{
            myProperties = 
                    new HashMap<String, JointProperty>(properties.size());
            for(JointProperty jp : properties){
                myProperties.put(jp.getPropertyName(), jp);
            }
        }
    }
    
    @Override
    public String getName() {
        return myServo.getName();
    }

    @Override
    public NormalizedDouble getDefaultPosition() {
        return myServo.getDefaultPosition();
    }
    
    @Override
    public NormalizableRange<Double> getPositionRange(){
        return myServo.getPositionRange();
    }

    @Override
    public void setEnabled(Boolean enabled) {
        Boolean oldVal = myServo.getEnabled();
        myServo.setEnabled(enabled);
        firePropertyChange(PROP_ENABLED, oldVal, enabled);
    }

    @Override
    public Boolean getEnabled() {
        return myServo.getEnabled();
    }

    @Override
    public NormalizedDouble getGoalPosition(){
        return myServo.getGoalPosition();
    }
    /**
     * Returns the Servo backing this ServoJoint.
     * @return Servo backing this ServoJoint
     */
    public Servo getServo(){
        return myServo;
    }

    /**
     * Allows the ServoRobot to set the goal position.
     * @param pos new goal position
     */
    protected void setGoalPosition(NormalizedDouble pos) {
        NormalizedDouble oldPos = myServo.getGoalPosition();
        myServo.setGoalPosition(pos);
        firePropertyChange(PROP_GOAL_POSITION, oldPos, pos);
    }
}
