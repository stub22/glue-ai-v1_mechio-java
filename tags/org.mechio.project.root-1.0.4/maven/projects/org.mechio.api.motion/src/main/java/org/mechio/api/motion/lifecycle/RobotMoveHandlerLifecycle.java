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
import org.jflux.api.common.rk.utils.ListenerConstants;
import org.jflux.api.core.Listener;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.blending.Blender;
import org.mechio.api.motion.blending.FrameSource;
import org.mechio.api.motion.messaging.RobotMoveHandler;
import org.mechio.api.motion.protocol.MotionFrameEvent;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotMoveHandlerLifecycle extends
        AbstractLifecycleProvider<Listener, RobotMoveHandler> {
    
    private final static String theRobot = "robot";    
    
    public RobotMoveHandlerLifecycle(String moveHandlerId, Robot.Id robotId){
        super(new DescriptorListBuilder()
                .dependency(theRobot, Robot.class)
                    .with(Robot.PROP_ID, robotId.getRobtIdString())
                .getDescriptors());
        if(moveHandlerId == null){
            throw new NullPointerException();
        }
        myRegistrationProperties = new Properties();
        myRegistrationProperties.put(Robot.PROP_ID, robotId.getRobtIdString());
        myRegistrationProperties.put(ListenerConstants.PROP_LISTENER_ID, moveHandlerId);
        myRegistrationProperties.put(ListenerConstants.PROP_LISTENER_TYPE, 
                MotionFrameEvent.class.getName());
        myRegistrationProperties.put(Blender.PROP_POSITION_MAP_TYPE, 
                Robot.RobotPositionMap.class.getName());
        
        myServiceClassNames = new String[]{
            Listener.class.getName(),
            FrameSource.class.getName()
        };
    }
    
    @Override
    protected RobotMoveHandler create(Map<String, Object> dependencies) {
        Robot robot = (Robot)dependencies.get(theRobot);
        return new RobotMoveHandler(robot);
    }

    @Override
    protected void handleChange(String name, Object dependency, 
            Map<String, Object> availableDependencies) {
        if(myService == null){
            return;
        }
        if(theRobot.equals(name)){
            myService.setRobot((Robot)dependency);
        }
    }

    @Override
    public Class<Listener> getServiceClass() {
        return Listener.class;
    }
    
}
