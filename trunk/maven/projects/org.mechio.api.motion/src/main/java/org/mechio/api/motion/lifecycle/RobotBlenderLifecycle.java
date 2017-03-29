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
import org.mechio.api.motion.blending.DefaultBlender;
import org.mechio.api.motion.blending.FrameCombiner;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
@Deprecated
public class RobotBlenderLifecycle extends
        AbstractLifecycleProvider<Blender, DefaultBlender>{
    private final static String theBlenderOutputId = "blenderOutput";
    private final static String theFrameCombinerId = "frameCombiner";

    public RobotBlenderLifecycle(Robot.Id robotId){
        super(new DescriptorListBuilder()
                .dependency(theBlenderOutputId, BlenderOutput.class)
                    .with(Robot.PROP_ID, robotId.getRobtIdString())
                    .with(Blender.PROP_POSITION_MAP_TYPE,
                            Robot.RobotPositionMap.class.getName())
                .dependency(theFrameCombinerId, FrameCombiner.class)
                    .with(Robot.PROP_ID, robotId.getRobtIdString())
                    .with(Blender.PROP_POSITION_MAP_TYPE,
                            Robot.RobotPositionMap.class.getName())
                .getDescriptors());
        if(myRegistrationProperties == null){
            myRegistrationProperties = new Properties();
        }
        myRegistrationProperties.put(Robot.PROP_ID, robotId.getRobtIdString());
        myRegistrationProperties.put(Blender.PROP_POSITION_MAP_TYPE,
                RobotPositionMap.class.getName());
    }

    @Override
    protected DefaultBlender create(Map<String, Object> services) {
        BlenderOutput output = (BlenderOutput)services.get(theBlenderOutputId);
        FrameCombiner combiner =
                (FrameCombiner)services.get(theFrameCombinerId);
        DefaultBlender blender = new DefaultBlender();
        blender.setOutput(output);
        blender.setFrameCombiner(combiner);
        return blender;
    }

    @Override
    protected void handleChange(
            String serviceId, Object service, Map<String,Object> dependencies) {
        if(theBlenderOutputId.equals(serviceId)){
            myService.setOutput((BlenderOutput)service);
        }else if(theFrameCombinerId.equals(serviceId)){
            myService.setFrameCombiner((FrameCombiner)service);
        }
    }

    @Override
    public Class<Blender> getServiceClass() {
        return Blender.class;
    }
}
