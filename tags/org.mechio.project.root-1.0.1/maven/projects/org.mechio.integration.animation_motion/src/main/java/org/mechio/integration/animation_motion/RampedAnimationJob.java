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
import org.jflux.api.common.rk.utils.Utils;
import org.osgi.framework.BundleContext;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.MotionPath;
import org.mechio.api.animation.compiled.CompiledMap;
import org.mechio.api.animation.compiled.CompiledPath;
import org.mechio.api.animation.player.AnimationJob;
import org.mechio.api.animation.player.AnimationJobListener;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.protocol.AnimationSignal;
import org.mechio.api.animation.protocol.AnimationSignal.AnimationSignalFactory;
import org.mechio.api.interpolation.InterpolatorFactory;
import org.mechio.api.interpolation.linear.LinearInterpolatorFactory;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.protocol.MotionFrame;
import org.mechio.api.motion.blending.FrameSource;
import org.mechio.api.motion.protocol.DefaultMotionFrame;
import org.mechio.api.motion.utils.RobotUtils;

/** 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RampedAnimationJob extends AbstractPlayable implements 
        AnimationJob, FrameSource<RobotPositionMap> {
    private final static Logger theLogger = Logger.getLogger(RampedAnimationJob.class.getName());
    private BundleContext myContext;
    private RobotPositionMap myPreviousPositions;
    private Animation myAnimation;
    private CompiledMap myRampedAnimationMap;
    private CompiledMap myAnimationMap;
    private List<AnimationJobListener> myAnimationListeners;
    private AnimationPlayer mySource;
    private Robot.Id myRobotId;
    private long myStepLengthMillisec;
    private long myRampTimeMillisec;
    private long myMaxRampTimeMillisec;
    private boolean myLoopFlag;
    private AnimationSignalFactory mySignalFactory;

    RampedAnimationJob(
            BundleContext context, AnimationPlayer source, Robot.Id robotId, 
            Animation anim, long stepLength, Long start, Long stop,
            int maxRampMillisec){
        if(context == null || robotId == null || anim == null){
            throw new NullPointerException();
        }if(stepLength <= 0){
            throw new IllegalArgumentException(
                    "stepLength must be greater than zero");
        }
        myContext = context;
        myLoopFlag = false;
        myRobotId = robotId;
        myAnimationListeners = new ArrayList();
        mySource = source;
        myAnimation = anim;
        myStepLengthMillisec = stepLength;
        myMaxRampTimeMillisec = maxRampMillisec;
        long s = start == null ? -1 : start;
        long e = stop == null ? -1 : stop;
        myAnimationMap = anim.compileMap(s, e, myStepLengthMillisec);
    }
    
    private void addRamping(Map<Integer,Double> curPos){
        myRampedAnimationMap = (CompiledMap)myAnimationMap.clone();
        double maxDif = findMaxDif(curPos);
        maxDif = Utils.bound(maxDif, 0.0, 1.0);
        myRampTimeMillisec = (int)(maxDif*myMaxRampTimeMillisec);
        InterpolatorFactory linearFact = new LinearInterpolatorFactory();
        for(Entry<Integer,CompiledPath> e : myRampedAnimationMap.entrySet()){
            CompiledPath path = e.getValue();
            int id = e.getKey();
            double start = getStart(path);
            if(start == -1){
                continue;
            }
            Double cur = curPos.get(id);
            if(cur == null){
                continue;
            }
            MotionPath rampPath = new MotionPath(linearFact);
            rampPath.addPoint(0.0, cur);
            rampPath.addPoint(myRampTimeMillisec, start);
            CompiledPath ramp = rampPath.compilePath(
                    0, myRampTimeMillisec, myStepLengthMillisec);
            path.addAll(0, ramp);
        }
        if(myRampTimeMillisec > 0){
            myRampedAnimationMap.setTimes(
                    myRampedAnimationMap.getStartTime(), 
                    myRampedAnimationMap.getEndTime()+myRampTimeMillisec, 
                    true);
        }
    }
    
    private double findMaxDif(Map<Integer,Double> currentPos){
        double maxDif = 0;
        for(Entry<Integer,CompiledPath> e : myAnimationMap.entrySet()){
            CompiledPath path = e.getValue();
            int id = e.getKey();
            double start = getStart(path);
            if(start == -1){
                continue;
            }
            Double cur = currentPos.get(id);
            if(cur == null){
                continue;
            }
            double dif = Math.abs(cur - start);
            if(dif > maxDif){
                maxDif = dif;
            }
        }
        return maxDif;
    }
    
    private double getStart(CompiledPath path){
        for(Double d : path){
            if(d == null || d == -1){
                continue;
            }
            return d;
        }
        return -1;
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
        Long l = getElapsedPlayTime(time) + myRampTimeMillisec;
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
        for(Entry<Integer,CompiledPath> e : myRampedAnimationMap.entrySet()){
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
        long next = getCurrentTime(time+interval);
        for(Entry<Integer,CompiledPath> e : myRampedAnimationMap.entrySet()){
            CompiledPath path = e.getValue();
            Integer id = e.getKey();
            double pos = path.estimatePosition(next);
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
        long unrampedTime = cur - myRampTimeMillisec;
        if(isComplete(unrampedTime)){
            complete(time);
        }
        advance(unrampedTime);
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

    private void advance(long elapsed){
        if(myAnimationListeners == null){
            return;
        }
        for(AnimationJobListener listener : myAnimationListeners){
            listener.animationAdvanced(elapsed);
        }
    }

    @Override
    public boolean start(long time) {
        myPreviousPositions = 
                RobotUtils.getCurrentPositions(myContext, myRobotId);
        addRamping(RobotUtils.convertMap(myPreviousPositions));
        return super.start(time + myRampTimeMillisec);
    }
    
    @Override
    public boolean onStart(long time) {
        for(ServiceAddOn<Playable> addon : myAnimation.getAddOns()){
            Playable p = addon.getAddOn();
            p.start(time);
        }
        if(myAnimationListeners != null){
            for(AnimationJobListener listener : myAnimationListeners){
                listener.animationStart(myRampedAnimationMap.getStartTime(), 
                        myRampedAnimationMap.getEndTime());
            }
        }
        
        if(mySource != null && mySource instanceof RampedAnimationPlayer
                && mySignalFactory != null) {
            List<String> props = new ArrayList<String>();
            props.add(AnimationSignal.PROP_RAMPING);
        
            if(myLoopFlag) {
                props.add(AnimationSignal.PROP_LOOP);
            }
        
            AnimationSignal signal = mySignalFactory.createAnimationSignal(
                    mySource.getAnimationPlayerId(),
                    AnimationSignal.EVENT_START,
                    myAnimation.getVersion().getName(),
                    myAnimation.getVersion().getNumber(),
                    myAnimation.hashCode(), myAnimation.getLength(), props);
            
            ((RampedAnimationPlayer)mySource).notifyListeners(signal);
        }
        
        return true;
    }
    
    @Override
    public Long getAnimationLength(){
        long time = myAnimationMap.getEndTime() - myAnimationMap.getStartTime();
        return time;
    }
    
    @Override
    public Long getRemainingTime(long time){
        long now = getElapsedPlayTime(time);
        long end = getAnimationLength();// + myRampTimeMillisec;
        return end - now;
    }

    @Override
    protected boolean onPause(long time) {
        for(ServiceAddOn<Playable> addon : myAnimation.getAddOns()){
            Playable p = addon.getAddOn();
            p.pause(time);
        }
        
        if(mySource != null && mySource instanceof RampedAnimationPlayer
                && mySignalFactory != null) {
            List<String> props = new ArrayList<String>();
            props.add(AnimationSignal.PROP_RAMPING);
        
            if(myLoopFlag) {
                props.add(AnimationSignal.PROP_LOOP);
            }
        
            AnimationSignal signal = mySignalFactory.createAnimationSignal(
                    mySource.getAnimationPlayerId(),
                    AnimationSignal.EVENT_PAUSE,
                    myAnimation.getVersion().getName(),
                    myAnimation.getVersion().getNumber(),
                    myAnimation.hashCode(), myAnimation.getLength(), props);
            
            ((RampedAnimationPlayer)mySource).notifyListeners(signal);
        }
        
        return true;
    }
    @Override
    protected boolean onResume(long time) {
        for(ServiceAddOn<Playable> addon : myAnimation.getAddOns()){
            Playable p = addon.getAddOn();
            p.resume(time);
        }
        
        if(mySource != null && mySource instanceof RampedAnimationPlayer
                && mySignalFactory != null) {
            List<String> props = new ArrayList<String>();
            props.add(AnimationSignal.PROP_RAMPING);
        
            if(myLoopFlag) {
                props.add(AnimationSignal.PROP_LOOP);
            }
        
            AnimationSignal signal = mySignalFactory.createAnimationSignal(
                    mySource.getAnimationPlayerId(),
                    AnimationSignal.EVENT_RESUME,
                    myAnimation.getVersion().getName(),
                    myAnimation.getVersion().getNumber(),
                    myAnimation.hashCode(), myAnimation.getLength(), props);
            
            ((RampedAnimationPlayer)mySource).notifyListeners(signal);
        }
        
        return true;
    }
    @Override
    protected boolean onStop(long time) {
        for(ServiceAddOn<Playable> addon : myAnimation.getAddOns()){
            Playable p = addon.getAddOn();
            p.stop(time);
        }
        
        if(mySource != null && mySource instanceof RampedAnimationPlayer
                && mySignalFactory != null) {
            List<String> props = new ArrayList<String>();
            props.add(AnimationSignal.PROP_RAMPING);
        
            if(myLoopFlag) {
                props.add(AnimationSignal.PROP_LOOP);
            }
        
            AnimationSignal signal = mySignalFactory.createAnimationSignal(
                    mySource.getAnimationPlayerId(),
                    AnimationSignal.EVENT_CANCEL,
                    myAnimation.getVersion().getName(),
                    myAnimation.getVersion().getNumber(),
                    myAnimation.hashCode(), myAnimation.getLength(), props);
            
            ((RampedAnimationPlayer)mySource).notifyListeners(signal);
        }
        
        return true;
    }
    @Override
    protected boolean onComplete(long time) {
        for(ServiceAddOn<Playable> addon : myAnimation.getAddOns()){
            Playable p = addon.getAddOn();
            p.complete(time);
        }
        
        if(mySource != null && mySource instanceof RampedAnimationPlayer
                && mySignalFactory != null) {
            List<String> props = new ArrayList<String>();
            props.add(AnimationSignal.PROP_RAMPING);
        
            if(myLoopFlag) {
                props.add(AnimationSignal.PROP_LOOP);
            }
        
            AnimationSignal signal = mySignalFactory.createAnimationSignal(
                    mySource.getAnimationPlayerId(),
                    AnimationSignal.EVENT_COMPLETE,
                    myAnimation.getVersion().getName(),
                    myAnimation.getVersion().getNumber(),
                    myAnimation.hashCode(), myAnimation.getLength(), props);
            
            ((RampedAnimationPlayer)mySource).notifyListeners(signal);
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
