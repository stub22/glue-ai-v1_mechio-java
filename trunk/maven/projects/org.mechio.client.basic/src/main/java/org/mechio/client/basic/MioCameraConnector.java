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
import org.jflux.api.core.util.EmptyAdapter;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.api.messaging.rk.services.ServiceError;
import org.jflux.impl.messaging.rk.ServiceErrorRecord;
import org.jflux.impl.messaging.rk.services.PortableServiceCommand;
import org.mechio.api.vision.ImageEvent;
import org.mechio.api.vision.config.CameraServiceConfig;
import org.mechio.api.vision.messaging.RemoteImageServiceClient;
import org.mechio.client.basic.ConnectionContext.MioServiceConnector;
import org.mechio.impl.vision.ImageRecord;

/**
 *
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
final class MioCameraConnector extends MioServiceConnector{
    final static String CMD_SENDER = "cmCommandSender";
    final static String CONFIG_SENDER = "cmConfigSender";
    final static String ERROR_RECEIVER = "cmErrorReceiver";
    final static String EVENT_RECEIVER = "cmEventReceiver";
    
    static MioCameraConnector theMioCameraConnector;
    
    private String myCommandDest = "camera0Command";
    private String myConfigDest = "camera0Command"; //Same as command
    private String myErrorDest = "camera0Error";
    private String myEventDest = "camera0Event";
    
    static synchronized MioCameraConnector getConnector(){
        if(theMioCameraConnector == null){
            theMioCameraConnector = new MioCameraConnector();
        }
        return theMioCameraConnector;
    }
    
    @Override
    protected synchronized void addConnection(Session session) 
            throws JMSException, URISyntaxException{
        if(myConnectionContext == null || myConnectionsFlag){
            return;
        }
        
        readCameraId();
        
        Destination cmdDest = ConnectionContext.getQueue(myCommandDest);
        Destination confDest = ConnectionContext.getQueue(myConfigDest);
        Destination errDest = ConnectionContext.getTopic(myErrorDest);
        Destination evtDest = ConnectionContext.getTopic(myEventDest);
        
        myConnectionContext.addSender(CMD_SENDER, session, cmdDest, 
                new EmptyAdapter());
        myConnectionContext.addSender(CONFIG_SENDER, session, confDest, 
                new EmptyAdapter());
        myConnectionContext.addAsyncReceiver(ERROR_RECEIVER, session, errDest,
                ServiceErrorRecord.class, ServiceErrorRecord.SCHEMA$,
                new EmptyAdapter());
        myConnectionContext.addAsyncReceiver(EVENT_RECEIVER, session, evtDest,
                ImageRecord.class, ImageRecord.SCHEMA$, new EmptyAdapter());
        
        myConnectionsFlag = true;
    }
    
    synchronized RemoteImageServiceClient<CameraServiceConfig> buildRemoteClient(){
        if(myConnectionContext == null || !myConnectionsFlag){
            return null;
        }
        MessageSender<ServiceCommand> cmdSender = 
                myConnectionContext.getSender(CMD_SENDER);
        MessageSender<CameraServiceConfig> confSender = 
                myConnectionContext.getSender(CONFIG_SENDER);
        MessageAsyncReceiver<ServiceError> errReceiver = 
                myConnectionContext.getAsyncReceiver(ERROR_RECEIVER);
        MessageAsyncReceiver<ImageEvent> evtReceiver = 
                myConnectionContext.getAsyncReceiver(EVENT_RECEIVER);
        
        RemoteImageServiceClient<CameraServiceConfig> client = 
                new RemoteImageServiceClient(CameraServiceConfig.class, 
                "imageServiceId", "remoteImageServiceId", cmdSender,
                confSender, errReceiver, new PortableServiceCommand.Factory(),
                evtReceiver);
        
        return client;
    }
    
    private synchronized void readCameraId() {
        String cameraId = UserSettings.getCameraId();
        
        if(cameraId.equals("0")) {
            myCommandDest = myCommandDest.replace("1", cameraId);
            myConfigDest = myConfigDest.replace("1", cameraId);
            myErrorDest = myErrorDest.replace("1", cameraId);
            myEventDest = myEventDest.replace("1", cameraId);
        } else if(cameraId.equals("1")) {
            myCommandDest = myCommandDest.replace("0", cameraId);
            myConfigDest = myConfigDest.replace("0", cameraId);
            myErrorDest = myErrorDest.replace("0", cameraId);
            myEventDest = myEventDest.replace("0", cameraId);
        }
    }
}
