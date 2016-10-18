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
package org.mechio.api.motion.utils;

import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.blending.FrameSource;

/**
 * FrameSource intended for a single Robot.
 * The output of a RobotFrameSource is a RobotPositionMap for a single Robot.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface RobotFrameSource extends FrameSource<RobotPositionMap>{
    /**
     * Sets the Robot to 
     * @param robot 
     */
    public void setRobot(Robot robot);
    public Robot getRobot();
}
