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

package org.mechio.client.basic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotSensors {
    /**
     * Left Kick.
     */
    public final static int LEFT_FOOT_KICK = 0;
    /**
     * Left Ground.
     */
    public final static int LEFT_FOOT_GROUND = 1;
    /**
     * Left Heel.
     */
    public final static int LEFT_FOOT_HEEL = 2;
    
    /**
     * Right Kick.
     */
    public final static int RIGHT_FOOT_KICK = 3;
    /**
     * Right Ground.
     */
    public final static int RIGHT_FOOT_GROUND = 4;
    /**
     * Right Heel.
     */
    public final static int RIGHT_FOOT_HEEL = 5;
    
    /**
     * Left Hip.
     */
    public final static int LEFT_HIP_PROXIMITY = 9;
    /**
     * Right Hip.
     */
    public final static int RIGHT_HIP_PROXIMITY = 11;
    
    /**
     * Unmodifiable list containing all sensor ids.
     */
    public final static List<Integer> ALL_GPIO_PINS = 
            Collections.unmodifiableList(Arrays.asList(
                LEFT_FOOT_KICK, LEFT_FOOT_GROUND, LEFT_FOOT_HEEL,
                RIGHT_FOOT_KICK, RIGHT_FOOT_GROUND, RIGHT_FOOT_HEEL,
                LEFT_HIP_PROXIMITY, RIGHT_HIP_PROXIMITY));
}
