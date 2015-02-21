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

import java.util.List;
import org.jflux.api.core.Listener;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.protocol.AnimationSignal;

/**
 * An AnimationPlayer plays an Animation by creating an AnimationJob which 
 * defines how the Animation advances.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface AnimationPlayer {
    public final static String PROP_PLAYER_ID = "animationPlayerId";
    
    public String getAnimationPlayerId();
    /**
     * Creates an AnimationJob which plays the given Animation.
     * @param animation Animation to play
     * @return AnimationJob playing the Animation
     */
    public AnimationJob playAnimation(Animation animation);
    /**
     * Creates an AnimationJob which plays the given Animation.
     * @param animation Animation to play
     * @param start animation start time
     * @param stop animation stop time
     * @return AnimationJob playing the Animation
     */
    public AnimationJob playAnimation(Animation animation, Long start, Long stop);
    /**
     * Returns a List of all uncleared AnimationJobs which have been played by 
     * the AnimationPlayer.
     * @return List of all uncleared AnimationJobs which have been played by 
     * the AnimationPlayer
     */
    public List<AnimationJob> getCurrentAnimations();
    /**
     * Clears an AnimationJob from the List of AnimationJobs from the 
     * AnimationPlayer.
     * @param job AnimationJob to remove
     */
    public void removeAnimationJob(AnimationJob job);
    
    /**
     * Adds a Listener for AnimationSignal events.
     * @param listener Listener for AnimationSignal events
     */
    public void addAnimationSignalListener(Listener<AnimationSignal> listener);
    
    /**
     * Removes a Listener for AnimationSignal events.
     * @param listener Listener for AnimationSignal events
     */
    public void removeAnimationSignalListener(Listener<AnimationSignal> listener);
}
