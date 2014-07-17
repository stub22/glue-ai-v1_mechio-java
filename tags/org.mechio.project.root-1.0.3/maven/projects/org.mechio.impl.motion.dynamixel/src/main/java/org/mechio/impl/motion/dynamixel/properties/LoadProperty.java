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

import org.jflux.api.common.rk.position.IntegerRange;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.mechio.api.motion.joint_properties.ReadLoad;
import org.mechio.impl.motion.dynamixel.DynamixelServo;

/**
 *
 * @author matt
 */
public class LoadProperty extends ReadLoad { 
    private DynamixelServo myDynamixelServo;
    protected Integer myCachedValue;
    
    public LoadProperty(DynamixelServo dyna){
        if(dyna == null){
            throw new NullPointerException();
        }
        myDynamixelServo = dyna;
    }

    @Override
    public Integer getValue() {
        Integer old = myCachedValue;
        myCachedValue = readValue();
        firePropertyChange(getPropertyName(), old, myCachedValue);
        return myCachedValue;
    }
    
    private Integer readValue(){
        return myDynamixelServo.getCurrentLoad();
    }
    
    @Override
    public NormalizableRange<Integer> getNormalizableRange() {
        return new IntegerRange(-1023, 1023);
    }
}
