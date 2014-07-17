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
package org.mechio.api.speech.messaging;

import org.jflux.api.core.Listener;
import org.jflux.api.core.Notifier;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speech.SpeechService;

/**
 *
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
public class RemoteSpeechServiceHost implements Listener<SpeechRequest> {
    private SpeechService mySpeechService;
    private Listener<SpeechEventList<SpeechEvent>> mySpeechEventSender;
    private Notifier<SpeechRequest> mySpeechRequestReceiver;
    
    public final static String PROP_ID = "speechServiceHostId";
    
    public RemoteSpeechServiceHost() {
        mySpeechService = null;
        mySpeechEventSender = null;
        mySpeechRequestReceiver = null;
    }
    
    public RemoteSpeechServiceHost(
            SpeechService service,
            Listener<SpeechEventList<SpeechEvent>> sender,
            Notifier<SpeechRequest> receiver) {
        mySpeechService = service;
        mySpeechEventSender = sender;
        mySpeechRequestReceiver = receiver;
        
        if(mySpeechRequestReceiver != null && mySpeechService != null) {
            mySpeechRequestReceiver.addListener(this);
        }
        
        if(mySpeechEventSender != null) {
            mySpeechService.addSpeechEventListener(sender);
        }
    }
    
    public void setSpeechService(SpeechService service) {
        if(mySpeechEventSender != null && mySpeechService != null) {
            mySpeechService.removeSpeechEventListener(mySpeechEventSender);
        }
        
        mySpeechService = service;
        
        if(mySpeechEventSender != null && mySpeechService != null) {
            mySpeechService.addSpeechEventListener(mySpeechEventSender);
        }
    }
    
    public void setSpeechRequestReceiver(Notifier<SpeechRequest> receiver) {
        if(mySpeechRequestReceiver != null) {
            mySpeechRequestReceiver.removeListener(this);
        }
        
        mySpeechRequestReceiver = receiver;
        
        if(mySpeechRequestReceiver != null) {
            mySpeechRequestReceiver.addListener(this);
        }
    }
    
    public void setSpeechEventSender(
            Listener<SpeechEventList<SpeechEvent>> sender) {
        if(mySpeechEventSender != null && mySpeechService != null) {
            mySpeechService.removeSpeechEventListener(mySpeechEventSender);
        }
        
        mySpeechEventSender = sender;
        
        if(mySpeechEventSender != null && mySpeechService != null) {
            mySpeechService.addSpeechEventListener(mySpeechEventSender);
        }
    }

    @Override
    public void handleEvent(SpeechRequest t) {
        if(mySpeechService != null) {
            mySpeechService.speak(t.getPhrase());
        }
    }
}
