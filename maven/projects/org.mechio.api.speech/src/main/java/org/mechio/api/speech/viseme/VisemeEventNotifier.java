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

import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Listener;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a SpeechEvent to a VisemeEvent and notifies listeners.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeEventNotifier implements Listener<SpeechEventList<SpeechEvent>> {
	private static final Logger theLogger = LoggerFactory.getLogger(VisemeEventNotifier.class);
	private List<Listener<VisemeEvent>> myListeners;
	private long myRemoteStartTime;
	private long myLocalStartTime;
	private long myTimeOffset;

	/**
	 * Creates an empty VisemeEventNotifier.
	 */
	public VisemeEventNotifier() {
		myListeners = new ArrayList<>();
	}

	/**
	 * Adds a VisemeEvent listener to be notified.
	 *
	 * @param listener VisemeEvent listener to add
	 */
	public void addListener(Listener<VisemeEvent> listener) {
		if (!myListeners.contains(listener)) {
			myListeners.add(listener);
		}
	}

	/**
	 * Removes a VisemeEvent listener.
	 *
	 * @param listener VisemeEvent listener to removes
	 */
	public void removeListener(Listener<VisemeEvent> listener) {
		if (myListeners.contains(listener)) {
			myListeners.remove(listener);
		}
	}

	@Override
	public void handleEvent(SpeechEventList<SpeechEvent> eventList) {
		if (eventList == null || eventList.getSpeechEvents().isEmpty()
				|| myListeners.isEmpty()) {
			return;
		}

		for (SpeechEvent event : eventList.getSpeechEvents()) {
			if (event == null || event.getEventType() == null) {
				return;
			}
			if (SpeechEvent.SPEECH_START.equals(event.getEventType())) {
				myLocalStartTime = TimeUtils.now();
//                myRemoteStartTime = eventList.getTimestampMillisecUTC();
				myRemoteStartTime = TimeUtils.now();
				myTimeOffset = myLocalStartTime - myRemoteStartTime;
			} else if (SpeechEvent.VISEME.equals(event.getEventType())) {
				int n = Viseme.values().length;
				Integer cur = event.getCurrentData();
				Integer next = event.getNextData();
				if (cur == null || cur < 0 || cur >= n
						|| next == null || next < 0 || next >= n) {
					theLogger.warn("Received invalid viseme data: currentViseme={}, nextViseme={}",
							event.getCurrentData(),
							event.getNextData());
					return;
				}
				handleVisemeEvent(new DefaultVisemeEvent(
						event, myRemoteStartTime, myTimeOffset));
			}
		}
	}

	/**
	 * Notifies listeners of a VisemeEvent.
	 *
	 * @param event VisemeEvent to send to listeners
	 */
	public void handleVisemeEvent(VisemeEvent event) {
		for (Listener<VisemeEvent> l : myListeners) {
			l.handleEvent(event);
		}
	}

}
