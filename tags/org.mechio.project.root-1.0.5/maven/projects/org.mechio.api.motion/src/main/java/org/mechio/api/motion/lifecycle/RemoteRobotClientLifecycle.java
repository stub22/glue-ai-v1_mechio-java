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
package org.mechio.api.motion.lifecycle;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.messaging.rk.Constants;
import org.jflux.api.messaging.rk.MessageBlockingReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.messaging.RemoteRobotClient;
import org.mechio.api.motion.messaging.RobotRequestFactory;
import org.mechio.api.motion.protocol.MotionFrameEvent;
import org.mechio.api.motion.protocol.MotionFrameEvent.MotionFrameEventFactory;
import org.mechio.api.motion.protocol.RobotRequest;
import org.mechio.api.motion.protocol.RobotResponse;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteRobotClientLifecycle extends 
        AbstractLifecycleProvider<RemoteRobotClient, RemoteRobotClient>{
    private final static Logger theLogger = 
            Logger.getLogger(RemoteRobotClientLifecycle.class.getName());
    private final static String theFrameEventFactory = "motionFrameEventFactory";
    private final static String theRequestSender = "requestSender";
    private final static String theResponseReceiver = "responseReceiver";
    private final static String theFrameSender = "frameSender";
    private final static String theRequestFactory = "responseFactory";
    
    private Robot.Id myRobotId;
    private String mySourceId;
    private String myDestId;

    public RemoteRobotClientLifecycle(String sourceId, String destId,
            Robot.Id robotId, String reqSenderId, String respReceiverId, 
            String frameSenderId){
        super(new DescriptorListBuilder()
                .dependency(theRequestSender, MessageSender.class)
                    .with(Constants.PROP_MESSAGE_SENDER_ID, reqSenderId)
                    .with(Constants.PROP_MESSAGE_TYPE, 
                            RobotRequest.class.getName())
                .dependency(theResponseReceiver, MessageBlockingReceiver.class) 
                    .with(Constants.PROP_MESSAGE_RECEIVER_ID, respReceiverId)
                    .with(Constants.PROP_MESSAGE_TYPE, 
                            RobotResponse.class.getName())
                .dependency(theFrameSender, MessageSender.class)
                    .with(Constants.PROP_MESSAGE_SENDER_ID, frameSenderId)
                    .with(Constants.PROP_MESSAGE_TYPE, 
                                MotionFrameEvent.class.getName())
                .dependency(theRequestFactory, RobotRequestFactory.class)
                .dependency(theFrameEventFactory, MotionFrameEventFactory.class)
                .getDescriptors());
        if(sourceId == null || destId == null || robotId == null){
            throw new NullPointerException();
        }
        mySourceId = sourceId;
        myDestId = destId;
        myRobotId = robotId;
        if(myRegistrationProperties == null){
            myRegistrationProperties = new Properties();
        }
        myRegistrationProperties.put(Robot.PROP_ID, robotId.getRobtIdString());
    }

    @Override
    protected RemoteRobotClient create(Map<String, Object> services) {
        MessageSender<RobotRequest> reqSender = 
                (MessageSender)services.get(theRequestSender);
        MessageBlockingReceiver<RobotResponse> respReceiver = 
                (MessageBlockingReceiver) services.get(theResponseReceiver);
        MessageSender<MotionFrameEvent> frameSender = 
                (MessageSender)services.get(theFrameSender);
        RobotRequestFactory reqFact = 
                (RobotRequestFactory)services.get(theRequestFactory);
        MotionFrameEventFactory frameEventFact = 
                (MotionFrameEventFactory)services.get(theFrameEventFactory);
        RemoteRobotClient client = 
                new RemoteRobotClient(myRobotId, mySourceId, myDestId, 
                        reqFact, frameEventFact);
        client.setMotionFrameSender(frameSender);
        client.setRequestSender(reqSender);
        client.setResponseReceiver(respReceiver);
        try{
            reqSender.start();
            respReceiver.start();
            frameSender.start();
        }catch(Exception ex){
            theLogger.log(Level.WARNING, "Error starting RemoteRobotHost "
                    + "messaging components.", ex);
        }
        return client;
    }

    @Override
    protected void handleChange(
            String serviceId, Object service, Map<String,Object> dependencies) {
        if(myService == null){
            return;
        }
        if(theResponseReceiver.equals(serviceId) && service != null){
            myService.setResponseReceiver((MessageBlockingReceiver<RobotResponse>)service);            
        }else if(theRequestSender.equals(serviceId) && service != null){
            myService.setRequestSender(
                    (MessageSender<RobotRequest>)service);            
        }else if(theFrameSender.equals(serviceId) && service != null){
            myService.setMotionFrameSender(
                    (MessageSender<MotionFrameEvent>)service);            
        }else if(theFrameEventFactory.equals(serviceId)){
            //myService.setFrameEventFactory((MotionFrameEventFactory)service);            
        }else if(theRequestFactory.equals(serviceId)){
            //myService.setRequestFactory((RobotResponseFactory)service);            
        }
    }

    @Override
    public Class<RemoteRobotClient> getServiceClass() {
        return RemoteRobotClient.class;
    }
    
    @Override
    public synchronized void stop() {
        if(myService != null) {
            myService.shutDown();
        }
    }
}
