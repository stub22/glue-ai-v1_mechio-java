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
package org.mechio.api.speech.viseme.config;

import java.util.List;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ServiceFactory;
import org.mechio.api.speech.viseme.VisemeBinding;
import org.mechio.api.speech.viseme.VisemeBindingManager;
import org.mechio.api.speech.viseme.VisemePosition;

/**
 * ServiceFactory for creating a VisemeBindingManager from a 
 * VisemeBindingManagerConfig.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeBindingManagerFactory implements 
        ServiceFactory<VisemeBindingManager, VisemeBindingManagerConfig> {
    /**
     * Creates a new VisemeBindingManager from the given configuration.
     * @param config configuration for the VisemeBindingManager
     * @return new VisemeBindingManager from the given configuration
     */
    public static VisemeBindingManager buildManager(
            VisemeBindingManagerConfig config){
        if(config == null){
            throw new NullPointerException();
        }
        VisemeBindingManager manager = new VisemeBindingManager();
        List<VisemeBindingConfig> configs = config.getVisemeBindings();
        if(configs == null){
            throw new NullPointerException();
        }
        for(VisemeBindingConfig bindingConfig : configs){
            manager.addBinding(buildVisemeBinding(bindingConfig));
        }
        return manager;
    }
    
    /**
     * Creates a new VisemeBinding from the given configuration.
     * @param config configuration for the VisemeBinding
     * @return new VisemeBinding from the given configuration
     */
    protected static VisemeBinding buildVisemeBinding(VisemeBindingConfig<VisemePosition> config){
        if(config == null){
            throw new NullPointerException();
        }
        VisemeBinding vb = new VisemeBinding(config);
        return vb;
    }

    @Override
    public VersionProperty getServiceVersion() {
        return VisemeBindingManager.VERSION;
    }

    @Override
    public VisemeBindingManager build(VisemeBindingManagerConfig config) {
        return buildManager(config);
    }

    @Override
    public Class<VisemeBindingManager> getServiceClass() {
        return VisemeBindingManager.class;
    }

    @Override
    public Class<VisemeBindingManagerConfig> getConfigurationClass() {
        return VisemeBindingManagerConfig.class;
    }
}
