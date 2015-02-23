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
package org.mechio.api.speech.lifecycle;

import java.util.Map;
import java.util.Properties;
import org.jflux.api.messaging.rk.Constants;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.api.messaging.rk.services.ServiceCommandFactory;
import org.jflux.api.messaging.rk.services.ServiceError;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.mechio.api.speech.messaging.RemoteSpeechServiceClient;
import org.mechio.api.speech.SpeechConfig;
import org.mechio.api.speech.SpeechEventList;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speech.SpeechRequestFactory;
import org.mechio.api.speech.SpeechService;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteSpeechServiceClientLifecycle extends 
        AbstractLifecycleProvider<SpeechService, RemoteSpeechServiceClient> {
    private final static String theCommandSender = "commandSender";
    private final static String theConfigSender = "configSender";
    private final static String theErrorReceiver = "errorReceiver";   
    private final static String theCommandFactory = "commandFactory"; 
    private final static String theRequestSender = "requestSender";
    private final static String theEventsReceiver = "eventsReceiver";
    private final static String theRequestFactory = "requestFactory";
    private String myLocalServiceId;
    private String myRemoteServiceId;
    
    /**
     * Creates a new SpeechTriggerHandlerLifecycle for a SpeechService
     * @param commandSenderId SpeechService to use
     */
    public RemoteSpeechServiceClientLifecycle(
            String speechServiceId, String remoteId,
            String commandSenderId, String configSenderId, 
            String errorReceiverId, String requestSenderId,
            String eventsReceiverId){
        super(new DescriptorListBuilder()
                .dependency(theCommandSender, MessageSender.class)
                    .with(Constants.PROP_MESSAGE_SENDER_ID, commandSenderId)
                    .with(Constants.PROP_MESSAGE_TYPE, ServiceCommand.class.getName())
                .dependency(theConfigSender, MessageSender.class)
                    .with(Constants.PROP_MESSAGE_SENDER_ID, configSenderId)
                    .with(Constants.PROP_MESSAGE_TYPE, SpeechConfig.class.getName())
                .dependency(theErrorReceiver, MessageAsyncReceiver.class)
                    .with(Constants.PROP_MESSAGE_RECEIVER_ID, errorReceiverId)
                    .with(Constants.PROP_MESSAGE_TYPE, ServiceError.class.getName())
                .dependency(theCommandFactory, ServiceCommandFactory.class)
                .dependency(theRequestSender, MessageSender.class)
                    .with(Constants.PROP_MESSAGE_SENDER_ID, requestSenderId)
                    .with(Constants.PROP_MESSAGE_TYPE, SpeechRequest.class.getName())
                .dependency(theEventsReceiver, MessageAsyncReceiver.class)
                    .with(Constants.PROP_MESSAGE_RECEIVER_ID, eventsReceiverId)
                    .with(Constants.PROP_MESSAGE_TYPE, SpeechEventList.class.getName())
                .dependency(theRequestFactory, SpeechRequestFactory.class)
                .getDescriptors());
        if(speechServiceId == null || remoteId == null){
            throw new NullPointerException();
        }
        myLocalServiceId = speechServiceId;
        myRemoteServiceId = remoteId;
        myRegistrationProperties = new Properties();
        myRegistrationProperties.put(
                SpeechService.PROP_ID, myLocalServiceId);
    }

    @Override
    protected RemoteSpeechServiceClient create(Map<String, Object> services) {
        MessageSender<ServiceCommand> commandSender = 
                (MessageSender)services.get(theCommandSender);
        MessageSender<SpeechConfig> configSender = 
                (MessageSender)services.get(theConfigSender);
        MessageAsyncReceiver<ServiceError> errorReceiver = 
                (MessageAsyncReceiver)services.get(theErrorReceiver);
        ServiceCommandFactory commandFactory = 
                (ServiceCommandFactory)services.get(theCommandFactory);
        MessageSender<SpeechRequest> requestSender = 
                (MessageSender)services.get(theRequestSender);
        MessageAsyncReceiver<SpeechEventList> eventsReceiver = 
                (MessageAsyncReceiver)services.get(theEventsReceiver);
        SpeechRequestFactory requestFactory = 
                (SpeechRequestFactory)services.get(theRequestFactory);
        
        return new RemoteSpeechServiceClient(
                SpeechConfig.class, myLocalServiceId, myRemoteServiceId, 
                commandSender, configSender, errorReceiver, commandFactory, 
                requestSender, eventsReceiver, requestFactory);
    }

    @Override
    protected void handleChange(String name, Object dependency, 
            Map<String,Object> availableDependencies){
        if(theCommandSender.equals(name)){
            myService.setCommandSender((MessageSender)dependency);
        }else if(theConfigSender.equals(name)){
            myService.setConfigSender((MessageSender)dependency);
        }else if(theErrorReceiver.equals(name)){
            myService.setErrorReceiver((MessageAsyncReceiver)dependency);
        }else if(theCommandFactory.equals(name)){
            myService.setCommandFactory((ServiceCommandFactory)dependency);
        }else if(theRequestSender.equals(name)){
            myService.setSpeechRequestSender((MessageSender)dependency);
        }else if(theEventsReceiver.equals(name)){
            myService.setSpeechEventsReceiver((MessageAsyncReceiver)dependency);
        }else if(theRequestFactory.equals(name)){
            myService.setSpeechRequestFactory((SpeechRequestFactory)dependency);
        }
    }

    @Override
    public Class<SpeechService> getServiceClass() {
        return SpeechService.class;
    }
}
