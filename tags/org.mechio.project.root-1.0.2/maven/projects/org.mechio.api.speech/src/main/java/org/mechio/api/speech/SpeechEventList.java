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

import java.util.List;

/**
 * Contains a List of SpeechEvents and metadata about the event origin.
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface SpeechEventList<T extends SpeechEvent> {
    /**
     * Returns the id String of the SpeechService the events originated from.
     * @return id String of the SpeechService the events originated from
     */
    public String getSpeechServiceId();
    /**
     * Returns the timestamp of the events.
     * @return timestamp of the events
     */
    public Long getTimestampMillisecUTC();
    /**
     * Returns the List of SpeechEvents.
     * @return List of SpeechEvents
     */
    public List<T> getSpeechEvents();
}
