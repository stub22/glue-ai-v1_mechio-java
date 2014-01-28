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

import java.util.ArrayList;
import java.util.List;
import javax.speech.synthesis.SpeakableEvent;
import javax.speech.synthesis.SpeakableListener;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.util.DefaultNotifier;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.mechio.impl.speech.SpeechEventListRecord;
import org.mechio.impl.speech.SpeechEventRecord;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
public class FreeTTSSpeakableListener extends DefaultNotifier<SpeechEventList<SpeechEvent>> implements SpeakableListener {
    private String mySpeechServiceId;
    
    public FreeTTSSpeakableListener() {
        super();
    }

    @Override
    public void markerReached(SpeakableEvent se) {
//        SpeechEventRecord.Builder eventBuilder = SpeechEventRecord.newBuilder();
//        SpeechEventListRecord.Builder eventListBuilder = SpeechEventListRecord.newBuilder();
//        
//        eventBuilder.setEventType(SpeechEvent.BOOKMARK);
//        eventBuilder.setTextLength(se.getText().length());
//        
//        SpeechEventRecord event = eventBuilder.build();
//        List<SpeechEventRecord> listOfEvents = new ArrayList<SpeechEventRecord>();
//        listOfEvents.add(event);
//        eventListBuilder.setSpeechEvents(listOfEvents);
//        SpeechEventList eventList = eventListBuilder.build();
//        
//        notifyListeners(eventList);
    }

    @Override
    public void speakableCancelled(SpeakableEvent se) {
        SpeechEventRecord.Builder eventBuilder = SpeechEventRecord.newBuilder();
        SpeechEventListRecord.Builder eventListBuilder = SpeechEventListRecord.newBuilder();
        
        eventBuilder.setEventType(SpeechEvent.SPEECH_END);
        eventBuilder.setTextPosition(-1);
        
        if(se.getText() != null) {
            eventBuilder.setTextLength(se.getText().length());
        } else {
            eventBuilder.setTextLength(0);
        }
        
        eventBuilder.setCurrentData(0);
        eventBuilder.setDuration(0);
        eventBuilder.setNextData(0);
        eventBuilder.setStreamNumber(0);
        eventBuilder.setStringData("");
        
        SpeechEventRecord event = eventBuilder.build();
        List<SpeechEventRecord> listOfEvents = new ArrayList<SpeechEventRecord>();
        listOfEvents.add(event);
        eventListBuilder.setSpeechEvents(listOfEvents);
        eventListBuilder.setTimestampMillisecUTC(TimeUtils.now());
        eventListBuilder.setSpeechServiceId(mySpeechServiceId);
        SpeechEventList eventList = eventListBuilder.build();
        
        notifyListeners(eventList);
    }

    @Override
    public void speakableEnded(SpeakableEvent se) {
        SpeechEventRecord.Builder eventBuilder = SpeechEventRecord.newBuilder();
        SpeechEventListRecord.Builder eventListBuilder = SpeechEventListRecord.newBuilder();
        
        eventBuilder.setEventType(SpeechEvent.SPEECH_END);
        eventBuilder.setTextPosition(-1);
        
        if(se.getText() != null) {
            eventBuilder.setTextLength(se.getText().length());
        } else {
            eventBuilder.setTextLength(0);
        }
        
        eventBuilder.setCurrentData(0);
        eventBuilder.setDuration(0);
        eventBuilder.setNextData(0);
        eventBuilder.setStreamNumber(0);
        eventBuilder.setStringData("");
        
        SpeechEventRecord event = eventBuilder.build();
        List<SpeechEventRecord> listOfEvents = new ArrayList<SpeechEventRecord>();
        listOfEvents.add(event);
        eventListBuilder.setSpeechEvents(listOfEvents);
        eventListBuilder.setTimestampMillisecUTC(TimeUtils.now());
        eventListBuilder.setSpeechServiceId(mySpeechServiceId);
        SpeechEventList eventList = eventListBuilder.build();
        
        notifyListeners(eventList);
    }

    @Override
    public void speakablePaused(SpeakableEvent se) {
//        SpeechEventRecord.Builder eventBuilder = SpeechEventRecord.newBuilder();
//        SpeechEventListRecord.Builder eventListBuilder = SpeechEventListRecord.newBuilder();
//        
//        eventBuilder.setEventType(SpeechEvent.SPEECH_END);
//        eventBuilder.setTextPosition(-1);
//        eventBuilder.setTextLength(se.getText().length());
//        
//        SpeechEventRecord event = eventBuilder.build();
//        List<SpeechEventRecord> listOfEvents = new ArrayList<SpeechEventRecord>();
//        listOfEvents.add(event);
//        eventListBuilder.setSpeechEvents(listOfEvents);
//        SpeechEventList eventList = eventListBuilder.build();
//        
//        notifyListeners(eventList);
    }

    @Override
    public void speakableResumed(SpeakableEvent se) {
//        SpeechEventRecord.Builder eventBuilder = SpeechEventRecord.newBuilder();
//        SpeechEventListRecord.Builder eventListBuilder = SpeechEventListRecord.newBuilder();
//        
//        eventBuilder.setEventType(SpeechEvent.SPEECH_START);
//        eventBuilder.setTextPosition(-1);
//        eventBuilder.setTextLength(se.getText().length());
//        
//        SpeechEventRecord event = eventBuilder.build();
//        List<SpeechEventRecord> listOfEvents = new ArrayList<SpeechEventRecord>();
//        listOfEvents.add(event);
//        eventListBuilder.setSpeechEvents(listOfEvents);
//        SpeechEventList eventList = eventListBuilder.build();
//        
//        notifyListeners(eventList);
    }

    @Override
    public void speakableStarted(SpeakableEvent se) {
        SpeechEventRecord.Builder eventBuilder = SpeechEventRecord.newBuilder();
        SpeechEventListRecord.Builder eventListBuilder = SpeechEventListRecord.newBuilder();
        
        eventBuilder.setEventType(SpeechEvent.SPEECH_START);
        eventBuilder.setTextPosition(-1);
        
        if(se.getText() != null) {
            eventBuilder.setTextLength(se.getText().length());
        } else {
            eventBuilder.setTextLength(0);
        }
        
        eventBuilder.setCurrentData(0);
        eventBuilder.setDuration(0);
        eventBuilder.setNextData(0);
        eventBuilder.setStreamNumber(0);
        eventBuilder.setStringData("");
        
        SpeechEventRecord event = eventBuilder.build();
        List<SpeechEventRecord> listOfEvents = new ArrayList<SpeechEventRecord>();
        listOfEvents.add(event);
        eventListBuilder.setSpeechEvents(listOfEvents);
        eventListBuilder.setTimestampMillisecUTC(TimeUtils.now());
        eventListBuilder.setSpeechServiceId(mySpeechServiceId);
        SpeechEventList eventList = eventListBuilder.build();
        
        notifyListeners(eventList);
    }

    @Override
    public void topOfQueue(SpeakableEvent se) {
    }

    @Override
    public void wordStarted(SpeakableEvent se) {
        SpeechEventRecord.Builder eventBuilder = SpeechEventRecord.newBuilder();
        SpeechEventListRecord.Builder eventListBuilder = SpeechEventListRecord.newBuilder();
        
        eventBuilder.setEventType(SpeechEvent.WORD_BOUNDARY);
        eventBuilder.setTextPosition(se.getWordStart());
        eventBuilder.setTextLength(se.getText().length());
        
        eventBuilder.setCurrentData(0);
        eventBuilder.setDuration(0);
        eventBuilder.setNextData(0);
        eventBuilder.setStreamNumber(0);
        eventBuilder.setStringData("");
        
        SpeechEventRecord event = eventBuilder.build();
        List<SpeechEventRecord> listOfEvents = new ArrayList<SpeechEventRecord>();
        listOfEvents.add(event);
        eventListBuilder.setSpeechEvents(listOfEvents);
        eventListBuilder.setTimestampMillisecUTC(TimeUtils.now());
        eventListBuilder.setSpeechServiceId(mySpeechServiceId);
        SpeechEventList eventList = eventListBuilder.build();
        
        notifyListeners(eventList);
    }
    
    public void setSpeechServiceId(String id) {
        mySpeechServiceId = id;
    }
}
