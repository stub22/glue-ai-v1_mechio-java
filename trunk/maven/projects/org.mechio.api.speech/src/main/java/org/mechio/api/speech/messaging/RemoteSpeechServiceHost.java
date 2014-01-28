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
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speech.SpeechService;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
public class RemoteSpeechServiceHost implements Listener<SpeechRequest> {
    private SpeechService mySpeechService;
    private Notifier<SpeechRequest> mySpeechRequestNotifier;
    private Listener<SpeechEventList<SpeechEvent>> mySpeechEventListener;
    
    public final static String PROP_ID = "speechServiceHostId";
    
    public RemoteSpeechServiceHost() {
        mySpeechService = null;
        mySpeechRequestNotifier = null;
        mySpeechEventListener = null;
    }
    
    public RemoteSpeechServiceHost(
            SpeechService service, Notifier<SpeechRequest> notifier,
            Listener<SpeechEventList<SpeechEvent>> listener) {
        mySpeechService = service;
        mySpeechRequestNotifier = notifier;
        mySpeechEventListener = listener;
        
        if(mySpeechEventListener != null && mySpeechService != null) {
            mySpeechService.addSpeechEventListener(mySpeechEventListener);
        }
        
        if(mySpeechRequestNotifier != null) {
            mySpeechRequestNotifier.addListener(this);
        }
    }
    
    public void setSpeechService(SpeechService service) {
        if(mySpeechEventListener != null && mySpeechService != null) {
            mySpeechService.removeSpeechEventListener(mySpeechEventListener);
        }
        
        mySpeechService = service;
        
        if(mySpeechEventListener != null && mySpeechService != null) {
            mySpeechService.addSpeechEventListener(mySpeechEventListener);
        }
    }
    
    public void setSpeechRequestNotifier(Notifier<SpeechRequest> notifier) {
        if(mySpeechRequestNotifier != null) {
            mySpeechRequestNotifier.removeListener(this);
        }
        
        mySpeechRequestNotifier = notifier;
        
        if(mySpeechRequestNotifier != null) {
            mySpeechRequestNotifier.addListener(this);
        }
    }
    
    public void setSpeechEventListener(
            Listener<SpeechEventList<SpeechEvent>> listener) {
        if(mySpeechEventListener != null && mySpeechService != null) {
            mySpeechService.removeSpeechEventListener(mySpeechEventListener);
        }
        
        mySpeechEventListener = listener;
        
        if(mySpeechEventListener != null && mySpeechService != null) {
            mySpeechService.addSpeechEventListener(mySpeechEventListener);
        }
    }

    @Override
    public void handleEvent(SpeechRequest t) {
        if(mySpeechService != null) {
            mySpeechService.speak(t.getPhrase());
        }
    }
}
