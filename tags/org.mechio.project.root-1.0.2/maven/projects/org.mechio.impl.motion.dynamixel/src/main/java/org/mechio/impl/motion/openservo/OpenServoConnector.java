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

package org.mechio.impl.motion.openservo;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ServiceFactory;
import org.mechio.api.motion.servos.ServoController;
import org.mechio.impl.motion.openservo.utils.OpenServoControllerConfig;

/**
 * Connector to a controller for a chain of DynamixelJoints.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class OpenServoConnector implements ServiceFactory<ServoController,OpenServoControllerConfig> {
    @Override
    public ServoController build(OpenServoControllerConfig config) {
        return new OpenServoController(config);
    }

    @Override
    public VersionProperty getServiceVersion() {
        return OpenServoController.VERSION;
    }

    @Override
    public Class<OpenServoControllerConfig> getConfigurationClass() {
        return OpenServoControllerConfig.class;
    }

    @Override
    public Class<ServoController> getServiceClass() {
        return ServoController.class;
    }
}
