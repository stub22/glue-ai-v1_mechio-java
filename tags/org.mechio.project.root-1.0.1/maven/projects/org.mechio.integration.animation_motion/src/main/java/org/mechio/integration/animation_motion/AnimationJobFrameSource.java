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

package org.mechio.integration.animation_motion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.jflux.api.common.rk.playable.AbstractPlayable;
import org.jflux.api.common.rk.playable.PlayState;
import org.jflux.api.common.rk.playable.Playable;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.services.addon.ServiceAddOn;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.Channel;
import org.mechio.api.animation.compiled.CompiledMap;
import org.mechio.api.animation.compiled.CompiledPath;
import org.mechio.api.animation.player.AnimationJob;
import org.mechio.api.animation.player.AnimationJobListener;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.protocol.AnimationSignal;
import org.mechio.api.animation.protocol.AnimationSignal.AnimationSignalFactory;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.protocol.MotionFrame;
import org.mechio.api.motion.blending.FrameSource;
import org.mechio.api.motion.protocol.DefaultMotionFrame;

/**
 * An implementation of an AnimationJob which expects to be queried for new
 * positions as a FrameSource.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationJobFrameSource extends AbstractPlayable implements 
        AnimationJob, FrameSource<RobotPositionMap> {
    private final static Logger theLogger = Logger.getLogger(AnimationJobFrameSource.class.getName());
    private RobotPositionMap myPreviousPositions;
    private Animation myAnimation;
    private CompiledMap myAnimationMap;
    private List<AnimationJobListener> myAnimationListeners;
    private AnimationPlayer mySource;
    private Robot.Id myRobotId;
    private boolean myLoopFlag;
    private AnimationSignalFactory mySignalFactory;

    AnimationJobFrameSource(AnimationPlayer source, Robot.Id robotId, 
            Animation anim, long stepLength, Long start, Long stop){
        if(robotId == null){
            throw new NullPointerException();
        }if(anim == null){
            throw new NullPointerException();
        }if(stepLength <= 0){
            throw new IllegalArgumentException("stepLength must be greater than zero");
        }
        myLoopFlag = false;
        myRobotId = robotId;
        myAnimationListeners = new ArrayList();
        mySource = source;
        myAnimation = anim;
        long s = start == null ? -1 : start;
        long e = stop == null ? -1 : stop;
        myAnimationMap = anim.compileMap(s, e, stepLength);
        List<Channel> channels = myAnimation.getChannels();
        myPreviousPositions = new Robot.RobotPositionHashMap(channels.size());
        for(Channel c : channels){
            if(c == null){
                continue;
            }
            CompiledPath path = myAnimationMap.get(c.getId());
            if(path == null){
                continue;
            }
            Integer id = c.getId();
            double v = path.estimatePosition(0);
            if(id == null || !NormalizedDouble.isValid(v)){
                continue;
            }
            NormalizedDouble val = new NormalizedDouble(v);
            Joint.Id jId = new Joint.Id(id);
            Robot.JointId djId = new Robot.JointId(myRobotId, jId);
            myPreviousPositions.put(djId, val);
        }        
    }

    /**
     * Sets the AnimationPlayer which created the AnimationJobFrameSource.
     * @param source AnimationPlayer which created the AnimationJobFrameSource
     */
    public void setSource(AnimationPlayer source){
        mySource = source;
    }
    
    @Override
    public AnimationPlayer getSource() {
        return mySource;
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
        Long s = myAnimationMap.getStartTime();
        if(s == null){
            return l;
        }
        return l+s;
    }
    
    @Override
    public Map<Integer,Double> advanceAnimation(long time, long interval){
        MotionFrame frame = getMovements(time, interval);
        if(frame == null){
            return null;
        }
        return frame.getGoalPositions();
    }
    
    @Override
    public MotionFrame getMovements(long time, long interval) {
        if(myPlayState != PlayState.RUNNING){
            return null;
        }
        MotionFrame frame = new DefaultMotionFrame();
        frame.setTimestampMillisecUTC(time);
        frame.setFrameLengthMillisec(interval);
        myPreviousPositions.clear();
        long cur = getCurrentTime(time);
        for(Entry<Integer,CompiledPath> e : myAnimationMap.entrySet()){
            CompiledPath path = e.getValue();
            Integer id = e.getKey();
            double pos = path.estimatePosition(cur);
            if(id == null || !NormalizedDouble.isValid(pos)){
                continue;
            }
            NormalizedDouble val = new NormalizedDouble(pos);
            Joint.Id jId = new Joint.Id(id);
            Robot.JointId djId = 
                    new Robot.JointId(myRobotId, jId);
            myPreviousPositions.put(djId, val);
        }
        frame.setPreviousPositions(myPreviousPositions);
        myPreviousPositions.clear();
        cur = getCurrentTime(time+interval);
        for(Entry<Integer,CompiledPath> e : myAnimationMap.entrySet()){
            CompiledPath path = e.getValue();
            Integer id = e.getKey();
            double pos = path.estimatePosition(cur);
            if(id == null || !NormalizedDouble.isValid(pos)){
                continue;
            }
            NormalizedDouble val = new NormalizedDouble(pos);
            Joint.Id jId = new Joint.Id(id);
            Robot.JointId djId = 
                    new Robot.JointId(myRobotId, jId);
            myPreviousPositions.put(djId, val);
        }
        frame.setGoalPositions(myPreviousPositions);
        if(isComplete(cur)){
            complete(time);
        }
        advance(time);
        return frame;
    }

    private boolean isComplete(long currentTime){
        for(Entry<Integer,CompiledPath> e : myAnimationMap.entrySet()){
            CompiledPath path = e.getValue();
            if(currentTime < path.getEndTime()){
                return false;
            }
        }
        return true;
    }
    
    private void advance(long time){
        if(myAnimationListeners == null){
            return;
        }
        for(AnimationJobListener listener : myAnimationListeners){
            listener.animationAdvanced(getCurrentTime(time));
        }
    }

    @Override
    public boolean onStart(long time) {
        for(ServiceAddOn<Playable> addon : myAnimation.getAddOns()){
            Playable p = addon.getAddOn();
            p.start(time);
        }
        if(myAnimationListeners != null){
            for(AnimationJobListener listener : myAnimationListeners){
                listener.animationStart(myAnimationMap.getStartTime(), myAnimationMap.getEndTime());
            }
        }
        
        if(mySource != null && mySource instanceof DefaultAnimationPlayer
                && mySignalFactory != null) {
            List<String> props = new ArrayList<String>();
        
            if(myLoopFlag) {
                props.add(AnimationSignal.PROP_LOOP);
            }
            
            AnimationSignal signal = mySignalFactory.createAnimationSignal(
                    mySource.getAnimationPlayerId(),
                    AnimationSignal.EVENT_START,
                    myAnimation.getVersion().getName(),
                    myAnimation.getVersion().getNumber(),
                    myAnimation.hashCode(), myAnimation.getLength(), props);
            
            ((DefaultAnimationPlayer)mySource).notifyListeners(signal);
        }
            
        return true;
    }
    
    @Override
    public Long getAnimationLength(){
        return myAnimationMap.getEndTime() - myAnimationMap.getStartTime();
    }
    
    @Override
    public Long getRemainingTime(long time){
        long now = getElapsedPlayTime(time);
        long end = getAnimationLength();
        return end - now;
    }

    @Override
    protected boolean onPause(long time) {
        for(ServiceAddOn<Playable> addon : myAnimation.getAddOns()){
            Playable p = addon.getAddOn();
            p.pause(time);
        }
        
        if(mySource != null && mySource instanceof DefaultAnimationPlayer
                && mySignalFactory != null) {
            List<String> props = new ArrayList<String>();
        
            if(myLoopFlag) {
                props.add(AnimationSignal.PROP_LOOP);
            }
        
            AnimationSignal signal = mySignalFactory.createAnimationSignal(
                    mySource.getAnimationPlayerId(),
                    AnimationSignal.EVENT_PAUSE,
                    myAnimation.getVersion().getName(),
                    myAnimation.getVersion().getNumber(),
                    myAnimation.hashCode(), myAnimation.getLength(), props);
            
            ((DefaultAnimationPlayer)mySource).notifyListeners(signal);
        }
        return true;
    }
    @Override
    protected boolean onResume(long time) {
        for(ServiceAddOn<Playable> addon : myAnimation.getAddOns()){
            Playable p = addon.getAddOn();
            p.resume(time);
        }
        
        if(mySource != null && mySource instanceof DefaultAnimationPlayer) {
            List<String> props = new ArrayList<String>();
        
            if(myLoopFlag) {
                props.add(AnimationSignal.PROP_LOOP);
            }
        
            AnimationSignal signal = mySignalFactory.createAnimationSignal(
                    mySource.getAnimationPlayerId(),
                    AnimationSignal.EVENT_RESUME,
                    myAnimation.getVersion().getName(),
                    myAnimation.getVersion().getNumber(),
                    myAnimation.hashCode(), myAnimation.getLength(), props);
            
            ((DefaultAnimationPlayer)mySource).notifyListeners(signal);
        }
        
        return true;
    }
    @Override
    protected boolean onStop(long time) {
        for(ServiceAddOn<Playable> addon : myAnimation.getAddOns()){
            Playable p = addon.getAddOn();
            p.stop(time);
        }
        
        if(mySource != null && mySource instanceof DefaultAnimationPlayer
                && mySignalFactory != null) {
            List<String> props = new ArrayList<String>();
        
            if(myLoopFlag) {
                props.add(AnimationSignal.PROP_LOOP);
            }
        
            AnimationSignal signal = mySignalFactory.createAnimationSignal(
                    mySource.getAnimationPlayerId(),
                    AnimationSignal.EVENT_CANCEL,
                    myAnimation.getVersion().getName(),
                    myAnimation.getVersion().getNumber(),
                    myAnimation.hashCode(), myAnimation.getLength(), props);
            
            ((DefaultAnimationPlayer)mySource).notifyListeners(signal);
        }
        
        return true;
    }
    @Override
    protected boolean onComplete(long time) {
        for(ServiceAddOn<Playable> addon : myAnimation.getAddOns()){
            Playable p = addon.getAddOn();
            p.complete(time);
        }
        
        if(mySource != null && mySource instanceof DefaultAnimationPlayer
                && mySignalFactory != null) {
            List<String> props = new ArrayList<String>();
        
            if(myLoopFlag) {
                props.add(AnimationSignal.PROP_LOOP);
            }
        
            AnimationSignal signal = mySignalFactory.createAnimationSignal(
                    mySource.getAnimationPlayerId(),
                    AnimationSignal.EVENT_COMPLETE,
                    myAnimation.getVersion().getName(),
                    myAnimation.getVersion().getNumber(),
                    myAnimation.hashCode(), myAnimation.getLength(), props);
            
            ((DefaultAnimationPlayer)mySource).notifyListeners(signal);
        }
        
        return true;
    }
    
    @Override
    protected void afterComplete(long time) {
        if(myLoopFlag){
            start(time);
        }
    }

    @Override
    public void setLoop(boolean loop) {
        myLoopFlag = loop;
    }

    @Override
    public boolean getLoop() {
        return myLoopFlag;
    }
    
    public void setAnimationSignalFactory(AnimationSignalFactory factory) {
        mySignalFactory = factory;
    }
}
