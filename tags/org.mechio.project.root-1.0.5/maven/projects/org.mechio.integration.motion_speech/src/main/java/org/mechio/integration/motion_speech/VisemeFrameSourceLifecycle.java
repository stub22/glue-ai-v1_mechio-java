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
package org.mechio.integration.motion_speech;

import java.util.Map;
import java.util.Properties;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.blending.FrameSource;
import org.mechio.api.speech.SpeechService;
import org.mechio.api.speech.viseme.VisemeBindingManager;
import org.mechio.api.speech.viseme.VisemeEventNotifier;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeFrameSourceLifecycle extends 
        AbstractLifecycleProvider<FrameSource, VisemeFrameSource> {
    private final static String theVisemeNotifier = "speechService";
    private final static String theVisemeManager = "visemeManager";
    private Robot.Id myRobotId;
    private VisemeEventNotifier myNotifier;
    
    public VisemeFrameSourceLifecycle(
            Robot.Id robotId, String speechServiceId){
        super(new DescriptorListBuilder()
                .dependency(theVisemeNotifier, VisemeEventNotifier.class)
                    .with(SpeechService.PROP_ID, speechServiceId)
                .dependency(theVisemeManager, VisemeBindingManager.class)
                    .with(Robot.PROP_ID, robotId.getRobtIdString())
                .getDescriptors());
        if(robotId == null || speechServiceId == null){
            throw new NullPointerException();
        }
        myRobotId = robotId;
        myRegistrationProperties = new Properties();
        myRegistrationProperties.put(
                Robot.PROP_ID, myRobotId);
    }

    @Override
    protected VisemeFrameSource create(Map<String, Object> services) {
        myNotifier = (VisemeEventNotifier)services.get(theVisemeNotifier);
        VisemeBindingManager viseme = 
                (VisemeBindingManager)services.get(theVisemeManager);
        VisemeFrameSource vfs = 
                new VisemeFrameSource(myRobotId, viseme);
        myNotifier.addListener(vfs.getVisemeListener());
        return vfs;
    }

    @Override
    protected void handleChange(
            String serviceId, Object service, Map<String,Object> dependencies) {
        if(myNotifier != null && myService != null){
            myNotifier.removeListener(myService.getVisemeListener());
        }
        if(theVisemeManager.equals(serviceId)){
            if(service == null){
                if(myService != null){
                    myService = null;
                }
                return;
            }
            myService = new VisemeFrameSource(
                    myRobotId, (VisemeBindingManager)service);
        }else if(theVisemeNotifier.equals(serviceId)){
            myNotifier = (VisemeEventNotifier)service;
        }
        if(myNotifier != null && myService != null){
            myNotifier.addListener(myService.getVisemeListener());
        }
    }

    @Override
    public Class<FrameSource> getServiceClass() {
        return FrameSource.class;
    }
    
}
