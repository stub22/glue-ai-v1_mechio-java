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

package org.mechio.impl.motion.dynamixel.properties;

import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.impl.motion.dynamixel.DynamixelServo;
import org.mechio.api.motion.joint_properties.ReadCurrentPosition;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class CurrentPositionProperty extends ReadCurrentPosition { 
    private DynamixelServo myDynamixelServo;
    protected NormalizedDouble myCachedValue;
    
    public CurrentPositionProperty(DynamixelServo dyna){
        if(dyna == null){
            throw new NullPointerException();
        }
        myDynamixelServo = dyna;
    }

    @Override
    public NormalizedDouble getValue() {
        NormalizedDouble old = myCachedValue;
        myCachedValue = readValue();
        firePropertyChange(getPropertyName(), old, myCachedValue);
        return myCachedValue;
    }
    
    private NormalizedDouble readValue(){
        return myDynamixelServo.getCurrentPosition();
    }
    
    @Override
    public NormalizableRange<NormalizedDouble> getNormalizableRange() {
        return new NormalizableRange<NormalizedDouble>() {

            @Override
            public boolean isValid(NormalizedDouble t) {
                return true;
            }

            @Override
            public NormalizedDouble normalizeValue(NormalizedDouble t) {
                return t;
            }

            @Override
            public NormalizedDouble denormalizeValue(NormalizedDouble v) {
                return v;
            }

            @Override
            public NormalizedDouble getMin() {
                return new NormalizedDouble(0.0);
            }

            @Override
            public NormalizedDouble getMax() {
                return new NormalizedDouble(1.0);
            }
        };
    }
}
