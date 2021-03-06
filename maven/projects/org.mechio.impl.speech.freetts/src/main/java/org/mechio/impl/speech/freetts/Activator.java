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

package org.mechio.impl.speech.freetts;

import org.jflux.api.core.Listener;
import org.jflux.impl.messaging.rk.config.RKMessagingConfigUtils;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceFactory;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.mechio.api.speech.SpeechConfig;
import org.mechio.api.speech.SpeechService;
import org.mechio.impl.speech.RemoteSpeechUtils;
import org.mechio.impl.speech.SpeechConfigRecord;

public class Activator implements BundleActivator {
    public final static String CONNECTION_CONFIG_ID =
            "speechServiceConnectionConfig";
    
    @Override
    public void start(BundleContext context) throws Exception {       
        FreeTTSSpeechService service = new FreeTTSSpeechService();
        SpeechConfigRecord.Builder configBuilder =
                SpeechConfigRecord.newBuilder();
        
        configBuilder.setConfigSourceId("activator");
        configBuilder.setSampleRate(16000);
        configBuilder.setSpeechServiceId("FreeTTS");
        configBuilder.setVoiceName("kevin");
        
        SpeechConfig config = configBuilder.build();
        service.initialize(config);
        
        ManagedServiceFactory fact = new OSGiComponentFactory(context);
        
        RKMessagingConfigUtils.registerConnectionConfig(
                CONNECTION_CONFIG_ID, "127.0.0.1", null, fact);
        
        SimpleLifecycle<SpeechService> lc =
                new SimpleLifecycle<SpeechService>(
                        service, SpeechService.class,
                        "speechServiceId", "speechService", null);
        OSGiComponent component = new OSGiComponent(context, lc);
        component.start();
        
        RemoteSpeechUtils.connectHost(
                fact, "speechService", "speech", CONNECTION_CONFIG_ID);
        
//        Listener evListener = new EventListener();
//        service.addSpeechEventListener(evListener);
//        
//        service.speak("Hello, world.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO add deactivation code here
    }

    private static class EventListener implements Listener<SpeechEventList<SpeechEvent>> {

        @Override
        public void handleEvent(SpeechEventList<SpeechEvent> t) {
            for(SpeechEvent event: t.getSpeechEvents()) {
                System.out.println(event.getEventType());
            }
        }
        
    }
}
