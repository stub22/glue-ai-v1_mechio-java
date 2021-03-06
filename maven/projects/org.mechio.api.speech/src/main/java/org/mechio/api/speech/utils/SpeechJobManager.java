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

import org.mechio.api.speech.SpeechJob;
import org.mechio.api.speech.SpeechRequest;

/**
 *
 * @author matt
 */
public interface SpeechJobManager {
    
    /**
     * Creates a SpeechJob from a SpeechRequest
     * @param req the SpeechRequest
     * @return SpeechJob which represents the speech
     */
    public SpeechJob createSpeechJob(SpeechRequest req);
    
    public void cancelSpeechJob(SpeechJob job);
	
	public String getRequestIdString();
}
