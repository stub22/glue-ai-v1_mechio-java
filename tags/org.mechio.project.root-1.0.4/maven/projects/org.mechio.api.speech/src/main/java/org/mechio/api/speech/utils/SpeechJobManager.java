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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.core.Listener;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.mechio.api.speech.SpeechJob;
import org.mechio.api.speech.SpeechService;

/**
 *
 * @author matt
 */
public class SpeechJobManager {
    private final static Logger theLogger = Logger.getLogger(SpeechJobManager.class.getName());
    
    private SpeechService mySpeechProxy;
    private Map<Long,SpeechJob> mySpeechJobs;
    private Queue<Long> mySpeechJobIds;
    private Long myCurrentSpeechJobId;
    private SpeechEventListener myEventListener;

    public SpeechJobManager(SpeechService speechProxy){
        if(speechProxy == null){
            throw new NullPointerException();
        }
        mySpeechProxy = speechProxy;
        mySpeechJobs = new HashMap<Long, SpeechJob>();
        mySpeechJobIds = new LinkedList<Long>();
        myCurrentSpeechJobId = null;
        myEventListener = new SpeechEventListener();
        mySpeechProxy.addSpeechEventListener(myEventListener);
    }
    
    /**
     * Adds text to the speech queue.
     * @param text text to speak
     * @return SpeechJob which represents the speech
     */
    public synchronized SpeechJob createSpeechJob(String text){
        SpeechJob job = new DefaultSpeechJob(this, text);
        mySpeechJobs.put(job.getSpeechJobId(), job);
        mySpeechJobIds.add(job.getSpeechJobId());
        return job;
    }
    
    void cancelSpeechJob(SpeechJob job){
        if(myCurrentSpeechJobId == null 
                || myCurrentSpeechJobId == job.getSpeechJobId()){
            mySpeechProxy.cancelSpeech();
        }
    }
    
    private final class SpeechEventListener implements Listener<SpeechEventList<SpeechEvent>>{

        @Override
        public synchronized void handleEvent(SpeechEventList<SpeechEvent> t) {
            if(t == null){
                return;
            }
            for(SpeechEvent event : t.getSpeechEvents()){
                handleSingleEvent(event);
            }
        }
        
        private void handleSingleEvent(SpeechEvent t){
            if(t == null){
                return;
            }
            String eventType = t.getEventType();
            if(SpeechEvent.SPEECH_START.equals(eventType)){
                Long id = mySpeechJobIds.poll();
                if(id == null){
                    return;
                }
                SpeechJob job = mySpeechJobs.get(id);
                if(job == null){
                    theLogger.log(Level.WARNING, 
                            "Unable to find SpeechJob with id={0}.  "
                            + "Ignoring SPEECH_START event.", id);
                    return;
                }
                if(DefaultSpeechJob.CANCELED == job.getStatus()){
                    mySpeechProxy.cancelSpeech();
                    return;
                }
                job.setStatus(DefaultSpeechJob.RUNNING);
                if(myCurrentSpeechJobId != null){
                    completeJob(myCurrentSpeechJobId);
                }
                myCurrentSpeechJobId = id;
            }else if(SpeechEvent.SPEECH_END.equals(eventType)){
                if(myCurrentSpeechJobId == null){
                    return;
                }
                SpeechJob job = mySpeechJobs.get(myCurrentSpeechJobId);
                if(job == null){
                    theLogger.log(Level.WARNING, 
                            "Unable to find SpeechJob with id={0}.  "
                            + "Ignoring SPEECH_END event.", myCurrentSpeechJobId);
                    return;
                }
                completeJob(myCurrentSpeechJobId);
            }
        }
        
        private void completeJob(long id){
            SpeechJob job = mySpeechJobs.get(id);
            if(job == null){
                return;
            }
            int status = job.getStatus();
            if(status == DefaultSpeechJob.COMPLETE){
                return;
            }else if(status == DefaultSpeechJob.PENDING 
                    || status == DefaultSpeechJob.RUNNING){
                job.setStatus(DefaultSpeechJob.COMPLETE);
            }
        }
    }
}
