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

package org.mechio.api.motion.jointgroup;

import java.util.List;
import org.jflux.api.common.rk.config.VersionProperty;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot.JointId;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotJointGroup extends 
        AbstractJointGroup<JointId, RobotJointGroup, Joint> {
    /**
     * Service version name.
     */
    public final static String CONFIG_TYPE = "RobotJointGroup";
    /**
     * Service version number.
     */
    public final static String CONFIG_VERSION = "1.0";
    /**
     * Service VersionProperty.
     */
    public final static VersionProperty VERSION = 
            new VersionProperty(CONFIG_TYPE, CONFIG_VERSION);
    
    private Robot myRobot;

    /**
     * 
     * @param name
     * @param ids
     * @param groups
     */
    public RobotJointGroup(String name, 
            List<JointId> ids, List<RobotJointGroup> groups){
        super(name, ids, groups);
    }
    
    public void setRobot(Robot robot){
        Robot old = myRobot;
        setRobotQuite(robot);
        firePropertyChange(PROP_STRUCTURE_CHANGED, old, myRobot);
    }
    
    public Robot getRobot(){
        return myRobot;
    }
    
    private void setRobotQuite(Robot robot){
        if(myRobot == robot){
            return;
        }
        myRobot = robot;
        for(RobotJointGroup group : myGroups){
            group.setRobotQuite(robot);
        }
    }
    
    @Override
    protected Joint getJointById(JointId jointId) {
        if(myRobot == null){
            return null;
        }
        return myRobot.getJoint(jointId);
    }
}
