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

import org.mechio.api.motion.servos.ServoJoint;
import java.util.List;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.JointProperty;
import org.mechio.api.motion.servos.Servo;

/**
 * Common functionality for ServoJointAdapters.
 * @param <S> Type of Servo used
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class AbstractServoJointAdapter<S extends Servo> implements 
        ServoJointAdapter<S,ServoJoint<S>> {

    /**
     * Creates a ServoJoint with the given Joint.Id, Servo, and default 
     * JointProperties returned by getJointProperties
     * @param jId Joint.Id for the ServoJoint
     * @param s Servo to use
     * @return ServoJoint with the given Joint.Id, Servo, and default 
     * JointProperties returned by getJointProperties
     */
    @Override
    public ServoJoint<S> getJoint(Joint.Id jId, S s) {
        List<JointProperty> props = getJointProperties(s);
        ServoJoint<S> servoJoint = new ServoJoint(jId, s, props);
        return servoJoint;
    }
    
    /**
     * Returns the default JointProperties for a new ServoJoint.
     * @param servo Servo to get JointProperties for
     * @return A List of the default JointProperties for a new ServoJoint
     */
    protected abstract List<JointProperty> getJointProperties(S servo);
    
}
