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
package org.mechio.api.speech.lifecycle;

import java.util.Map;
import java.util.Properties;
import org.jflux.api.core.Listener;
import org.jflux.api.core.Notifier;
import org.jflux.api.messaging.rk.Constants;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speech.SpeechService;
import org.mechio.api.speech.messaging.RemoteSpeechServiceHost;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
public class RemoteSpeechServiceHostLifecycle extends 
        AbstractLifecycleProvider<RemoteSpeechServiceHost, RemoteSpeechServiceHost> {
    private final static String theSpeechService = "speechService";
    private final static String theSpeechRequestNotifier =
            "speechRequestNotifier";
    private final static String theSpeechEventListener = "speechEventListener";
    
    private String myLocalServiceId;
    private String myRemoteServiceId;
    
    public RemoteSpeechServiceHostLifecycle(
            String speechServiceHostId, String remoteId,
            String speechServiceId, String speechRequestNotifierId, 
            String speechEventListenerId){
        super(new DescriptorListBuilder()
                .dependency(theSpeechService, SpeechService.class)
                    .with(SpeechService.PROP_ID, speechServiceId)
                .dependency(theSpeechRequestNotifier, Notifier.class)
                    .with(Constants.PROP_MESSAGE_RECEIVER_ID, speechRequestNotifierId)
                    .with(Constants.PROP_MESSAGE_TYPE, SpeechRequest.class.getName())
                .dependency(theSpeechEventListener, Listener.class)
                    .with(Constants.PROP_MESSAGE_SENDER_ID, speechEventListenerId)
                    .with(Constants.PROP_MESSAGE_TYPE, SpeechEventList.class.getName())
                .getDescriptors());
        if(speechServiceHostId == null || remoteId == null){
            throw new NullPointerException();
        }
        myLocalServiceId = speechServiceHostId;
        myRemoteServiceId = remoteId;
        myRegistrationProperties = new Properties();
        myRegistrationProperties.put(
                RemoteSpeechServiceHost.PROP_ID, myLocalServiceId);
    }

    @Override
    protected RemoteSpeechServiceHost create(Map<String, Object> dependencies) {
        SpeechService service = (SpeechService)dependencies.get(theSpeechService);
        Notifier<SpeechRequest> notifier = (Notifier)dependencies.get(theSpeechRequestNotifier);
        Listener<SpeechEventList<SpeechEvent>> listener = (Listener)dependencies.get(theSpeechEventListener);
        
        return new RemoteSpeechServiceHost(service, notifier, listener);
    }

    @Override
    protected void handleChange(String name, Object dependency, Map<String, Object> availableDependencies) {
        if(theSpeechService.equals(name)) {
            myService.setSpeechService((SpeechService)dependency);
        } else if(theSpeechRequestNotifier.equals(name)) {
            myService.setSpeechRequestNotifier((Notifier)dependency);
        } else if(theSpeechEventListener.equals(name)) {
            myService.setSpeechEventListener((Listener)dependency);
        }
    }

    @Override
    protected Class<RemoteSpeechServiceHost> getServiceClass() {
        return RemoteSpeechServiceHost.class;
    }
    
}
