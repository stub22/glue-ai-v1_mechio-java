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
package org.mechio.api.motion.sync;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ServiceFactory;
import org.mechio.api.motion.Robot;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class SynchronizedRobotFactory 
        implements ServiceFactory<Robot, SynchronizedRobotConfig> {

    @Override
    public VersionProperty getServiceVersion() {
        return SynchronizedRobot.VERSION;
    }

    @Override
    public Robot build(SynchronizedRobotConfig config){
        return new SynchronizedRobot(config);
    }

    @Override
    public Class<Robot> getServiceClass() {
        return Robot.class;
    }

    @Override
    public Class<SynchronizedRobotConfig> getConfigurationClass() {
        return SynchronizedRobotConfig.class;
    }
    
}
