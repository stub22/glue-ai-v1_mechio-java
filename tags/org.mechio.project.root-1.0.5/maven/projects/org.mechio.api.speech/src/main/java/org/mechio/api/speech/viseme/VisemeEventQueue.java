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

import java.util.Comparator;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.jflux.api.core.Listener;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeEventQueue {
    private SortedSet<VisemeEvent> myEvents;
    private VisemeListener myListener;
    
    public VisemeEventQueue(){
        myEvents = new ConcurrentSkipListSet<VisemeEvent>(new VisemeEventComparator());
        myListener = new VisemeListener();
    }
    
    public Listener<VisemeEvent> getListener(){
        return myListener;
    }
    
    public synchronized void clear(){
        myEvents.clear();
    }
    
    public VisemeEvent getEvent(long timeUTC){
        if(myEvents.isEmpty()){
            return null;
        }
        VisemeEvent ev = myEvents.first();
        if(timeUTC <= ev.getTimestampMillisecUTC() + ev.getDuration()){
            return ev;
        }
        myEvents.remove(ev);
        if(myEvents.isEmpty()){
            return ev;
        }
        return myEvents.first();
    }
    
    class VisemeEventComparator implements Comparator<VisemeEvent> {
        @Override
        public int compare(VisemeEvent o1, VisemeEvent o2) {
            if(o1 == null){
                return -1;
            }else if(o2 == null){
                return 1;
            }
            long t1 = o1.getTimestampMillisecUTC();
            long t2 = o2.getTimestampMillisecUTC();
            return t1 > t2 ? 1 : 
                    (t1 < t2 ? -1 : 0);
        }
    }
    
    class VisemeListener implements Listener<VisemeEvent>{
        @Override
        public void handleEvent(VisemeEvent event) {
            myEvents.add(event);
        }
    }
}
