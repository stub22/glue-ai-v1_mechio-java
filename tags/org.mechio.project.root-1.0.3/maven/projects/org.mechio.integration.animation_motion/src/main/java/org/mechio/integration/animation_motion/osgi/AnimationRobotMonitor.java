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

package org.mechio.integration.animation_motion.osgi;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.osgi.ServiceClassListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.mechio.api.motion.Robot;
import org.mechio.integration.animation_motion.AnimationMotionUtils;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationRobotMonitor extends ServiceClassListener<Robot>{
    private final static Logger theLogger = Logger.getLogger(AnimationRobotMonitor.class.getName());
    private Map<Robot,AnimationServices> myRegistrationMap;
    private BundleContext myContext;
    public final static String REPLICATION_DESTINATION_ID =
            "replicationDestinationId";
    public final static String REPLICATION_CONNECTION_ID =
            "replicationConnectionId";
    public final static String REPLICATION_SESSION_ID =
            "replicationSessionId";
    public final static String GROUP_TYPE = "ReplicationReceiverGroup";
    private final static String theIdFormat = "%s/" + GROUP_TYPE + "/%s";
    
    public AnimationRobotMonitor(BundleContext context, String filter){
        super(Robot.class, context, filter);
        if(context == null){
            throw new NullPointerException();
        }
        
        myContext = context;
        myRegistrationMap = new HashMap<Robot, AnimationServices>();
    }
    
    @Override
    protected void addService(Robot t) {       
        AnimationServices regs =
                AnimationMotionUtils.registerAnimationEditingComponents(
                        myContext, t);
        if(regs == null){
            return;
        }
        
        myRegistrationMap.put(t, regs);
    }

    @Override
    protected void removeService(Robot t) {
        AnimationServices regs = myRegistrationMap.remove(t);
        if(regs == null){
            return;
        }
        regs.stopAll();
    }
    
    private static String id(String groupId, String part){
        return String.format(theIdFormat, groupId, part);
    }
}
