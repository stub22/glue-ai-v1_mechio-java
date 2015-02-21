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
import org.mechio.api.speech.SpeechService;
import org.mechio.api.speech.viseme.VisemeEventNotifier;

/**
 * Lifecycle provider for a VisemeEventNotifier.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeEventNotifierLifecycle extends AbstractLifecycleProvider<VisemeEventNotifier, VisemeEventNotifier> {
    private final static String theSpeechService = "speechService";
    /**
     * Creates a new lifecycle provider for a VisemeEventNotifier which uses
     * the given SpeechService.
     * @param speechServiceId SpeechService for the VisemeEventNotifier to use
     */
    public VisemeEventNotifierLifecycle(String speechServiceId){
        super(new DescriptorListBuilder()
                .dependency(theSpeechService, SpeechService.class)
                    .with(SpeechService.PROP_ID, speechServiceId)
                .getDescriptors());
        
        myRegistrationProperties = new Properties();
        myRegistrationProperties.put(
                SpeechService.PROP_ID, speechServiceId);
    }

    @Override
    protected VisemeEventNotifier create(Map<String, Object> services) {
        SpeechService speech = (SpeechService)services.get(theSpeechService);
        VisemeEventNotifier ven = new VisemeEventNotifier();
        speech.addSpeechEventListener(ven);
        return ven;
    }
    
    @Override
    protected void handleChange(
            String serviceId, Object service, Map<String,Object> dependencies) {
    }

    @Override
    public Class<VisemeEventNotifier> getServiceClass() {
        return VisemeEventNotifier.class;
    }
    
}
