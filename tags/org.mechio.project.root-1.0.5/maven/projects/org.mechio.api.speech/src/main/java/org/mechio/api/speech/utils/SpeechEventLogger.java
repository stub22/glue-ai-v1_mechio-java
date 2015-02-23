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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.core.Listener;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class SpeechEventLogger implements Listener<SpeechEventList<SpeechEvent>>{
    private final static Logger theLogger = Logger.getLogger(SpeechEventLogger.class.getName());

    @Override
    public void handleEvent(SpeechEventList<SpeechEvent> t) {
        for(SpeechEvent e: t.getSpeechEvents()) {
            theLogger.log(Level.INFO, "[SpeechEvent:{0}]\n"
                    + "\tspeech service id: {1}, \n"
                    + "\tstream number: {2}, \n"
                    + "\ttext position: {3}, \n"
                    + "\ttext length: {4}, \n"
                    + "\tcurrent data: {5}, \n"
                    + "\tnext data: {6}, \n"
                    + "\tduration: {7}, \n"
                    + "\tstring data: {8}", 
                    new Object[]{e.getEventType(),
                        t.getTimestampMillisecUTC(), e.getStreamNumber(),
                        e.getTextPosition(), e.getTextLength(),
                        e.getCurrentData(), e.getNextData(),
                        e.getDuration(), e.getStringData()});
        }
    }
}
