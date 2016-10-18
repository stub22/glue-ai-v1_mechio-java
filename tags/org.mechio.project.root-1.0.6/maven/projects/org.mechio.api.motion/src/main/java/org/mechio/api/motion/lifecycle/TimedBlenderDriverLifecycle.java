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
import org.mechio.api.motion.blending.Blender;
import org.mechio.api.motion.blending.FrameSourceTracker;
import org.mechio.api.motion.blending.TimedBlenderDriver;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class TimedBlenderDriverLifecycle extends 
        AbstractLifecycleProvider<TimedBlenderDriver, TimedBlenderDriver>{
    private final static String theBlender = "blender";
    private final static String theFrameTracker = "frameTracker";
    private long myBlenderInterval;

    public TimedBlenderDriverLifecycle(
            Robot.Id robotId, long blenderIntervalMillisec){
        super(new DescriptorListBuilder()
                .dependency(theBlender, Blender.class)
                    .with(Robot.PROP_ID, robotId.getRobtIdString())
                    .with(Blender.PROP_POSITION_MAP_TYPE, 
                            Robot.RobotPositionMap.class.getName())
                .dependency(theFrameTracker, FrameSourceTracker.class)
                    .with(Robot.PROP_ID, robotId.getRobtIdString())
                    .with(Blender.PROP_POSITION_MAP_TYPE, 
                            Robot.RobotPositionMap.class.getName())
                .getDescriptors());
        myBlenderInterval = blenderIntervalMillisec;
        if(myRegistrationProperties == null){
            myRegistrationProperties = new Properties();
        }
        myRegistrationProperties.put(Robot.PROP_ID, robotId.getRobtIdString());
    }

    @Override
    protected TimedBlenderDriver create(Map<String, Object> services) {
        Blender blender = (Blender)services.get(theBlender);
        FrameSourceTracker tracker = (FrameSourceTracker)services.get(theFrameTracker);
        TimedBlenderDriver driver = 
                new TimedBlenderDriver(myBlenderInterval);
        driver.setBlender(blender);
        driver.setFrameSourceTracker(tracker);
        driver.start();
        return driver;
    }

    @Override
    protected void handleChange(
            String serviceId, Object service, Map<String,Object> dependencies) {
        if(theBlender.equals(serviceId)){
            myService.setBlender((Blender)service);
        }else if(theFrameTracker.equals(serviceId)){
            myService.setFrameSourceTracker((FrameSourceTracker)service);
        }
    }

    @Override
    public Class<TimedBlenderDriver> getServiceClass() {
        return TimedBlenderDriver.class;
    }
}
