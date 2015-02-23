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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class Activator implements BundleActivator {
    private final static Logger theLogger = Logger.getLogger(Activator.class.getName());
    private AnimationRobotMonitor myAnimationRobotMonitor;
    
    @Override
    public void start(BundleContext context) throws Exception {
        myAnimationRobotMonitor = new AnimationRobotMonitor(context, null);
        myAnimationRobotMonitor.start();
        theLogger.log(Level.INFO, 
                "AnimationRobotMonitor Registered Successfully.");
        theLogger.log(Level.INFO, 
                "integration.animation_motion Activation Complete.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
