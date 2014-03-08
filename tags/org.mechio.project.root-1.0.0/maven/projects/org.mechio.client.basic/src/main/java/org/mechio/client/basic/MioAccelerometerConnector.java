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
import org.mechio.api.sensor.AccelerometerConfigEvent;
import org.mechio.api.sensor.DeviceReadPeriodEvent;
import org.mechio.api.sensor.FilteredVector3Event;
import org.mechio.api.sensor.imu.RemoteAccelerometerServiceClient;
import org.mechio.client.basic.ConnectionContext.MioServiceConnector;
import org.mechio.impl.sensor.AccelerometerConfigRecord;
import org.mechio.impl.sensor.DeviceReadPeriodRecord;
import org.mechio.impl.sensor.FilteredVector3Record;
import org.mechio.impl.sensor.HeaderRecord;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
final class MioAccelerometerConnector extends MioServiceConnector{
    final static String ACCEL_VALUE_RECEIVER = "accelValueReceiver";
    final static String ACCEL_CONFIG_SENDER = "accelConfigSender";
    final static String ACCEL_READ_PERIOD_SENDER = "accelReadPeriodSender";
    
    private static MioAccelerometerConnector theMioAccelerometerConnector;
    
    private String theAccelInputDest = "accelerometerEvent";
    private String theAccelConfigDest = "accelerometerConfig";
    private String theAccelReadDest = "accelerometerRead";
    
    static synchronized MioAccelerometerConnector getConnector(){
        if(theMioAccelerometerConnector == null){
            theMioAccelerometerConnector = new MioAccelerometerConnector();
        }
        return theMioAccelerometerConnector;
    }
    
    @Override
    protected synchronized void addConnection(Session session) 
            throws JMSException, URISyntaxException{
        if(myConnectionContext == null || myConnectionsFlag){
            return;
        }
        Destination accelValReceiver = ConnectionContext.getTopic(theAccelInputDest);
        myConnectionContext.addAsyncReceiver(ACCEL_VALUE_RECEIVER, session, accelValReceiver,
                FilteredVector3Record.class, FilteredVector3Record.SCHEMA$,
                new EmptyAdapter<FilteredVector3Record, FilteredVector3Record>());
        Destination accelCfgSender = ConnectionContext.getTopic(theAccelConfigDest);
        myConnectionContext.addSender(ACCEL_CONFIG_SENDER, session, accelCfgSender, 
                new EmptyAdapter<AccelerometerConfigRecord, AccelerometerConfigRecord>());
        Destination accelPerSender = ConnectionContext.getTopic(theAccelReadDest);
        myConnectionContext.addSender(ACCEL_READ_PERIOD_SENDER, session, accelPerSender, 
                new EmptyAdapter<DeviceReadPeriodRecord, DeviceReadPeriodRecord>());
        myConnectionsFlag = true;
    }
    
    synchronized RemoteAccelerometerServiceClient buildRemoteClient() {
        if(myConnectionContext == null || !myConnectionsFlag){
            return null;
        }
        MessageAsyncReceiver<FilteredVector3Event> accelValReceiver = 
                myConnectionContext.getAsyncReceiver(ACCEL_VALUE_RECEIVER);
        MessageSender<AccelerometerConfigEvent<HeaderRecord>> accelCfgSender = 
                myConnectionContext.getSender(ACCEL_CONFIG_SENDER);
        MessageSender<DeviceReadPeriodEvent<HeaderRecord>> accelPerSender = 
                myConnectionContext.getSender(ACCEL_READ_PERIOD_SENDER);
        
        RemoteAccelerometerServiceClient<HeaderRecord> client =
                new RemoteAccelerometerServiceClient<HeaderRecord>(
                accelCfgSender, accelPerSender, accelValReceiver);
        return client;
    }
}
