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
package org.mechio.api.speech.viseme;

import org.mechio.api.speech.SpeechEvent;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DefaultVisemeEvent implements VisemeEvent{
    private SpeechEvent mySpeechEvent;
    private long myTimestampMillisecUTC;
    
    /**
     * Creates a DefaultVisemeEvent from the given SpeechEvent.
     * @param speechEvent SpeechEvent with Viseme information
     */
    public DefaultVisemeEvent(
            SpeechEvent speechEvent, long timestampMillisecUTC){
        if(speechEvent == null){
            throw new NullPointerException();
        }
        mySpeechEvent = speechEvent;
        myTimestampMillisecUTC = timestampMillisecUTC;
    }
    
    public DefaultVisemeEvent(
            SpeechEvent speechEvent, long timestampMillisecUTC, long timeOffset){
        if(speechEvent == null){
            throw new NullPointerException();
        }
        mySpeechEvent = speechEvent;
        myTimestampMillisecUTC = timestampMillisecUTC + timeOffset;
    }

    @Override
    public long getStream() {
        return mySpeechEvent.getStreamNumber();
    }

    @Override
    public Viseme getCurrentViseme() {
        return Viseme.getById(mySpeechEvent.getCurrentData());
    }

    @Override
    public Viseme getNextViseme() {
        return Viseme.getById(mySpeechEvent.getNextData());
    }

    @Override
    public int getDuration() {
        return mySpeechEvent.getDuration();
    }
    
    @Override
    public long getTimestampMillisecUTC(){
        return myTimestampMillisecUTC;
    }
}
