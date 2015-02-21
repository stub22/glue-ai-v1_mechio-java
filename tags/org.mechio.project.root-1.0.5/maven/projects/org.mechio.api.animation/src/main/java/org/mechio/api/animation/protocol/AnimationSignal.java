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
package org.mechio.api.animation.protocol;

import java.util.List;

/**
 * An interface for animation signalling events.
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
public interface AnimationSignal {
    public final static String EVENT_START = "START";
    public final static String EVENT_PAUSE = "PAUSE";
    public final static String EVENT_RESUME = "RESUME";
    public final static String EVENT_COMPLETE = "COMPLETED";
    public final static String EVENT_CANCEL = "CANCELED";
    
    public final static String PROP_LOOP = "LOOP";
    public final static String PROP_RAMPING = "RAMPING";
    
    /**
     * Returns the id String for the source of the AnimationSignal.
     * @return id String for the source of the AnimationSignal
     */
    public String getSourceId();
    
    /**
     * Returns the timestamp of the AnimationSignal.
     * @return timestamp of the AnimationSignal
     */
    public Long getTimestampMillisecUTC();
    
    /**
     * Returns a String representing the type of the event.
     * @return String representing the type of the event
     */
    public String getEventType();
    
    /**
     * Returns a String representing the name of the animation.
     * @return String representing the name of the animation
     */
    public String getAnimationName();
    
    /**
     * Returns a String representing the version of the animation.
     * @return String representing the version of the animation
     */
    public String getAnimationVersion();
    
    /**
     * Returns an Integer hash of the animation
     * @return Integer hash of the animation
     */
    public Integer getAnimationHash();
    
    /**
     * Returns the Long length of the animation.
     * @return Long length of the animation
     */
    public Long getAnimationLength();
    
    /**
     * Returns a List of Strings containing the animation properties
     * @return List of Strings containing the animation properties
     */
    public List<String> getAnimationProperties();
    
    public static interface AnimationSignalFactory {
        public AnimationSignal createAnimationSignal(
                String sourceId, String eventType, String animationName,
                String animationVersion, int animationHash,
                long animationLength, List<String> animationProperties);
    }
}
