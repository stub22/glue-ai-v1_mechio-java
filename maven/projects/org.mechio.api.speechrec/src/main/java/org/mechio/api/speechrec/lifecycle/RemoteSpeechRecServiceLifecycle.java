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
package org.mechio.api.speechrec.lifecycle;

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
import org.mechio.api.speechrec.SpeechRecEvent;
import org.mechio.api.speechrec.SpeechRecEventList;
import org.mechio.api.speechrec.SpeechRecService;
import org.mechio.api.speechrec.messaging.RemoteSpeechRecServiceClient;

/**
 *
 * @author Amy Jessica Book <www.mechio.org>
 */
public class RemoteSpeechRecServiceLifecycle<Conf> extends 
        AbstractLifecycleProvider<SpeechRecService, RemoteSpeechRecServiceClient> {
    private final static String theCommandSender = "commandSender";
    private final static String theConfigSender = "configSender";
    private final static String theErrorReceiver = "errorReceiver";   
    private final static String theCommandFactory = "commandFactory"; 
    private final static String theSpeechRecReceiver = "speechRecReceiver";
    private String myClientServiceId;
    private String myHostServiceId;
    private Class<Conf> myConfigClass;
    
    /**
     * Creates a new SpeechTriggerHandlerLifecycle for a SpeechService
     * @param commandSenderId SpeechService to use
     */
    public RemoteSpeechRecServiceLifecycle(Class<Conf> configClass,
            String speechRecServiceId, String remoteId,
            String commandSenderId, String configSenderId,
            String errorReceiverId, String requestSenderId,
            String eventsReceiverId){
        super(new DescriptorListBuilder()
                .dependency(theCommandSender, MessageSender.class)
                    .with(Constants.PROP_MESSAGE_SENDER_ID, commandSenderId)
                    .with(Constants.PROP_MESSAGE_TYPE, ServiceCommand.class.getName())
                .dependency(theConfigSender, MessageSender.class)
                    .with(Constants.PROP_MESSAGE_SENDER_ID, configSenderId)
                    .with(Constants.PROP_MESSAGE_TYPE, configClass.getName())
                .dependency(theErrorReceiver, MessageAsyncReceiver.class)
                    .with(Constants.PROP_MESSAGE_RECEIVER_ID, errorReceiverId)
                    .with(Constants.PROP_MESSAGE_TYPE, ServiceError.class.getName())
                .dependency(theSpeechRecReceiver, MessageAsyncReceiver.class)
                    .with(Constants.PROP_MESSAGE_RECEIVER_ID, eventsReceiverId)
                    .with(Constants.PROP_MESSAGE_TYPE, SpeechRecEventList.class.getName())
                .dependency(theCommandFactory, ServiceCommandFactory.class)
                .getDescriptors());
        if(speechRecServiceId == null || remoteId == null){
            throw new NullPointerException();
        }
        myClientServiceId = speechRecServiceId;
        myHostServiceId = remoteId;
        myRegistrationProperties = new Properties();
        myRegistrationProperties.put(
                SpeechRecService.PROP_ID, myClientServiceId);
    }

    @Override
    protected RemoteSpeechRecServiceClient create(Map<String, Object> services) {
        MessageSender<ServiceCommand> commandSender = 
                (MessageSender)services.get(theCommandSender);
        MessageSender<Conf> configSender = 
                (MessageSender)services.get(theConfigSender);
        MessageAsyncReceiver<ServiceError> errorReceiver = 
                (MessageAsyncReceiver)services.get(theErrorReceiver);
        ServiceCommandFactory commandFactory = 
                (ServiceCommandFactory)services.get(theCommandFactory);
        MessageAsyncReceiver<SpeechRecEvent> speechRecReceiver = 
                (MessageAsyncReceiver)services.get(theSpeechRecReceiver);
        return new RemoteSpeechRecServiceClient(
                myConfigClass, myClientServiceId, myHostServiceId, 
                commandSender, configSender, errorReceiver, 
                commandFactory, speechRecReceiver);
    }

    @Override
    protected void handleChange(String name, Object dependency, 
            Map<String,Object> availableDependencies){
        if(myService == null){
            return;
        }
        if(theCommandSender.equals(name)){
            myService.setCommandSender(
                    (MessageSender<ServiceCommand>)dependency);
        }else if(theConfigSender.equals(name)){
            myService.setConfigSender((MessageSender<Conf>)dependency);
        }else if(theErrorReceiver.equals(name)){
            myService.setErrorReceiver(
                    (MessageAsyncReceiver<ServiceError>)dependency);
        }else if(theCommandFactory.equals(name)){
            myService.setCommandFactory((ServiceCommandFactory)dependency);
        }else if(theSpeechRecReceiver.equals(name)){
            myService.setSpeechRecReceiver(
                    (MessageAsyncReceiver<SpeechRecEvent>)dependency);
        }
    }

    @Override
    public Class<SpeechRecService> getServiceClass() {
        return SpeechRecService.class;
    }
}
