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

package org.mechio.client.basic;

import java.net.URISyntaxException;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import org.jflux.api.messaging.rk.MessageSender;
import org.mechio.api.animation.messaging.RemoteAnimationPlayerClient;
import org.mechio.api.animation.protocol.AnimationEvent;
import org.mechio.impl.animation.messaging.PortableAnimationEvent;
import org.mechio.client.basic.ConnectionContext.MioServiceConnector;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
final class MioAnimationConnector extends MioServiceConnector{
    final static String ANIMATION_SENDER = "animationSender";
    private String myAnimDest = "animationRequest";
    
    static MioAnimationConnector theMioAnimationConnector;
    
    static synchronized MioAnimationConnector getConnector(){
        if(theMioAnimationConnector == null){
            theMioAnimationConnector = new MioAnimationConnector();
        }
        return theMioAnimationConnector;
    }
    
    @Override
    protected synchronized void addConnection(Session session) 
            throws JMSException, URISyntaxException{
        if(myConnectionContext == null || myConnectionsFlag){
            return;
        }
        Destination anim = ConnectionContext.getTopic(myAnimDest);
        myConnectionContext.addSender(ANIMATION_SENDER, session, anim, 
                new PortableAnimationEvent.MessageRecordAdapter());
        myConnectionsFlag = true;
    }
    
    synchronized RemoteAnimationPlayerClient buildRemoteClient() {
        if(myConnectionContext == null || !myConnectionsFlag){
            return null;
        }
        
        MessageSender<AnimationEvent> animSender = myConnectionContext.getSender(ANIMATION_SENDER);
        
        RemoteAnimationPlayerClient client = new RemoteAnimationPlayerClient(
                null, "animationPlayer", "remoteAnimationPlayerId");
        client.setAnimationEventFactory(new PortableAnimationEvent.Factory());
        client.setAnimationEventSender(animSender);
        return client;
    }
}
