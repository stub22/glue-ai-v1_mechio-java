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
import org.jflux.api.core.util.DefaultTimestampSource;
import org.jflux.api.core.util.EmptyAdapter;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.mechio.api.sensor.DeviceReadPeriodEvent;
import org.mechio.api.sensor.GpioConfigEvent;
import org.mechio.api.sensor.gpio.GpioService;
import org.mechio.api.sensor.gpio.RemoteGpioServiceClient;
import org.mechio.api.sensor.packet.channel.ChannelBoolEvent;
import org.mechio.client.basic.ConnectionContext.MioServiceConnector;
import org.mechio.impl.sensor.ChannelBoolRecord;
import org.mechio.impl.sensor.DeviceReadPeriodRecord;
import org.mechio.impl.sensor.GpioConfigRecord;
import org.mechio.impl.sensor.HeaderRecord;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
final class MioSensorConnector extends MioServiceConnector{
    final static String GPIO_VALUE_RECEIVER = "gpioValueReceiver";
    final static String GPIO_DIRECTION_SENDER = "gpioDirectionSender";
    final static String GPIO_READ_PERIOD_SENDER = "gpioReadPeriodSender";
    final static String GPIO_VALUE_SENDER = "gpioValueSender";
    
    private static MioSensorConnector theMioSensorConnector;
    
    private String theGpioInputDest = "gpioEvent";
    private String theGpioDirectionDest = "gpioConfig";
    private String theGpioOutputDest = "gpioWrite";
    private String theGpioReadDest = "gpioRead";
    
    static synchronized MioSensorConnector getConnector(){
        if(theMioSensorConnector == null){
            theMioSensorConnector = new MioSensorConnector();
        }
        return theMioSensorConnector;
    }
    
    @Override
    protected synchronized void addConnection(Session session) 
            throws JMSException, URISyntaxException{
        if(myConnectionContext == null || myConnectionsFlag){
            return;
        }
        Destination gpioValReceiver = ConnectionContext.getTopic(theGpioInputDest);
        myConnectionContext.addAsyncReceiver(GPIO_VALUE_RECEIVER, session, gpioValReceiver,
                ChannelBoolRecord.class, ChannelBoolRecord.SCHEMA$,
                new EmptyAdapter<ChannelBoolRecord, ChannelBoolRecord>());
        Destination gpioDirSender = ConnectionContext.getTopic(theGpioDirectionDest);
        myConnectionContext.addSender(GPIO_DIRECTION_SENDER, session, gpioDirSender, 
                new EmptyAdapter<GpioConfigRecord, GpioConfigRecord>());
        Destination gpioPerSender = ConnectionContext.getTopic(theGpioReadDest);
        myConnectionContext.addSender(GPIO_READ_PERIOD_SENDER, session, gpioPerSender, 
                new EmptyAdapter<DeviceReadPeriodRecord, DeviceReadPeriodRecord>());
        Destination gpioValSender = ConnectionContext.getTopic(theGpioOutputDest);
        myConnectionContext.addSender(GPIO_VALUE_SENDER, session, gpioValSender, 
                new EmptyAdapter<ChannelBoolRecord, ChannelBoolRecord>());
        myConnectionsFlag = true;
    }
    
    synchronized RemoteGpioServiceClient buildRemoteClient() {
        if(myConnectionContext == null || !myConnectionsFlag){
            return null;
        }
        MessageAsyncReceiver<ChannelBoolEvent<HeaderRecord>> gpioValReceiver = 
                myConnectionContext.getAsyncReceiver(GPIO_VALUE_RECEIVER);
        MessageSender<GpioConfigEvent<HeaderRecord>> gpioDirSender = 
                myConnectionContext.getSender(GPIO_DIRECTION_SENDER);
        MessageSender<DeviceReadPeriodEvent<HeaderRecord>> gpioPerSender = 
                myConnectionContext.getSender(GPIO_READ_PERIOD_SENDER);
        MessageSender<ChannelBoolEvent<HeaderRecord>> gpioValSender = 
                myConnectionContext.getSender(GPIO_VALUE_SENDER);
        
        HeaderRecord.Builder headerFact = HeaderRecord.newBuilder();
        headerFact.setFrameId(0);
        headerFact.setSequenceId(0);
        headerFact.setTimestamp(0);
        HeaderRecord emptyHeader = headerFact.build();
        
        ChannelBoolRecord.Builder eventFact = ChannelBoolRecord.newBuilder();
        eventFact.setHeader(emptyHeader);
        eventFact.setChannelId(0);
        eventFact.setBoolValue(false);
        
        GpioConfigRecord.Builder configFact = GpioConfigRecord.newBuilder();
        configFact.setHeader(emptyHeader);
        configFact.setInputMask(0);
        
        RemoteGpioServiceClient<HeaderRecord> client = new RemoteGpioServiceClient<HeaderRecord>(
                headerFact, new DefaultTimestampSource(), eventFact, configFact,  
                gpioDirSender, gpioPerSender, gpioValSender, gpioValReceiver, 0,
                0, 0, 0, 1, 2, 3, 4, 5, 8, 9, 10, 11);
        return client;
    }
    
    static void initializeGpioClient(GpioService client){
        client.setPinDirection(8, GpioService.OUT);
        client.setPinValue(8, true);
        client.setPinDirection(10, GpioService.OUT);
        client.setPinValue(10, true);
        for(int i : RobotSensors.ALL_GPIO_PINS){
            client.setPinDirection(i, GpioService.IN);
        }
    }    
}
