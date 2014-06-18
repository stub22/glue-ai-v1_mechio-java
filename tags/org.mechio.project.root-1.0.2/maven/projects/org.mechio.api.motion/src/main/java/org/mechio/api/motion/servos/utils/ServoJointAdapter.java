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

import org.mechio.api.motion.Joint;
import org.mechio.api.motion.servos.Servo;
import org.mechio.api.motion.servos.ServoJoint;

/**
 * A ServoJointAdapter is used by a ServoRobot to create ServoJoints from 
 * Servos.
 * @param <S> Servo Type used
 * @param <J> ServoJoint Type used
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface ServoJointAdapter<S extends Servo, J extends ServoJoint> {
    
    /**
     * Creates a ServoJoint with the given Joint.Id and Servo
     * @param jId Joint.Id for the ServoJoint
     * @param s Servo to use
     * @return ServoJoint with the given Joint.Id and Servo
     */
    public J getJoint(Joint.Id jId, S s);
}
