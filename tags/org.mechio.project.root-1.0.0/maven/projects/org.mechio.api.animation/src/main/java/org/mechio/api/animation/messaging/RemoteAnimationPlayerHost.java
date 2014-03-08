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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.common.rk.playable.PlayState;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.protocol.AnimationEvent;
import org.jflux.api.core.Listener;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.mechio.api.animation.player.AnimationJob;
import org.mechio.api.animation.protocol.AnimationSignal;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteAnimationPlayerHost implements Listener<AnimationSignal> {
    private final static Logger theLogger = 
            Logger.getLogger(RemoteAnimationPlayerHost.class.getName());
    private AnimationPlayer myAnimationPlayer;
    private MessageAsyncReceiver<AnimationEvent> myAnimationReceiver;
    private Listener<AnimationEvent> myEventPlayer;
    private MessageSender<AnimationSignal> mySignalSender;
    
    public RemoteAnimationPlayerHost(){
        myEventPlayer = new HackedEventPlayer();
    }
    
    public void setAnimationPlayer(AnimationPlayer player){
        if(myAnimationPlayer != null) {
            myAnimationPlayer.removeAnimationSignalListener(this);
        }
        
        myAnimationPlayer = player;
        
        if(myAnimationPlayer != null) {
            myAnimationPlayer.addAnimationSignalListener(this);
        }
    }
    
    public void setAnimationReceiver(
            MessageAsyncReceiver<AnimationEvent> receiver){
        if(myAnimationReceiver != null){
            myAnimationReceiver.removeListener(myEventPlayer);
        }
        myAnimationReceiver = receiver;
        if(myAnimationReceiver != null){
            myAnimationReceiver.addListener(myEventPlayer);
        }
    }
    
    public void setSignalSender(MessageSender<AnimationSignal> sender){
        mySignalSender = sender;
    }

    @Override
    public void handleEvent(AnimationSignal t) {
        if(mySignalSender != null){
            mySignalSender.notifyListeners(t);
        }
    }
    
    public class AnimationEventPlayer implements Listener<AnimationEvent>{

        @Override
        public void handleEvent(AnimationEvent event) {
            if(event == null){
                theLogger.warning("Ignoring null AnimaionEvent.");
                return;
            }
            Animation anim = event.getAnimation();
            if(anim == null){
                theLogger.warning(
                        "Ignoring null Animaion from AnimationEvent.");
                return;
            }else if(myAnimationPlayer == null){
                theLogger.warning("Animation Player is null.  "
                        + "Ignoring AnimaionEvent.");
                return;
            }
            theLogger.log(Level.INFO, 
                    "Sending Animation: {0}, to AnimationPlayer: {1}.", 
                    new Object[]{
                        anim.getVersion().display(),
                        myAnimationPlayer.getAnimationPlayerId()});
            
            myAnimationPlayer.playAnimation(event.getAnimation());
        }
    }
    
    public class HackedEventPlayer implements Listener<AnimationEvent>{
        private Map<Animation,AnimationJob> myAnimationMap;
        
        public HackedEventPlayer(){
            myAnimationMap = new HashMap<Animation, AnimationJob>();
        }
        
        @Override
        public void handleEvent(AnimationEvent event) {
            if(event == null){
                theLogger.warning("Ignoring null AnimaionEvent.");
                return;
            }
            Animation anim = event.getAnimation();
            if(anim == null){
                theLogger.warning(
                        "Ignoring null Animaion from AnimationEvent.");
                return;
            }else if(myAnimationPlayer == null){
                theLogger.warning("Animation Player is null.  "
                        + "Ignoring AnimaionEvent.");
                return;
            }
            if("CLEAR".equals(event.getDestinationId())){
                theLogger.log(Level.INFO, 
                        "Clearing all animations from AnimationPlayer: {0}.", 
                        myAnimationPlayer.getAnimationPlayerId());
                List<AnimationJob> jobs = myAnimationPlayer.getCurrentAnimations();
                if(jobs == null){
                    return;
                }
                for(AnimationJob job : jobs){
                    if(job == null){
                        continue;
                    }
                    myAnimationPlayer.removeAnimationJob(job);
                }
                return;
            }
            AnimationJob job = myAnimationMap.get(anim);
            if("STOP".equals(event.getDestinationId())){
                if(job != null){
                    theLogger.log(Level.INFO, 
                            "Stopping Animation: {0}, from AnimationPlayer: {1}.", 
                            new Object[]{anim.getVersion().display(),
                                myAnimationPlayer.getAnimationPlayerId()});
                    job.stop(TimeUtils.now());
                }else{
                    theLogger.log(Level.INFO, 
                            "Could not find Animation to stop: {0}, from AnimationPlayer: {1}.", 
                            new Object[]{anim.getVersion().display(),
                                myAnimationPlayer.getAnimationPlayerId()});
                }
                return;
            }                
            if(job == null){
                job = myAnimationPlayer.playAnimation(event.getAnimation());
            }else if(PlayState.RUNNING != job.getPlayState()){
                job.start(TimeUtils.now());
            }
            boolean loop = "LOOP".equals(event.getDestinationId());
            job.setLoop(loop);
            String msg = loop ? "Looping" : "Playing";
            theLogger.log(Level.INFO, 
                    "{0} Animation: {1}, from AnimationPlayer: {2}.", 
                    new Object[]{msg, anim.getVersion().display(),
                        myAnimationPlayer.getAnimationPlayerId()});

            myAnimationMap.put(anim, job);
        }
    }
}
