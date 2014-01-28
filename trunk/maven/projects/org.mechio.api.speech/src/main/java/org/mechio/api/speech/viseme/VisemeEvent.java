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

/**
 * SpeechEvent with Visemes from a SpeechService, representing the
 * visual information corresponding to current Speech from the SpeechService.
 * Used to synchronize mouth movements (or other output) with speech.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface VisemeEvent {
    /**
     * Returns the speech Stream identifier.
     * @return speech Stream identifier
     */
    public long getStream();
    /**
     * Returns the current Viseme at the time of the event.
     * @return current Viseme at the time of the event
     */
    public Viseme getCurrentViseme();
    /**
     * Returns the Viseme to transition to.
     * @return Viseme to transition to
     */
    public Viseme getNextViseme();
    /**
     * Returns the number of milliseconds for the transition to the next Viseme.
     * @return number of milliseconds for the transition to the next Viseme
     */
    public int getDuration();
    /**
     * Returns the timestamp of the VisemeEvent.
     * @return timestamp of the VisemeEvent
     */
    public long getTimestampMillisecUTC();
}
