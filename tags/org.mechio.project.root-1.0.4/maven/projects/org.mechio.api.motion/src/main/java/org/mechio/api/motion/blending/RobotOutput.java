/*
 *  Copyright 2014 the MechIO Project. All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *  
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *  
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE MECHIO PROJECT "AS IS" AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE MECHIO PROJECT OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of the MechIO Project.
 */

package org.mechio.api.motion.blending;

import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;

/**
 * A BlenderOutput implementation which sends Blender movements to a List of
 * Robots.
 * 
 * @author Matthew Stevenson
 */
public class RobotOutput implements BlenderOutput<RobotPositionMap> {
    private Robot myRobot;
        
    public void setRobot(Robot robot){
        myRobot = robot;
    }
    
    public Robot getRobot(){
        return myRobot;
    }
     
    @Override
    public void write(RobotPositionMap positions, long lenMillisec) {
        if(positions == null || positions.isEmpty()){
            return;
        }
        Robot robot = getRobot();
        if(robot == null){
            return;
        }
        robot.move(positions, lenMillisec);
    }

    @Override
    public RobotPositionMap getPositions() {
        Robot robot = getRobot();
        if(robot == null){
            return null;
        }
        return robot.getGoalPositions();
    }
}
