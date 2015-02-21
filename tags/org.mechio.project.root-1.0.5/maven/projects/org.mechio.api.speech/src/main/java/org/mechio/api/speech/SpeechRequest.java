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

/**
 * Defines a request for a SpeechService to speak a phrase.
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface SpeechRequest {
    /**
     * Returns the id String of request's intended SpeechService.
     * @return id String of request's intended SpeechService
     */
    public String getSpeechServiceId();
    /**
     * Returns the id String of the service making the request.
     * @return id String of the service making the request
     */
    public String getRequestSourceId();
    /**
     * Returns the timestamp of the request.
     * @return timestamp of the request
     */
    public Long getTimestampMillisecUTC();
    /**
     * Returns the phrase the SpeechService is being requested to speak.
     * @return phrase the SpeechService is being requested to speak
     */
    public String getPhrase();
}
