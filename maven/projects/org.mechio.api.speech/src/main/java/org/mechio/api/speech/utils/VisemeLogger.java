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
import org.mechio.api.speech.viseme.VisemeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeLogger implements Listener<VisemeEvent> {
	private static final Logger theLogger = LoggerFactory.getLogger(VisemeLogger.class);


	@Override
	public void handleEvent(VisemeEvent event) {
		theLogger.info("[Viseme Event] cur: {}, next: {}, duration: {}",
				event.getCurrentViseme(),
				event.getNextViseme(),
				event.getDuration());
	}
}
