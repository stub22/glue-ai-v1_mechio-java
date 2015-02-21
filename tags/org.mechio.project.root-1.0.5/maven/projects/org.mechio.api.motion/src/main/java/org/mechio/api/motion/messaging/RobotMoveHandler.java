package org.mechio.api.motion.messaging;

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


import org.jflux.api.core.Listener;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.protocol.MotionFrameEvent;
import org.mechio.api.motion.utils.RobotMoverFrameSource;

/**
 * Listens for MotionFrameEvents and moves a Robot.
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotMoveHandler extends 
        RobotMoverFrameSource implements Listener<MotionFrameEvent>{
    
    public RobotMoveHandler(Robot robot){
        super(robot);
    }
    
    @Override
    public void handleEvent(MotionFrameEvent event) {
        if(event == null){
            return;
        }else if(event.getMotionFrame() == null){
            return;
        }
        RobotPositionMap goals = event.getMotionFrame().getGoalPositions();
        long duration = event.getMotionFrame().getFrameLengthMillisec();
        if(goals == null){
            return;
        }
        move(goals, duration);
    }
}
