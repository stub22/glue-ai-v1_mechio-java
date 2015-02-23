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
import java.util.Map;
import java.util.UUID;
import org.jflux.api.core.Listener;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.mechio.api.speech.SpeechJob;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speech.SpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author matt
 */
public class SpeechJobManagerImpl implements SpeechJobManager {
    private final static Logger theLogger = LoggerFactory.getLogger(SpeechJobManagerImpl.class);
	
    private SpeechService mySpeechProxy;
    private Map<Long,SpeechJob> mySpeechJobs;
	private SpeechJob myActiveSpeechJob;
	private Map<Long,SpeechJob> myPendingJobs;
    private SpeechEventListener myEventListener;

    public SpeechJobManagerImpl(SpeechService speechProxy){
        if(speechProxy == null){
            throw new NullPointerException();
        }
        mySpeechProxy = speechProxy;
        mySpeechJobs = new HashMap<Long, SpeechJob>();
        myPendingJobs = new HashMap<Long, SpeechJob>();
        myEventListener = new SpeechEventListener();
        mySpeechProxy.addSpeechEventListener(myEventListener);
    }
    
    /**
     * Creates a SpeechJob from a SpeechRequest
     * @param req the SpeechRequest
     * @return SpeechJob which represents the speech
     */
	@Override
    public synchronized SpeechJob createSpeechJob(SpeechRequest req){
        SpeechJob job = new DefaultSpeechJob(this, req);
		theLogger.debug("Created new speech job with id: {}.", getRequestId(job));
        mySpeechJobs.put(getRequestId(job), job);
		myPendingJobs.put(getRequestId(job), job);
        return job;
    }
    
	@Override
    public void cancelSpeechJob(SpeechJob job){
		theLogger.info("Attempting to cancel SpeechJob: {}", getRequestId(job));
        if(myActiveSpeechJob == job){
			theLogger.info("SpeechJob {} is active.  Cancelling current speech output.", getRequestId(job));
            mySpeechProxy.cancelSpeech();
			myActiveSpeechJob = null;
        }
    }
	
	@Override
	public String getRequestIdString(){
		Integer id = (int)UUID.randomUUID().getMostSignificantBits();
		while(mySpeechJobs.containsKey((long)id)){
			theLogger.error("PLEASE REPORT THIS ERROR: Collision in speech job id: {}. Discarding and regenerating.", id);
			id = (int)UUID.randomUUID().getMostSignificantBits();
		}
		theLogger.debug("Generated new speech job id: {}.", id);
		return id.toString();
	}
	
	private long getRequestId(SpeechJob job){
		return getRequestIdLong(job.getSpeechRequest().getRequestSourceId());
	}
	
	private long getRequestIdLong(String reqIdStr){
		return Long.parseLong(reqIdStr);
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
			if(!t.getEventType().equals(SpeechEvent.VISEME)){
				theLogger.info("SpeechEvent: {}, id: {}, pos: {}, len: {}, cur: {}, next: {}, str: {}, dur: {}", 
						t.getEventType(), t.getStreamNumber(), t.getTextPosition(), t.getTextLength(), 
							t.getCurrentData(), t.getNextData(), t.getStringData(), t.getDuration());
			}
			long reqId = t.getStreamNumber();
			if(myActiveSpeechJob != null && getRequestId(myActiveSpeechJob) == reqId){
				theLogger.debug("SpeechEvent matches active job. Updating status for event type {}.", t.getEventType());
				updateActiveJobStatus(t);
				return;
			}
			if(myActiveSpeechJob != null){
				//New speech request id means a new job is already active
				theLogger.warn("SpeechEvent does not match active job. Found event id: {}, active job id: {}. "
						+ "This should not happen, SPEECH_END expected for active job before any new events. "
						+ "Assuming SPEECH_END was dropped and marking active job complete.", 
						t.getStreamNumber(), getRequestId(myActiveSpeechJob));
				completeActiveJob();
			}
			if(!myPendingJobs.containsKey(reqId)){
				//SpeechEvent for a request from an outside source, ignore it.
				theLogger.debug("Unable to find SpeechJob with id={}. Ignoring SpeechEvent of type={}.", 
						reqId, t.getEventType());
				return;
			}
			theLogger.debug("Found SpeechEvent for pending job: {}.  Setting job active.", reqId);
			myActiveSpeechJob = myPendingJobs.remove(reqId);
			if(myActiveSpeechJob.getStatus() == DefaultSpeechJob.PENDING){
				theLogger.debug("Changing status for job: {}, from PENDING to RUNNING.", reqId);
				myActiveSpeechJob.setStatus(DefaultSpeechJob.RUNNING);
			}
			updateActiveJobStatus(t);
        }
		
		private void updateActiveJobStatus(SpeechEvent t){
            String eventType = t.getEventType();
			if(DefaultSpeechJob.CANCELED == myActiveSpeechJob.getStatus() 
					&& !SpeechEvent.SPEECH_END.equals(eventType)){
				theLogger.info("Active job: {} has status CANCELED, cancelling speech output.", getRequestId(myActiveSpeechJob));
				cancelSpeechJob(myActiveSpeechJob);
			}else if(SpeechEvent.SPEECH_END.equals(eventType)){
				theLogger.debug("SPEECH_END found, completing active job: {}.", getRequestId(myActiveSpeechJob));
				completeActiveJob();
			}
		}
		
		private void completeActiveJob(){
			if(myActiveSpeechJob == null){
				return;
			}
			if(myActiveSpeechJob.getStatus() != DefaultSpeechJob.CANCELED
					&& myActiveSpeechJob.getStatus() != DefaultSpeechJob.COMPLETE){
				theLogger.debug("Changing status for job: {}, from {} to COMPLETE.", 
						getRequestId(myActiveSpeechJob), myActiveSpeechJob.getStatus());
				myActiveSpeechJob.setStatus(DefaultSpeechJob.COMPLETE);
			}
			theLogger.debug("Clearing active job: {}.", getRequestId(myActiveSpeechJob));
			myActiveSpeechJob = null;
		}
    }
}
