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
package org.mechio.api.motion.servos.config;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.osgi.lifecycle.ConfiguredServiceParams;
import org.mechio.api.motion.servos.ServoController;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ServoControllerContext<Param> {
    private ConfiguredServiceParams<ServoController,?,Param> myServiceParams;
    
    public ServoControllerContext(Class<Param> paramClass, Param param,
            VersionProperty controllerVersion, VersionProperty configFormat){
        if(paramClass == null || param == null || 
                controllerVersion == null || configFormat == null){
            throw new NullPointerException();
        }
        myServiceParams = new ConfiguredServiceParams(
                ServoController.class, null, paramClass, 
                null, param, null, controllerVersion, configFormat);
    }
}
