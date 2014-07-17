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

import org.mechio.api.animation.Animation;

/**
 * An Animation with messaging metadata
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface AnimationEvent {
    /**
     * Returns the id String for the source of the AnimationEvent.
     * @return id String for the source of the AnimationEvent
     */
    public String getSourceId();
    /**
     * Returns the id String for the destination of the AnimationEvent.
     * @return id String for the destination of the AnimationEvent
     */
    public String getDestinationId();
    /**
     * Returns the timestamp of the AnimationEvent.
     * @return timestamp of the AnimationEvent
     */
    public Long getCurrentTimeMillisec();
    /**
     * Returns the Animation associated with this AnimationEvent.
     * @return Animation associated with this AnimationEvent
     */
    public Animation getAnimation();
    
    public static interface AnimationEventFactory{
        public AnimationEvent createAnimationEvent(
                String clientId, String hostId, Animation animation);
    }
}
