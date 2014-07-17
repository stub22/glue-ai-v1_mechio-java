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
package org.mechio.api.speech.messaging;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.common.rk.utils.EventRepeater;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Listener;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.jflux.api.messaging.rk.services.DefaultServiceClient;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.api.messaging.rk.services.ServiceCommandFactory;
import org.jflux.api.messaging.rk.services.ServiceError;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.mechio.api.speech.SpeechJob;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speech.SpeechRequestFactory;
import org.mechio.api.speech.SpeechService;
import org.mechio.api.speech.utils.SpeechEventNotifier;
import org.mechio.api.speech.utils.SpeechJobManager;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteSpeechServiceClient<Conf> extends 
        DefaultServiceClient<Conf> implements SpeechService{
    private final static Logger theLogger = 
            Logger.getLogger(RemoteSpeechServiceClient.class.getName());
    private String mySpeechServiceId;
    private MessageSender<SpeechRequest> myRequestSender;
    private MessageAsyncReceiver<SpeechEventList<SpeechEvent>> myEventReceiver;
    private SpeechEventNotifier mySpeechEventNotifier;
    private SpeechRequestFactory myRequestFactory;
    private SpeechJobManager myJobManager;
    /**
     * Add request listeners to an EventRepeater rather than the RequestSender
     * in order to retain listeners when the sender changes.
     */
    private EventRepeater<SpeechRequest> myRequestRepeater;
    /**
     * Connects to a remote SpeechService through Messaging components
     */
    public RemoteSpeechServiceClient(
            Class<Conf> configClass,
            String speechServiceId,
            String remoteId,
            MessageSender<ServiceCommand> commandSender,
            MessageSender<Conf> configSender,
            MessageAsyncReceiver<ServiceError> errorReceiver,
            ServiceCommandFactory commandFactory,
            MessageSender<SpeechRequest> requestSender,
            MessageAsyncReceiver<SpeechEventList<SpeechEvent>> eventReceiver,
            SpeechRequestFactory requestFactory){
        super(speechServiceId, remoteId, 
                commandSender, configSender, errorReceiver, commandFactory);
        if(speechServiceId == null){
            throw new NullPointerException();
        }
        mySpeechServiceId = speechServiceId;
        mySpeechEventNotifier = new SpeechEventNotifier();
        myRequestSender = requestSender;
        myRequestRepeater = new EventRepeater();
        if(myRequestSender != null){
            myRequestSender.addListener(myRequestRepeater);
        }
        myRequestFactory = requestFactory;
        myEventReceiver = eventReceiver;
        if(myEventReceiver != null){
            myEventReceiver.addListener(mySpeechEventNotifier);
        }
        myJobManager = new SpeechJobManager(this);
    }
    
    @Override
    public String getSpeechServiceId(){
        return mySpeechServiceId;
    }
    
    @Override
    public void start() throws Exception{
        start(TimeUtils.now());
    }

    @Override
    public SpeechJob speak(String text) {
        if(text == null){
            throw new NullPointerException();
        }if(myRequestSender == null || myRequestFactory == null){
            theLogger.warning("Unable to send speech request, "
                    + "Request Sender or Factory is null.");
            return null;
        }
        theLogger.log(Level.INFO, "Speaking: {0}", text);
        SpeechRequest req = 
                myRequestFactory.create(getClientId(), getHostId(), text);
        myRequestSender.notifyListeners(req);
        return myJobManager.createSpeechJob(text);
    }
    
    @Override
    public void cancelSpeech(){
        send("cancelSpeech");
    }

    @Override
    public void stop() {
        super.stop(TimeUtils.now());
        myEventReceiver.stop();
        myRequestSender.stop();
    }
    
    public void setSpeechRequestSender(MessageSender<SpeechRequest> sender){
        if(myRequestSender != null){
            myRequestSender.removeListener(myRequestRepeater);
        }
        myRequestSender = sender;
        if(myRequestSender != null){
            myRequestSender.addListener(myRequestRepeater);
        }
    }
    
    public void setSpeechEventsReceiver(
            MessageAsyncReceiver<SpeechEventList<SpeechEvent>> receiver){
        if(myEventReceiver != null){
            myEventReceiver.removeListener(mySpeechEventNotifier);
        }
        myEventReceiver = receiver;
        if(myEventReceiver != null){
            myEventReceiver.addListener(mySpeechEventNotifier);
        }
    }
    
    public void setSpeechRequestFactory(SpeechRequestFactory factory){
        myRequestFactory = factory;
    }

    @Override
    public void addRequestListener(Listener<SpeechRequest> listener) {
        myRequestRepeater.addListener(listener);
    }

    @Override
    public void removeRequestListener(Listener<SpeechRequest> listener) {
        myRequestRepeater.removeListener(listener);
    }

    @Override
    public void addSpeechEventListener(Listener<SpeechEventList<SpeechEvent>> listener) {
        mySpeechEventNotifier.addSpeechEventListener(listener);
    }

    @Override
    public void removeSpeechEventListener(Listener<SpeechEventList<SpeechEvent>> listener) {
        mySpeechEventNotifier.removeSpeechEventListener(listener);
    }
    
}
