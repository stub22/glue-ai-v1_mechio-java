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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.core.Listener;
import org.mechio.api.speech.viseme.VisemeEvent;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeLogger implements Listener<VisemeEvent>{
    private final static Logger theLogger = Logger.getLogger(VisemeLogger.class.getName());
    

    @Override
    public void handleEvent(VisemeEvent event) {
        theLogger.log(Level.INFO, 
                "[Viseme Event] cur: {0}, next: {1}, duration: {2}", 
                new Object[]{
                    event.getCurrentViseme(), 
                    event.getNextViseme(),
                    event.getDuration()});
    }
    
}
