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
package org.mechio.api.motion.servos.utils;

import org.jflux.api.common.rk.position.DoubleRange;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.servos.Servo;
import org.mechio.api.motion.servos.ServoController;
import org.mechio.api.motion.servos.ServoJoint;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.api.motion.joint_properties.ReadCurrentPosition;

/**
 * An Empty implementation of a ServoJoint, used for missing Servos.
 * @author Matthew Stevenson <www.mechio.org>
 */
public class EmptyServoJoint extends ServoJoint{    
    /**
     * Creates a new EmptyServoJoint with the given properties.
     * @param jointId Joint.Id of the Joint
     * @param name name of the Joint
     * @param defPos default position of the Joint
     */
    public EmptyServoJoint(
            Joint.Id jointId,
            String name, NormalizedDouble defPos, int min, int max){
        super(jointId, new EmptyServo(jointId, name, defPos, min, max), null);
        addProperty(new ReadCurrentPosition() {
            @Override 
            public NormalizedDouble getValue() {
                return getGoalPosition();
            }
            @Override 
            public NormalizableRange<NormalizedDouble> getNormalizableRange() {
                return NormalizableRange.NORMALIZED_RANGE;
            }
        });
    }
    
    public EmptyServoJoint(
            Joint.Id jointId, String name, NormalizedDouble defPos){
        this(jointId, name, defPos, 0, 1023);
    }
    
    static class EmptyServo extends PropertyChangeNotifier implements Servo{
        private Object myId;
        private String myName;
        private NormalizedDouble myDefaultPosition;
        private NormalizedDouble myGoalPosition;
        private boolean myEnabledFlag;
        private int myMin;
        private int myMax;

        /**
         * Creates a new EmptyServoJoint with the given properties.
         * @param id id of the Servo
         * @param name name of the Servo
         * @param defPos default position of the Servo
         */
        private EmptyServo(
                Object id,
                String name, NormalizedDouble defPos, int min, int max){
            if(id == null || name == null || defPos == null){
                throw new NullPointerException();
            }
            myId = id;
            myName = name;
            myDefaultPosition = defPos;
            myGoalPosition = defPos;
            myEnabledFlag = true;
            myMin = min;
            myMax = max;
        }
        
        @Override
        public Object getId() {
            return myId;
        }
    
        @Override
        public String getName() {
            return myName;
        }

        @Override
        public NormalizedDouble getDefaultPosition() {
            return myDefaultPosition;
        }

        @Override
        public void setEnabled(Boolean enabled) {
            myEnabledFlag = enabled;
        }

        @Override
        public Boolean getEnabled() {
            return myEnabledFlag;
        }

        @Override
        public NormalizedDouble getGoalPosition(){
            return myGoalPosition;
        }

        /**
         * Allows a ServoRobot to set the goal position.
         * @param pos goal position to set
         */
        @Override
        public void setGoalPosition(NormalizedDouble pos) {
            NormalizedDouble oldPos = myGoalPosition;
            myGoalPosition = pos;
            firePropertyChange(PROP_GOAL_POSITION, oldPos, pos);
        }

        @Override
        public ServoConfig getConfig() {
            return null;
        }

        @Override
        public ServoController getController() {
            return null;
        }

        @Override
        public int getMinPosition() {
            return myMin;
        }

        @Override
        public int getMaxPosition() {
            return myMax;
        }

        @Override
        public NormalizableRange<Double> getPositionRange() {
            return new DoubleRange(getMinPosition(), getMaxPosition());
        }
    }
}
