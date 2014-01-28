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

package org.mechio.impl.motion.config;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ConfigurationLoader;
import org.mechio.api.motion.servos.config.ServoControllerConfig;

/**
 * Interface for an XML reader for a ServoControllerConfig.
 * @param <T> type of ServoControllerConfig that is read
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class ServoControllerConfigXMLReader<T extends ServoControllerConfig> 
    implements ConfigurationLoader<T, HierarchicalConfiguration> {
    
    @Override
    public abstract VersionProperty getConfigurationFormat();

    @Override
    public abstract T loadConfiguration(HierarchicalConfiguration param) throws Exception;

    @Override
    public abstract Class<T> getConfigurationClass();

    @Override
    public Class<HierarchicalConfiguration> getParameterClass() {
        return HierarchicalConfiguration.class;
    }
    
    
}
