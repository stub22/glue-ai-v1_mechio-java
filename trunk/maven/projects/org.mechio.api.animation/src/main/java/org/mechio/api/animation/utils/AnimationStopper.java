package org.mechio.api.animation.utils;

/**
 * Receives a ServiceCommandRecord to stop animations.
 *
 * @author Ben Jenkins <benjenkinsv95@gmail.com>
 */
public interface AnimationStopper {

    /**
     * Stop's all animations in the system.
     */
    public void stopAll();

}
