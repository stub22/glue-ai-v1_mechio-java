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

package org.mechio.api.speech;

import org.jflux.api.core.Listener;

/**
 * The SpeechService provides a basic interface for an service providing 
 * Text-to-Speech or equivalent capabilities.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface SpeechService {
    /**
     * Property name for the SpeechService Id.
     */
    public final static String PROP_ID = "speechServiceId";
    
    /**
     * Returns the SpeechService Id.
     * @return SpeechService Id
     */
    public String getSpeechServiceId();
    /**
     * Starts the SpeechService, making it ready to speak.
     * @throws Exception 
     */
    public void start() throws Exception;
    /**
     * Sends the SpeechService text to speak.
     * @param text the text to speak
     */
    public SpeechJob speak(String text);
    
    public void cancelSpeech();
    /**
     * Closes a SpeechService, leaving it unable to speak.
     */
    public void stop();
    
    /**
     * Adds a Listener to be notified when a speech request is made.
     * @param listener the Listener to be notified
     */
    public void addRequestListener(Listener<SpeechRequest> listener);
    /**
     * Removes a Listener from being notified when a speech request is made.
     * @param listener the Listener to remove
     */
    public void removeRequestListener(Listener<SpeechRequest> listener);
        
    /**
     * Adds a Listener to be notified when a speech event occurs.
     * @param listener the Listener to be notified
     */
    public void addSpeechEventListener(Listener<SpeechEventList<SpeechEvent>> listener);
    /**
     * Removes a Listener from being notified when a speech event occurs.
     * @param listener the Listener to remove
     */
    public void removeSpeechEventListener(Listener<SpeechEventList<SpeechEvent>> listener);
}
