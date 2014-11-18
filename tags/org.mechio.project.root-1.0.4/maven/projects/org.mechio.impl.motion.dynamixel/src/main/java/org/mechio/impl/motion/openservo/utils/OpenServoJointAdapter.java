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

package org.mechio.impl.motion.openservo.utils;

import java.util.ArrayList;
import java.util.List;
import org.mechio.api.motion.JointProperty;
import org.mechio.api.motion.servos.utils.AbstractServoJointAdapter;
import org.mechio.impl.motion.openservo.OpenServo;
import org.mechio.impl.motion.openservo.properties.AbsCurrentPositionProperty;
import org.mechio.impl.motion.openservo.properties.AbsolutePositionProperty;
import org.mechio.impl.motion.openservo.properties.CurrentPositionProperty;
import org.mechio.impl.motion.openservo.properties.LoadProperty;
import org.mechio.impl.motion.openservo.properties.VoltageProperty;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class OpenServoJointAdapter extends  
        AbstractServoJointAdapter<OpenServo> {
    
    @Override
    protected List<JointProperty> getJointProperties(OpenServo s) {
        List<JointProperty> props = new ArrayList();
        props.add(new CurrentPositionProperty(s));
        props.add(new AbsolutePositionProperty(s));
        props.add(new AbsCurrentPositionProperty(s));
        props.add(new LoadProperty(s));
        props.add(new VoltageProperty(s));
        return props;
    }
}
