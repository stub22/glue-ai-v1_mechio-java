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

package org.mechio.impl.motion.dynamixel.utils;

import java.util.ArrayList;
import java.util.List;
import org.mechio.api.motion.JointProperty;
import org.mechio.api.motion.servos.utils.AbstractServoJointAdapter;
import org.mechio.impl.motion.dynamixel.DynamixelServo;
import org.mechio.impl.motion.dynamixel.properties.AbsCurrentPositionProperty;
import org.mechio.impl.motion.dynamixel.properties.AbsolutePositionProperty;
import org.mechio.impl.motion.dynamixel.properties.CurrentPositionProperty;
import org.mechio.impl.motion.dynamixel.properties.LoadProperty;
import org.mechio.impl.motion.dynamixel.properties.TemperatureProperty;
import org.mechio.impl.motion.dynamixel.properties.VoltageProperty;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DynamixelJointAdapter extends  
        AbstractServoJointAdapter<DynamixelServo> {
    
    @Override
    protected List<JointProperty> getJointProperties(DynamixelServo s) {
        List<JointProperty> props = new ArrayList();
        props.add(new CurrentPositionProperty(s));
        props.add(new AbsolutePositionProperty(s));
        props.add(new AbsCurrentPositionProperty(s));
        props.add(new LoadProperty(s));
        props.add(new TemperatureProperty(s));
        props.add(new VoltageProperty(s));
        return props;
    }
}
