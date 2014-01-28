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

package org.mechio.impl.motion.pololu;

import java.util.Arrays;
import java.util.List;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.mechio.api.motion.JointProperty;
import org.mechio.api.motion.joint_properties.ReadCurrentPosition;
import org.mechio.api.motion.servos.utils.AbstractServoJointAdapter;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class MaestroJointAdapter extends  
        AbstractServoJointAdapter<MaestroServo> {
    
    @Override
    protected List<JointProperty> getJointProperties(MaestroServo s) {
        return Arrays.asList((JointProperty)new MaestroPosition(s));
    }
    
    public static class MaestroPosition extends 
            PropertyChangeNotifier implements JointProperty<NormalizedDouble> {
        private MaestroServo myServo;
        private NormalizableRange myRange;
        
        public MaestroPosition(MaestroServo servo){
            if(servo == null){
                throw new NullPointerException();
            }
            myServo = servo;
            myRange = new NormalizableRange.DefaultRange();
        }

        @Override
        public String getPropertyName() {
            return ReadCurrentPosition.PROPERTY_NAME;
        }

        @Override
        public String getDisplayName() {
            return ReadCurrentPosition.PROPERTY_NAME;
        }

        @Override
        public Class<NormalizedDouble> getPropertyClass() {
            return NormalizedDouble.class;
        }

        @Override
        public boolean getWriteable() {
            return false;
        }

        @Override
        public NormalizedDouble getValue() {
            return myServo.getGoalPosition();
        }

        @Override
        public void setValue(NormalizedDouble val) {}

        @Override
        public NormalizableRange<NormalizedDouble> getNormalizableRange() {
            return myRange;
        }
    }
}
