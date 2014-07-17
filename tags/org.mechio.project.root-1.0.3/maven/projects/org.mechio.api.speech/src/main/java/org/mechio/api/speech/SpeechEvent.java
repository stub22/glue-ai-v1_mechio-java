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
 * Defines a common interface for several types of Speech events.
 * Some examples of events are word start and end events, or phoneme events.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface SpeechEvent {
    public final static String SPEECH_START = "SPEECH_START";
    public final static String SPEECH_END = "SPEECH_END";
    public final static String VISEME = "VISEME";
    public final static String WORD_BOUNDARY = "WORD_BOUNDARY";
    public final static String BOOKMARK = "BOOKMARK";

    /**
     * Returns the name of the event of this event.
     * @return name of the event of this event
     */
    public String getEventType();
    /**
     * Returns the stream number for tts output the event originates from.
     * @return stream number for tts output the event originates from
     */
    public Long getStreamNumber();
    /**
     * Returns the position of the speech request the event begins at.
     * @return position of the speech request the event begins at
     */
    public Integer getTextPosition();
    /**
     * Returns the number of characters the event covers.
     * @return number of characters the event covers
     */
    public Integer getTextLength();
    /**
     * Returns event data (usually phone or viseme id) associated with the start
     * of the event.
     * @return event data (usually phone or viseme id) associated with the start
     * of the event
     */
    public Integer getCurrentData();
    /**
     * Returns event data (usually phone or viseme id) associated with the end
     * of the event.
     * @return event data (usually phone or viseme id) associated with the end
     * of the event
     */
    public Integer getNextData();
    /**
     * Returns any String data associated with the event (used for SAPI bookmark
     * events).
     * @return String data associated with the event (used for SAPI bookmark
     * events)
     */
    public String getStringData();
    /**
     * Returns the duration of the event in milliseconds.
     * For word boundaries, this duration for speaking the word in milliseconds.
     * For phonemes and visemes, this is the duration of event in milliseconds. 
     * @return duration of the event in milliseconds
     */
    public Integer getDuration();
}
