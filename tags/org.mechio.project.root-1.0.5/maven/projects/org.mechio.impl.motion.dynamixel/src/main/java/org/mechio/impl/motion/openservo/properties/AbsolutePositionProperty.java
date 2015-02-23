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

package org.mechio.impl.motion.openservo.properties;

import org.jflux.api.common.rk.position.IntegerRange;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.api.motion.JointProperty;
import org.mechio.impl.motion.openservo.OpenServo;

/**
 *
 * @author matt
 */
public class AbsolutePositionProperty extends JointProperty.ReadOnly<Integer>{
    private OpenServo myServo;
    
    public AbsolutePositionProperty(OpenServo servo){
        myServo = servo;
    }

    @Override
    public String getPropertyName() {
        return "absGoalPosition";
    }

    @Override
    public String getDisplayName() {
        return "Absolute Goal Postion";
    }

    @Override
    public Class<Integer> getPropertyClass() {
        return Integer.class;
    }

    @Override
    public Integer getValue() {
        double min = myServo.getConfig().getMinPosition();
        double max = myServo.getConfig().getMaxPosition();
        NormalizedDouble val = myServo.getGoalPosition();
        if(val == null){
            return null;
        }
        double cur = val.getValue();
        double range = max - min;
        cur = cur*range + min;
        return (int)cur;
    }
    
    @Override
    public NormalizableRange<Integer> getNormalizableRange() {
        return new IntegerRange(0, 1023);
    }
}
