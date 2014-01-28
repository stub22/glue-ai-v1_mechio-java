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
import org.mechio.api.sensor.DeviceReadPeriodEvent;
import org.mechio.api.sensor.FilteredVector3Event;
import org.mechio.api.sensor.GyroConfigEvent;
import org.mechio.api.sensor.imu.RemoteGyroscopeServiceClient;
import org.mechio.client.basic.ConnectionContext.MioServiceConnector;
import org.mechio.impl.sensor.DeviceReadPeriodRecord;
import org.mechio.impl.sensor.FilteredVector3Record;
import org.mechio.impl.sensor.GyroConfigRecord;
import org.mechio.impl.sensor.HeaderRecord;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
final class MioGyroscopeConnector extends MioServiceConnector{
    final static String GYRO_VALUE_RECEIVER = "gyroValueReceiver";
    final static String GYRO_CONFIG_SENDER = "gyroConfigSender";
    final static String GYRO_READ_PERIOD_SENDER = "gyroReadPeriodSender";
    
    private static MioGyroscopeConnector theMioGyroscopeConnector;
    
    private String theGyroInputDest = "gyroEvent";
    private String theGyroConfigDest = "gyroConfig";
    private String theGyroReadDest = "gyroRead";
    
    static synchronized MioGyroscopeConnector getConnector(){
        if(theMioGyroscopeConnector == null){
            theMioGyroscopeConnector = new MioGyroscopeConnector();
        }
        return theMioGyroscopeConnector;
    }
    
    @Override
    protected synchronized void addConnection(Session session) 
            throws JMSException, URISyntaxException{
        if(myConnectionContext == null || myConnectionsFlag){
            return;
        }
        Destination gyroValReceiver = ConnectionContext.getTopic(theGyroInputDest);
        myConnectionContext.addAsyncReceiver(GYRO_VALUE_RECEIVER, session, gyroValReceiver,
                FilteredVector3Record.class, FilteredVector3Record.SCHEMA$,
                new EmptyAdapter<FilteredVector3Record, FilteredVector3Record>());
        Destination gyroCfgSender = ConnectionContext.getTopic(theGyroConfigDest);
        myConnectionContext.addSender(GYRO_CONFIG_SENDER, session, gyroCfgSender, 
                new EmptyAdapter<GyroConfigRecord, GyroConfigRecord>());
        Destination gyroPerSender = ConnectionContext.getTopic(theGyroReadDest);
        myConnectionContext.addSender(GYRO_READ_PERIOD_SENDER, session, gyroPerSender, 
                new EmptyAdapter<DeviceReadPeriodRecord, DeviceReadPeriodRecord>());
        myConnectionsFlag = true;
    }
    
    synchronized RemoteGyroscopeServiceClient buildRemoteClient() {
        if(myConnectionContext == null || !myConnectionsFlag){
            return null;
        }
        MessageAsyncReceiver<FilteredVector3Event> gyroValReceiver = 
                myConnectionContext.getAsyncReceiver(GYRO_VALUE_RECEIVER);
        MessageSender<GyroConfigEvent<HeaderRecord>> gyroCfgSender = 
                myConnectionContext.getSender(GYRO_CONFIG_SENDER);
        MessageSender<DeviceReadPeriodEvent<HeaderRecord>> gyroPerSender = 
                myConnectionContext.getSender(GYRO_READ_PERIOD_SENDER);
        
        RemoteGyroscopeServiceClient<HeaderRecord> client =
                new RemoteGyroscopeServiceClient<HeaderRecord>(
                gyroCfgSender, gyroPerSender, gyroValReceiver);
        return client;
    }
}
