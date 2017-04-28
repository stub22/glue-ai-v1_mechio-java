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

import org.jflux.api.core.Notifier;
import org.jflux.api.core.playable.Playable.PlayState;


/**
 *
 * @author matt
 */
//TODO: Implement Playable
public interface SpeechJob extends Notifier<PlayState>{
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
	
    public long getSpeechJobId();
    public String getSpeechText();
	public SpeechRequest getSpeechRequest();
    public long getStartTime();
    public int getStatus();
    void setStatus(int status);
    public void cancel();
}
