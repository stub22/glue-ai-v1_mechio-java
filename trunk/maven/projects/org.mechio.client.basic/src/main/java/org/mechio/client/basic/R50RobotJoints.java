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
 * Defines the joint ids for RoboKind's R50 robot.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public final class R50RobotJoints {
    /**
     * Waist.
     */
    public final static int WAIST = 100;
    
    /**
     * Neck Yaw.
     */
    public final static int NECK_YAW = 200;
    /**
     * Neck Roll.
     */
    public final static int NECK_ROLL = 201;
    /**
     * Neck Pitch.
     */
    public final static int NECK_PITCH = 202;
    
    /**
     * Brows Pitch.
     */
    public final static int BROWS = 300;
    /**
     * Eyelids.
     */
    public final static int EYELIDS = 301;
    /**
     * Eyes Pitch.
     */
    public final static int EYES_PITCH = 310;
    /**
     * Eye Left.
     */
    public final static int LEFT_EYE_YAW = 311;
    /**
     * Eye Right.
     */
    public final static int RIGHT_EYE_YAW = 312;
    /**
     * Smile Left.
     */
    public final static int LEFT_SMILE = 320;
    /**
     * Smile Right.
     */
    public final static int RIGHT_SMILE = 321;
    /**
     * Jaw.
     */
    public final static int JAW = 322;
    
    /**
     * Left Shoulder Pitch.
     */
    public final static int LEFT_SHOULDER_PITCH = 400;
    /**
     * Left Shoulder Roll.
     */
    public final static int LEFT_SHOULDER_ROLL = 401;
    /**
     * Left Elbow Yaw.
     */
    public final static int LEFT_ELBOW_YAW = 410;
    /**
     * Left Elbow Pitch.
     */
    public final static int LEFT_ELBOW_PITCH = 411;
    /**
     * Left Wrist.
     */
    public final static int LEFT_WRIST_YAW = 420;
    /**
     * Left Grasp.
     */
    public final static int LEFT_HAND_GRASP = 430;
    
    /**
     * Right Shoulder Pitch.
     */
    public final static int RIGHT_SHOULDER_PITCH = 500;
    /**
     * Right Shoulder Roll.
     */
    public final static int RIGHT_SHOULDER_ROLL = 501;
    /**
     * Right Elbow Yaw.
     */
    public final static int RIGHT_ELBOW_YAW = 510;
    /**
     * Right Elbow Pitch.
     */
    public final static int RIGHT_ELBOW_PITCH = 511;
    /**
     * Right Wrist.
     */
    public final static int RIGHT_WRIST_YAW = 520;
    /**
     * Right Grasp.
     */
    public final static int RIGHT_HAND_GRASP = 530;
    
    /**
     * Left Hip Roll.
     */
    public final static int LEFT_HIP_ROLL = 600;
    /**
     * Left Hip Yaw.
     */
    public final static int LEFT_HIP_YAW = 601;
    /**
     * Left Hip Pitch.
     */
    public final static int LEFT_HIP_PITCH = 602;
    /**
     * Left Knee Pitch.
     */
    public final static int LEFT_KNEE_PITCH = 610;
    /**
     * Left Ankle Pitch.
     */
    public final static int LEFT_ANKLE_PITCH = 620;
    /**
     * Left Ankle Roll.
     */
    public final static int LEFT_ANKLE_ROLL = 621;
    
    /**
     * Right Hip Roll.
     */
    public final static int RIGHT_HIP_ROLL = 700;
    /**
     * Right Hip Yaw.
     */
    public final static int RIGHT_HIP_YAW = 701;
    /**
     * Right Hip Pitch.
     */
    public final static int RIGHT_HIP_PITCH = 702;
    /**
     * Right Knee Pitch.
     */
    public final static int RIGHT_KNEE_PITCH = 710;
    /**
     * Right Ankle Pitch.
     */
    public final static int RIGHT_ANKLE_PITCH = 720;
    /**
     * Right Ankle Roll.
     */
    public final static int RIGHT_ANKLE_ROLL = 721;
    
    /**
     * Unmodifiable list containing all joint ids.
     */
    public final static List<Integer> ALL_JOINTS = 
            Collections.unmodifiableList(Arrays.asList(
                    WAIST, 
                    
                    NECK_YAW, NECK_ROLL, NECK_PITCH,
                    BROWS, EYELIDS, EYES_PITCH, LEFT_EYE_YAW, RIGHT_EYE_YAW, 
                    LEFT_SMILE, RIGHT_SMILE, JAW, 
                    
                    LEFT_SHOULDER_PITCH, LEFT_SHOULDER_ROLL, 
                    LEFT_ELBOW_YAW, LEFT_ELBOW_PITCH, 
                    LEFT_WRIST_YAW, LEFT_HAND_GRASP,
                    RIGHT_SHOULDER_PITCH, RIGHT_SHOULDER_ROLL, 
                    RIGHT_ELBOW_YAW, RIGHT_ELBOW_PITCH,
                    RIGHT_WRIST_YAW, RIGHT_HAND_GRASP, 
                    
                    LEFT_HIP_ROLL, LEFT_HIP_YAW, LEFT_HIP_PITCH, 
                    LEFT_KNEE_PITCH, LEFT_ANKLE_PITCH, LEFT_ANKLE_ROLL, 
                    RIGHT_HIP_ROLL, RIGHT_HIP_YAW, RIGHT_HIP_PITCH, 
                    RIGHT_KNEE_PITCH, RIGHT_ANKLE_PITCH, RIGHT_ANKLE_ROLL));
    
    private R50RobotJoints(){}
}
