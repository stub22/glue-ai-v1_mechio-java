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
import org.osgi.framework.BundleContext;
import org.mechio.api.animation.messaging.RemoteAnimationPlayerClient;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.protocol.AnimationEvent;
import org.mechio.api.animation.protocol.AnimationEvent.AnimationEventFactory;
import org.mechio.api.animation.protocol.AnimationSignal;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationPlayerClientLifecycle extends 
        AbstractLifecycleProvider<
                AnimationPlayer, RemoteAnimationPlayerClient> {
    private final static String theAnimationSender = "animSender";
    private final static String theSignalReceiver = "signalReceiver";
    private final static String theAnimationEventFactory = "animEventFactory";
    private String myPlayerClientId;
    private String myPlayerHostId;
    private BundleContext myContext;
    
    public AnimationPlayerClientLifecycle(
            String animPlayerId, String remotePlayerId, String animSenderId,
            String signalReceiverId, BundleContext context){
        super(new DescriptorListBuilder()
                .dependency(theAnimationSender, MessageSender.class)
                    .with(Constants.PROP_MESSAGE_SENDER_ID, animSenderId)
                    .with(Constants.PROP_MESSAGE_TYPE, 
                            AnimationEvent.class.getName())
                .dependency(theAnimationEventFactory, 
                        AnimationEventFactory.class)
                .dependency(theSignalReceiver, MessageAsyncReceiver.class)
                    .with(Constants.PROP_MESSAGE_RECEIVER_ID, signalReceiverId)
                    .with(Constants.PROP_MESSAGE_TYPE, 
                            AnimationSignal.class.getName())
                .getDescriptors());
        if(animPlayerId == null || remotePlayerId == null){
            throw new NullPointerException();
        }
        myPlayerClientId = animPlayerId;
        myPlayerHostId = remotePlayerId;
        myContext = context;
        myRegistrationProperties = new Properties();
        myRegistrationProperties.put(
                AnimationPlayer.PROP_PLAYER_ID, animPlayerId);
        myServiceClassNames = new String[]{
            AnimationPlayer.class.getName(),
            RemoteAnimationPlayerClient.class.getName()
        };
    }

    @Override
    protected RemoteAnimationPlayerClient create(Map<String, Object> dependencies) {
        MessageSender<AnimationEvent> sender = 
                (MessageSender)dependencies.get(theAnimationSender);
        MessageAsyncReceiver<AnimationSignal> receiver = 
                (MessageAsyncReceiver)dependencies.get(theSignalReceiver);
        AnimationEventFactory factory = 
                (AnimationEventFactory)dependencies.get(
                        theAnimationEventFactory);
        RemoteAnimationPlayerClient client = 
                new RemoteAnimationPlayerClient(
                        myContext, myPlayerClientId, myPlayerHostId);
        client.setAnimationEventFactory(factory);
        client.setAnimationEventSender(sender);
        client.setAnimationSignalReceiver(receiver);
        return client;
    }

    @Override
    protected void handleChange(String name, Object dependency, 
            Map<String,Object> availableDependencies){
        if(myService == null){
            return;
        }
        if(theAnimationSender.equals(name)){
            myService.setAnimationEventSender(
                    (MessageSender)dependency);
        }else if(theAnimationEventFactory.equals(name)){
            myService.setAnimationEventFactory(
                    (AnimationEventFactory)dependency);
        }else if(theSignalReceiver.equals(name)){
            myService.setAnimationSignalReceiver(
                    (MessageAsyncReceiver<AnimationSignal>)dependency);
        }
    }

    @Override
    public Class<AnimationPlayer> getServiceClass() {
        return AnimationPlayer.class;
    }
}
