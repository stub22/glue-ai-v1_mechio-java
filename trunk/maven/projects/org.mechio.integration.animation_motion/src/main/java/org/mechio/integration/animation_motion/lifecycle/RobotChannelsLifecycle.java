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
package org.mechio.integration.animation_motion.lifecycle;

import java.util.Map;
import java.util.Properties;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.mechio.api.animation.utils.ChannelsParameterSource;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.jointgroup.JointGroup;
import org.mechio.integration.animation_motion.ChannelRobotParameters;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotChannelsLifecycle  extends 
        AbstractLifecycleProvider<ChannelsParameterSource, ChannelRobotParameters>{
    private final static String theRobot = "robot";
    private final static String theJointGroup = "jointGroup";
    

    public RobotChannelsLifecycle(Robot.Id robotId){
        super(new DescriptorListBuilder()
                .dependency(theRobot, Robot.class)
                    .with(Robot.PROP_ID, robotId.getRobtIdString())
                .dependency(theJointGroup, JointGroup.class)
                    .with(Robot.PROP_ID, robotId.getRobtIdString())
                    .optional()
                .getDescriptors());
        if(myRegistrationProperties == null){
            myRegistrationProperties = new Properties();
        }
        myRegistrationProperties.put(Robot.PROP_ID, robotId.getRobtIdString());
    }

    @Override
    protected ChannelRobotParameters create(Map<String, Object> services) {
        Robot robot = (Robot)services.get(theRobot);
        JointGroup group = (JointGroup)services.get(theJointGroup);
        ChannelRobotParameters params = new ChannelRobotParameters();
        params.setRobot(robot);
        params.setJointGroup(group);
        return params;
    }

    @Override
    protected void handleChange(
            String serviceId, Object service, Map<String,Object> dependencies) {
        if(theRobot.equals(serviceId)){
            myService.setRobot((Robot)service);
        }else if(theJointGroup.equals(serviceId)){
            myService.setJointGroup((JointGroup)service);
        }
    }

    @Override
    public Class<ChannelsParameterSource> getServiceClass() {
        return ChannelsParameterSource.class;
    }
}
