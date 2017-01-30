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

import org.jflux.api.core.Listener;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class SpeechEventLogger implements Listener<SpeechEventList<SpeechEvent>> {
	private static final Logger theLogger = LoggerFactory.getLogger(SpeechEventLogger.class);

	@Override
	public void handleEvent(SpeechEventList<SpeechEvent> t) {
		for (SpeechEvent e : t.getSpeechEvents()) {
			theLogger.info("[SpeechEvent:{}]\n"
							+ "\tspeech service id: {}, \n"
							+ "\tstream number: {}, \n"
							+ "\ttext position: {}, \n"
							+ "\ttext length: {}, \n"
							+ "\tcurrent data: {}, \n"
							+ "\tnext data: {}, \n"
							+ "\tduration: {}, \n"
							+ "\tstring data: {}",
					e.getEventType(),
					t.getTimestampMillisecUTC(), e.getStreamNumber(),
					e.getTextPosition(), e.getTextLength(),
					e.getCurrentData(), e.getNextData(),
					e.getDuration(), e.getStringData());
		}
	}
}
