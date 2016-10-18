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
import org.mechio.api.motion.Robot.Id;
import org.mechio.api.motion.Robot.JointId;

/**
 * Config for a RobotJointGroup which identifies the Robot the JointGroup should
 * use.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotJointGroupConfig extends 
        DefaultJointGroupConfig<JointId, RobotJointGroupConfig> {
    private Id myRobotId;
    /**
     * Creates an empty RobotJointGroupConfig.
     * @param name JointGroup name
     * @param robotId robot for the JointGroup to use
     */
    public RobotJointGroupConfig(String name, Id robotId){
       super(name); 
       if(robotId == null){
           throw new NullPointerException();
       }
       myRobotId = robotId;
    }
    /**
     * Creates a RobotJointGroupConfig with the given JointIds and sub-groups.
     * @param name JointGroup name
     * @param robotId robot for the JointGroup to use
     * @param ids JointIds belonging to the JointGroup
     * @param groups sub-groups of the JointGroup
     */
    public RobotJointGroupConfig(String name, Id robotId,
            List<? extends JointId> ids, 
            List<? extends RobotJointGroupConfig> groups){
        super(name, ids, groups);
        if(robotId == null){
            throw new NullPointerException();
        }
        myRobotId = robotId;
    }
    /**
     * Returns the Robot Id to be used by the JointGroup.
     * @return Robot Id to be used by the JointGroup
     */
    public Id getRobotId(){
        return myRobotId;
    }
}
