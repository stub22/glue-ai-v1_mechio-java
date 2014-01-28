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
package org.mechio.api.animation.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jflux.api.common.rk.playable.AbstractPlayable;
import org.jflux.api.common.rk.playable.PlayState;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.player.AnimationJob;
import org.mechio.api.animation.player.AnimationJobListener;
import org.mechio.api.animation.player.AnimationPlayer;

/**
 *
 * @author matt
 */
public class RemoteAnimationJob extends AbstractPlayable implements AnimationJob {
    private RemoteAnimationPlayerClient myPlayer;
    private Animation myAnimation;
    private long myMaxRampTimeMillisec;
    private boolean myLoopFlag;
    private long myAnimStartTime;
    private long myEndTime;
    private List<AnimationJobListener> myAnimationListeners;

    RemoteAnimationJob(RemoteAnimationPlayerClient player, Animation anim, Long start, Long stop, int maxRampMillisec){
        if(anim == null || player == null){
            throw new NullPointerException();
        }
        myPlayer = player;
        myLoopFlag = false;
        myAnimation = anim;
        myMaxRampTimeMillisec = maxRampMillisec;
        myAnimStartTime = start == null || start < 0 ? 0 : start;
        myEndTime = stop == null || stop < 0 ? anim.getLength() : stop;
        myAnimationListeners = new ArrayList<AnimationJobListener>();
    }
    @Override
    public AnimationPlayer getSource() {
        return myPlayer;
    }
    
    @Override
    public void addAnimationListener(AnimationJobListener listener) {
        if(myAnimationListeners == null){
            myAnimationListeners = new ArrayList<AnimationJobListener>();
        }
        if(!myAnimationListeners.contains(listener)){
            myAnimationListeners.add(listener);
        }
    }
    
    @Override
    public void removeAnimationListener(AnimationJobListener listener) {
        if(myAnimationListeners == null){
            return;
        }
        myAnimationListeners.remove(listener);
    }
    
    @Override
    public Animation getAnimation(){
        return myAnimation;
    }
    
    public Long getCurrentTime(long time){
        Long l = getElapsedPlayTime(time);
        if(l == null){
            return null;
        }
        return l+myAnimStartTime;
    }
    
    @Override
    public Map<Integer,Double> advanceAnimation(long time, long interval){
        return null;
    }

    @Override
    public boolean onStart(long time) {
        if(myAnimationListeners != null){
            for(AnimationJobListener listener : myAnimationListeners){
                listener.animationStart(myAnimStartTime, myEndTime);
            }
        }
        return true;
    }
    
    @Override
    public Long getAnimationLength(){
        return myEndTime - myAnimStartTime  + myMaxRampTimeMillisec;
    }
    
    @Override
    public Long getRemainingTime(long time){
        if(getPlayState() == PlayState.COMPLETED){
            return 0L;
        }
        long now = getElapsedPlayTime(time);
        long end = getAnimationLength();
        return end - now;
    }

    @Override
    protected boolean onPause(long time) {
        return true;
    }
    @Override
    protected boolean onResume(long time) {
        return true;
    }
    @Override
    protected boolean onStop(long time) {
        myPlayer.stopAnimation(myAnimation);
        return true;
    }
    @Override
    protected boolean onComplete(long time) {
        return true;
    }

    @Override
    public void setLoop(boolean loop) {
        if(loop){
            myPlayer.loopAnimation(myAnimation, false);
        }else if(myPlayState == PlayState.RUNNING){
            myPlayer.playAnimation(myAnimation, false);
        }
        myLoopFlag = loop;
    }

    @Override
    public boolean getLoop() {
        return myLoopFlag;
    }
}
