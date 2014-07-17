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

package org.mechio.api.animation.player;

import java.util.Map;
import org.jflux.api.common.rk.playable.Playable;
import org.mechio.api.animation.Animation;

/**
 * An AnimationJob is able to play an animation for some rendering system.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface AnimationJob extends Playable {
    /**
     * Adds an AnimationListener to be notified as the Animation advances.
     * @param listener AnimationListener to add
     */
    public void addAnimationListener(AnimationJobListener listener);
    /**
     * Removes an AnimationListener from listening to the Animation.
     * @param listener AnimationListener to remove
     */
    public void removeAnimationListener(AnimationJobListener listener);

    /**
     * Returns the Animation this AnimationJob is playing.
     * @return Animation this AnimationJob is playing
     */
    public Animation getAnimation();
    /**
     * Returns the length of the Animation being played in milliseconds.
     * @return length of the Animation being played in milliseconds
     */
    public Long getAnimationLength();
    /**
     * Returns the time remaining in the Animation at the given time in 
     * milliseconds.
     * @param time current time
     * @return time remaining in the Animation at the given time in milliseconds
     */
    public Long getRemainingTime(long time);

    /**
     * Advances the Animation to the given time.
     * @param time current time
     * @param interval preferred time between advancements
     * @return resulting Animation goal positions
     */
    public Map<Integer,Double> advanceAnimation(long time, long interval);
    /**
     * Returns the AnimationPlayer which created this AnimationJob.  This is
     * used especially with OSGi to remove AnimationJobs from the Service 
     * Registry.
     * @return AnimationPlayer which created this AnimationJob
     */
    public AnimationPlayer getSource();
    
    /**
     * If set true, the AnimationJob will loop, replaying after finishing.
     * @param loop looping status
     */
    public void setLoop(boolean loop);
    /**
     * Returns the looping status of the AnimationJob.
     * @return looping status of the AnimationJob
     */
    public boolean getLoop();
}
