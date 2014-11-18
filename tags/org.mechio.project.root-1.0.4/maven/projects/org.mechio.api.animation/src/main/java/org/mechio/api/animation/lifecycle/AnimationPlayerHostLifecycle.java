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
package org.mechio.api.animation.lifecycle;

import java.util.Map;
import java.util.Properties;
import org.jflux.api.messaging.rk.Constants;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.mechio.api.animation.messaging.RemoteAnimationPlayerHost;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.protocol.AnimationEvent;
import org.mechio.api.animation.protocol.AnimationSignal;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationPlayerHostLifecycle extends 
        AbstractLifecycleProvider<
                RemoteAnimationPlayerHost, RemoteAnimationPlayerHost> {
    private final static String theAnimationPlayer = "animPlayer";
    private final static String theAnimationReceiver = "animLibrary";
    private final static String theSignalSender = "signalSender";
    
    public AnimationPlayerHostLifecycle(
            String animPlayerId, String animReceiverId, String signalSenderId){
        super(new DescriptorListBuilder()
                .dependency(theAnimationPlayer, AnimationPlayer.class)
                    .with(AnimationPlayer.PROP_PLAYER_ID, animPlayerId)
                .dependency(theAnimationReceiver, MessageAsyncReceiver.class)
                    .with(Constants.PROP_MESSAGE_RECEIVER_ID, animReceiverId)
                    .with(Constants.PROP_MESSAGE_TYPE, 
                            AnimationEvent.class.getName())
                .dependency(theSignalSender, MessageSender.class)
                    .with(Constants.PROP_MESSAGE_SENDER_ID, signalSenderId)
                    .with(Constants.PROP_MESSAGE_TYPE, 
                            AnimationSignal.class.getName())
                .getDescriptors());
        myRegistrationProperties = new Properties();
        myRegistrationProperties.put(
                AnimationPlayer.PROP_PLAYER_ID, animPlayerId);
    }

    @Override
    protected RemoteAnimationPlayerHost create(Map<String, Object> dependencies) {
        AnimationPlayer player = 
                (AnimationPlayer)dependencies.get(theAnimationPlayer);
        MessageAsyncReceiver<AnimationEvent> eventReceiver = 
                (MessageAsyncReceiver)dependencies.get(theAnimationReceiver);
        MessageSender<AnimationSignal> sender = 
                (MessageSender)dependencies.get(theSignalSender);
        RemoteAnimationPlayerHost host = new RemoteAnimationPlayerHost();
        host.setAnimationPlayer(player);
        host.setAnimationReceiver(eventReceiver);
        host.setSignalSender(sender);
        return host;
    }

    @Override
    protected void handleChange(String name, Object dependency, 
            Map<String,Object> availableDependencies){
        if(myService == null){
            return;
        }
        if(theAnimationPlayer.equals(name)){
            myService.setAnimationPlayer((AnimationPlayer)dependency);
        }else if(theAnimationReceiver.equals(name)){
            myService.setAnimationReceiver(
                    (MessageAsyncReceiver<AnimationEvent>)dependency);
        }else if(theSignalSender.equals(name)){
            myService.setSignalSender((MessageSender)dependency);
        }
    }

    @Override
    public Class<RemoteAnimationPlayerHost> getServiceClass() {
        return RemoteAnimationPlayerHost.class;
    }
}
