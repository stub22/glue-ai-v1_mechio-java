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
package org.mechio.api.speech.utils;

import java.util.ArrayList;
import java.util.List;
import org.jflux.api.core.Listener;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;

/**
 * A SpeechEventNotifier receives SpeechEventList Messages and notifies 
 * listeners of the individual SpeechEvents in the SpeechEventList so 
 * SpeechEvents can be handled individually.
 * @author Matthew Stevenson <www.mechio.org>
 */
public class SpeechEventNotifier implements Listener<SpeechEventList<SpeechEvent>>{
    private List<Listener<SpeechEventList<SpeechEvent>>> mySpeechEventListeners;
    
    /**
     * Creates a new SpeechEventNotifier.
     */
    public SpeechEventNotifier(){
        mySpeechEventListeners = new ArrayList<Listener<SpeechEventList<SpeechEvent>>>();
    }
    
    @Override
    public void handleEvent(SpeechEventList<SpeechEvent> t) {
        if(t == null || t.getSpeechEvents() == null){
            throw new NullPointerException();
        }

        fireSpeechEvent(t);
    }
    
    /**
     * Notifies listeners of a SpeechEvent.
     * @param ev SpeechEvent to send to listeners
     */
    public void fireSpeechEvent(SpeechEventList<SpeechEvent> ev){
        for(Listener<SpeechEventList<SpeechEvent>> listener: mySpeechEventListeners){
            listener.handleEvent(ev);
        }
    }
    
    /**
     * Adds a SpeechEvent listener.
     * @param listener SpeechEvent listener to add
     */
    public void addSpeechEventListener(Listener<SpeechEventList<SpeechEvent>> listener){
        if(listener == null){
            throw new NullPointerException();
        }
        if(!mySpeechEventListeners.contains(listener)){
            mySpeechEventListeners.add(listener);
        }
    }
    
    /**
     * Removes a SpeechEvent listener.
     * @param listener SpeechEvent listener to remove
     */
    public void removeSpeechEventListener(Listener<SpeechEventList<SpeechEvent>> listener){
        if(listener == null){
            throw new NullPointerException();
        }
        if(mySpeechEventListeners.contains(listener)){
            mySpeechEventListeners.remove(listener);
        }
    }
    
}
