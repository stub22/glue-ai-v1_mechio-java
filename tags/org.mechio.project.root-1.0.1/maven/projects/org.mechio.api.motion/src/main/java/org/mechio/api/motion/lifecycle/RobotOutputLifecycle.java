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
package org.mechio.api.motion.lifecycle;

import java.util.Map;
import java.util.Properties;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.blending.Blender;
import org.mechio.api.motion.blending.BlenderOutput;
import org.mechio.api.motion.blending.RobotOutput;

/**
 * ServiceLifecycleProvider for a RobotOutput
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotOutputLifecycle extends 
        AbstractLifecycleProvider<BlenderOutput, RobotOutput>{
    private final static String theRobot = "robot";
    

    public RobotOutputLifecycle(Robot.Id robotId){
        super(new DescriptorListBuilder()
                .dependency(theRobot, Robot.class)
                    .with(Robot.PROP_ID, robotId.getRobtIdString())
                .getDescriptors());
        if(myRegistrationProperties == null){
            myRegistrationProperties = new Properties();
        }
        myRegistrationProperties.put(Robot.PROP_ID, robotId.getRobtIdString());
        myRegistrationProperties.put(Blender.PROP_POSITION_MAP_TYPE, 
                RobotPositionMap.class.getName());
    }

    @Override
    protected RobotOutput create(Map<String, Object> services) {
        Robot robot = (Robot)services.get(theRobot);
        RobotOutput ro = new RobotOutput();
        ro.setRobot(robot);
        return ro;
    }

    @Override
    protected void handleChange(
            String serviceId, Object service, Map<String,Object> dependencies) {
        if(theRobot.equals(serviceId)){
            myService.setRobot((Robot)service);
        }
    }

    @Override
    public Class<BlenderOutput> getServiceClass() {
        return BlenderOutput.class;
    }
}
