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

import org.jflux.api.common.rk.position.DoubleRange;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.types.Temperature;
import org.mechio.api.motion.joint_properties.ReadTemperature;
import org.mechio.impl.motion.dynamixel.DynamixelServo;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class TemperatureProperty extends ReadTemperature { 
    private DynamixelServo myDynamixelServo;
    protected Temperature myCachedValue;
    
    public TemperatureProperty(DynamixelServo dyna){
        if(dyna == null){
            throw new NullPointerException();
        }
        myDynamixelServo = dyna;
    }

    @Override
    public Temperature getValue() {
        Temperature old = myCachedValue;
        myCachedValue = readValue();
        firePropertyChange(getPropertyName(), old, myCachedValue);
        return myCachedValue;
    }
    
    private Temperature readValue(){
        Integer temperature = myDynamixelServo.getCurrentTemperature();
        if(temperature != null){
            return new Temperature(temperature);
        }
        return null;
    }
    
    @Override
    public NormalizableRange<Temperature> getNormalizableRange() {
        return new NormalizableRange<Temperature>() {
            private DoubleRange myDoubleRange = new DoubleRange(0, 1000);
            
            @Override
            public boolean isValid(Temperature t) {
                return myDoubleRange.isValid(t.getDegreesCelsius());
            }
            @Override
            public NormalizedDouble normalizeValue(Temperature t) {
                return myDoubleRange.normalizeValue(t.getDegreesCelsius());
            }
            @Override
            public Temperature denormalizeValue(NormalizedDouble v) {
                return new Temperature(myDoubleRange.denormalizeValue(v));
            }
            @Override
            public Temperature getMin() {
                return new Temperature(myDoubleRange.getMin());
            }
            @Override
            public Temperature getMax() {
                return new Temperature(myDoubleRange.getMax());
            }
        };
    }
}
