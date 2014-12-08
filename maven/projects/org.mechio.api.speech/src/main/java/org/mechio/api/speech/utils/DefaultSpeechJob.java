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

import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.playable.Playable.PlayState;
import org.jflux.api.core.util.DefaultNotifier;
import org.mechio.api.speech.SpeechJob;
import org.mechio.api.speech.SpeechRequest;

/**
 *
 * @author matt
 */
public class DefaultSpeechJob extends DefaultNotifier<PlayState> implements SpeechJob {
    /**
     * Waiting for robot to begin speaking.
     */
    public final static int PENDING = 0;
    /**
     * The robot is speaking.
     */
    public final static int RUNNING = 1;
    /**
     * The robot has finished speaking.
     */
    public final static int COMPLETE = 2;
    /**
     * The speech job has been canceled.
     */
    public final static int CANCELED = 3;
    
    private final long myJobId;
	private final SpeechRequest mySpeechRequest;
    private final long myStartTime;
    private int myStatus;
    
    private SpeechJobManager myManager;
    
    DefaultSpeechJob(SpeechJobManager manager, SpeechRequest speechRequest){
        if(speechRequest == null){
            throw new NullPointerException();
        }
        myJobId = nextId();
        myStartTime = TimeUtils.now();
		mySpeechRequest = speechRequest;
        myStatus = PENDING;
        myManager = manager;
    }
    /**
     * Returns a unique id for this SpeechJob.
     * @return unique id for this SpeechJob
     */
    
	@Override
    public long getSpeechJobId(){
        return myJobId;
    }
    /**
     * Returns the text being spoken.
     * @return text being spoken
     */
    @Override
    public String getSpeechText(){
        return mySpeechRequest.getPhrase();
    }
    /**
     * Returns the speech request for this job.
     * @return speech request for this job
     */
	@Override
	public SpeechRequest getSpeechRequest(){
		return mySpeechRequest;
	}
    /**
     * Returns the time the speech was queued.
     * @return time the speech was queued
     */
    @Override
    public long getStartTime(){
        return myStartTime;
    }
    /**
     * Returns the current status of the speech job.
     * @return current status of the speech job
     */
    @Override
    public int getStatus(){
        return myStatus;
    }
    
    @Override
    public void setStatus(int status){
        myStatus = status;
        if(status == CANCELED && myManager != null){
            myManager.cancelSpeechJob(this);
        }
        notifyListeners(status == PENDING ? PlayState.PENDING : 
                (status == RUNNING ? PlayState.RUNNING :
                (status == COMPLETE ? PlayState.COMPLETED : PlayState.ABORTED)));
    }
    
    /**
     * Stops the speech.
     */
    @Override
    public void cancel(){
        setStatus(CANCELED);
    }
    
    private static long theNextId = 0;
    private static synchronized long nextId(){
        return theNextId++;
    }
}
