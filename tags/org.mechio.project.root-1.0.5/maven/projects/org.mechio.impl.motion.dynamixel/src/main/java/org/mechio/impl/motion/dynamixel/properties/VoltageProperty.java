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
import org.jflux.api.common.rk.types.Voltage;
import org.mechio.api.motion.joint_properties.ReadVoltage;
import org.mechio.impl.motion.dynamixel.DynamixelServo;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VoltageProperty extends ReadVoltage { 
    private DynamixelServo myDynamixelServo;
    protected Voltage myCachedValue;
    
    public VoltageProperty(DynamixelServo dyna){
        if(dyna == null){
            throw new NullPointerException();
        }
        myDynamixelServo = dyna;
    }

    @Override
    public Voltage getValue() {
        Voltage old = myCachedValue;
        myCachedValue = readValue();
        firePropertyChange(getPropertyName(), old, myCachedValue);
        return myCachedValue;
    }
    
    private Voltage readValue(){
        Integer dynaVoltage = myDynamixelServo.getCurrentVoltage();
        if(dynaVoltage != null){
            double volts = dynaVoltage/10.0;
            return new Voltage(volts);
        }
        return null;
    }

    @Override
    public NormalizableRange<Voltage> getNormalizableRange() {
        return new NormalizableRange<Voltage>() {
            private DoubleRange myDoubleRange = new DoubleRange(0, 1000);
            
            @Override
            public boolean isValid(Voltage t) {
                return myDoubleRange.isValid(t.getVolts());
            }
            @Override
            public NormalizedDouble normalizeValue(Voltage t) {
                return myDoubleRange.normalizeValue(t.getVolts());
            }
            @Override
            public Voltage denormalizeValue(NormalizedDouble v) {
                return new Voltage(myDoubleRange.denormalizeValue(v));
            }
            @Override
            public Voltage getMin() {
                return new Voltage(myDoubleRange.getMin());
            }
            @Override
            public Voltage getMax() {
                return new Voltage(myDoubleRange.getMax());
            }
        };
    }
}

