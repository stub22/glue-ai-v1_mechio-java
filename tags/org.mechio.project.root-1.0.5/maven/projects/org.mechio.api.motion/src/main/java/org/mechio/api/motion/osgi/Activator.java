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

package org.mechio.api.motion.osgi;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.common.rk.services.ServiceUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.mechio.api.motion.servos.utils.ServoRobotConnector;
import org.mechio.api.motion.sync.SynchronizedRobotFactory;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class Activator implements BundleActivator {
    private final static Logger theLogger = Logger.getLogger(Activator.class.getName());

    /**
     * 
     * @param context
     * @throws Exception
     */
    @Override
    public void start(BundleContext context) throws Exception {
        theLogger.log(Level.INFO, "org.mechio.api.motion Activation Begin.");
        ServiceUtils.registerFactory(context, new ServoRobotConnector());
        ServiceUtils.registerFactory(
                context, new SynchronizedRobotFactory());
    }
    
    /**
     * 
     * @param context
     * @throws Exception
     */
    @Override
    public void stop(BundleContext context) throws Exception {}

}
