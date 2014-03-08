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
 * Defines the joint ids for RoboKind's R25 robot.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public final class R25RobotJoints {
    /**
     * Waist
     */
    public final static int WAIST = 100;
    
    /**
     * Neck Yaw
     */
    public final static int NECK_YAW = 200;
    /**
     * Neck Roll
     */
    
    public final static int NECK_PITCH = 202;
    
    /**
     * Brows
     */
    public final static int BROWS = 300;
    /**
     * Eyelids
     */
    public final static int EYELIDS = 301;
    /**
     * Eyes Pitch
     */
    public final static int EYE_YAW = 311;
    /**
     * Eye Right
     */
//    public final static int LEFT_SMILE = 320;
//    /**
//     * Smile Right
//     */
//    public final static int RIGHT_SMILE = 321;
//    /**
//     * Jaw
//     */
    public final static int SMILE = 320;
    /**
     * Smile Right
     */
    public final static int JAW = 322;
    
    /**
     * Left Shoulder Pitch
     */
    public final static int LEFT_SHOULDER_YAW = 400;
    /**
     * Left Shoulder Roll
     */
    public final static int LEFT_SHOULDER_ROLL = 401;
    /**
     * Left Elbow Yaw
     */
    public final static int LEFT_ELBOW = 410;
    /**
     * Left Elbow Pitch
     */
    public final static int LEFT_WRIST_YAW = 420;
    /**
     * Left Grasp
     */
    public final static int LEFT_HAND_GRASP = 421;
    
    /**
     * Right Shoulder Pitch
     */
    public final static int RIGHT_SHOULDER_YAW = 500;
    /**
     * Right Shoulder Roll
     */
    public final static int RIGHT_SHOULDER_ROLL = 501;
    /**
     * Right Elbow Yaw
     */
    public final static int RIGHT_ELBOW = 510;
    /**
     * Right Elbow Pitch
     */
    public final static int RIGHT_WRIST_YAW = 520;
    /**
     * Right Grasp
     */
    public final static int RIGHT_HAND_GRASP = 521;
    
    /**
     * Left Hip Roll
     */
    
    
    /**
     * Unmodifiable list containing all joint ids.
     */
    public final static List<Integer> ALL_JOINTS = 
            Collections.unmodifiableList(Arrays.asList(
                    WAIST, 
                    
                    NECK_YAW, NECK_PITCH,
                    BROWS, EYELIDS, EYE_YAW,  
                    SMILE, JAW, 
                    
                    LEFT_SHOULDER_YAW, LEFT_SHOULDER_ROLL, 
                    LEFT_ELBOW,  
                    LEFT_WRIST_YAW, LEFT_HAND_GRASP,
                    RIGHT_SHOULDER_YAW, RIGHT_SHOULDER_ROLL, 
                    RIGHT_ELBOW, 
                    RIGHT_WRIST_YAW, RIGHT_HAND_GRASP));
    
    private R25RobotJoints(){}
}
