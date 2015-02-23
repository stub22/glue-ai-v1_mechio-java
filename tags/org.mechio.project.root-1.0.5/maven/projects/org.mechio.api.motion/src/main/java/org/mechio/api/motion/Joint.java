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

import java.util.Collection;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.property.PropertyChangeSource;
import org.jflux.impl.services.rk.utils.HashCodeUtil;
import org.jflux.impl.services.rk.utils.LocalIdentifier;

/**
 * A Joint represents the values and properties of a DoF (degree of freedom) of 
 * a Robot.  A Joint provides the goal position, and various JointProperties.
 * Joints are intended to provide read-only access to properties, however
 * some JointProperties can support writing the property as well.
 * 
 * Joints can not be moved directly, move commands must go through the Robot.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface Joint extends PropertyChangeSource {
    /**
     * Property string for GoalPostion.
     */
    public final static String PROP_GOAL_POSITION = "goalPosition";
    /**
     * Property string for Enabled.
     */
    public final static String PROP_ENABLED = "enabled";
    /**
     * Returns the Joint's logical id.
     * @return Joint's logical id
     */
    public Joint.Id getId();
    /**
     * Returns the name of the Joint.
     * @return name of the Joint
     */
    public String getName();
    /**
     * Returns the Joint's default position.
     * @return Joint's default position
     */
    public NormalizedDouble getDefaultPosition();
    /**
     * Returns the NormalizableRange describing the range of motion.
     * @return NormalizableRange describing the range of motion
     */
    public NormalizableRange<Double> getPositionRange();
    /**
     * Returns the goal position.
     * @return goal position
     */
    public NormalizedDouble getGoalPosition();
    
    /**
     * If enabled, this Joint will accept move commands.
     * If not enabled, this joint should not move.
     * @param enabled 
     */
    public void setEnabled(Boolean enabled);
    
    /**
     * If enabled, this Joint will accept move commands.
     * If not enabled, this joint should not move.
     * @return true if enabled
     */
    public Boolean getEnabled();
    
    /**
     * Returns the JointProperty with the given name, ensuring the value is
     * assignable to propertyType.
     * @param <T> Value Type returned by the JointProperty
     * @param name name of the JointProperty
     * @param propertyType Class for T
     * @return the JointProperty with the given name and value assignable to 
     * propertyType
     */
    public <T> JointProperty<T> getProperty(String name, Class<T> propertyType);
    
    /**
     * Returns the JointProperty with the given name.
     * @param name name of the JointProperty
     * @return the JointProperty with the given name
     */
    public JointProperty getProperty(String name);
    
    public Collection<JointProperty> getProperties();
    
    /**
     * Joint.Id is an immutable identifier for a Joint within a Robot or 
     * ServoController.  This is not globally unique.  Use a Robot.JointId if
     * global uniqueness is needed.
     */
    public final static class Id implements LocalIdentifier{
        private int myJointId;
        private int myHashCode;
        
        /**
         * Creates a Id from the given integer id.
         * @param id the jointId
         */
        public Id(int id){
            myJointId = id;
        }
        
        /**
         * Returns the value of the Id.
         * @return the value of the Id
         */
        public int getLogicalJointNumber(){
            return myJointId;
        }
        
        @Override
        public boolean equals(Object obj){
            if(obj == null ||  obj.getClass() != this.getClass()){
                return false;
            }
            return myJointId == ((Id)obj).myJointId;
        }

        @Override
        public int hashCode() {
            if(myHashCode == 0){
                myHashCode = HashCodeUtil.hash(HashCodeUtil.SEED, myJointId);
            }
            return myHashCode;
        }

        @Override
        public String toString() {
            return "" + myJointId;
        }
    }
}
