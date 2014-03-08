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
package org.mechio.api.speech.viseme.lifecycle;

import java.util.Map;
import java.util.Properties;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.mechio.api.speech.viseme.VisemeBindingManager;
import org.mechio.api.speech.viseme.config.VisemeBindingManagerConfig;
import org.mechio.api.speech.viseme.config.VisemeBindingManagerFactory;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeBindingManagerLifecycle extends 
        AbstractLifecycleProvider<VisemeBindingManager, VisemeBindingManager> {
    private final static String theVisemeConfig = "visemeManagerConfig";
    /**
     * Creates a new lifecycle provider for a VisemeEventNotifier which uses
     * the given SpeechService.
     * @param speechServiceId SpeechService for the VisemeEventNotifier to use
     */
    public VisemeBindingManagerLifecycle(Properties registrationProps){
        super(new DescriptorListBuilder()
                .dependency(theVisemeConfig, VisemeBindingManagerConfig.class) 
                    .with(registrationProps)
                .getDescriptors());
        myRegistrationProperties = new Properties();
        myRegistrationProperties.putAll(registrationProps);
    }

    @Override
    protected VisemeBindingManager create(Map<String, Object> services) {
        VisemeBindingManagerConfig conf = 
                (VisemeBindingManagerConfig)services.get(theVisemeConfig);
        return VisemeBindingManagerFactory.buildManager(conf);
    }
    
    @Override
    protected void handleChange(
            String serviceId, Object service, Map<String,Object> dependencies) {
        if(service == null){
            if(myService != null){
                myService = null;
            }
            return;
        }
        myService = VisemeBindingManagerFactory.buildManager(
                (VisemeBindingManagerConfig)service);
    }

    @Override
    public Class<VisemeBindingManager> getServiceClass() {
        return VisemeBindingManager.class;
    }
    
}
