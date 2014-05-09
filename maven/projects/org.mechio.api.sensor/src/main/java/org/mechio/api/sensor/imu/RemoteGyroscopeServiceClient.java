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
package org.mechio.api.sensor.imu;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.core.Listener;
import org.jflux.api.core.Notifier;
import org.jflux.api.core.util.DefaultNotifier;
import org.mechio.api.sensor.DeviceReadPeriodEvent;
import org.mechio.api.sensor.FilteredVector3Event;
import org.mechio.api.sensor.GyroConfigEvent;
import org.mechio.api.sensor.SensorEventHeader;

/**
 *
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
public class RemoteGyroscopeServiceClient<T extends SensorEventHeader>
    extends DefaultNotifier<FilteredVector3Event>
    implements GyroscopeService<T> {
    private final static Logger theLogger =
            Logger.getLogger(RemoteGyroscopeServiceClient.class.getName());
    
    private Notifier<GyroConfigEvent<T>> myConfigSender;
    private Notifier<DeviceReadPeriodEvent<T>> myReadPeriodSender;
    private Notifier<FilteredVector3Event> myInputValueReceiver;
    private GyroscopeValueListener myEventListener;
    
    public RemoteGyroscopeServiceClient(
            Notifier<GyroConfigEvent<T>> configSender,
            Notifier<DeviceReadPeriodEvent<T>> readPeriodSender,
            Notifier<FilteredVector3Event> inputValueReceiver) {
        if(configSender == null || readPeriodSender == null ||
                inputValueReceiver == null) {
            theLogger.log(Level.SEVERE, "Null parameters.");
            throw new IllegalArgumentException("Parameter cannot be null.");
        }
        
        myConfigSender = configSender;
        myReadPeriodSender = readPeriodSender;
        myInputValueReceiver = inputValueReceiver;
        
        myEventListener = new GyroscopeValueListener();
        myInputValueReceiver.addListener(myEventListener);
    }

    @Override
    public void sendConfig(GyroConfigEvent<T> config) {
        if(config == null) {
            theLogger.log(Level.WARNING, "Null config.");
            throw new IllegalArgumentException("Config cannot be null.");
        }
        
        myConfigSender.notifyListeners(config);
    }

    @Override
    public void setReadPeriod(DeviceReadPeriodEvent<T> readPeriod) {
        if(readPeriod == null) {
            theLogger.log(Level.WARNING, "Null read period.");
            throw new IllegalArgumentException("Read period cannot be null.");
        }
        
        myReadPeriodSender.notifyListeners(readPeriod);
    }
    
    class GyroscopeValueListener implements Listener<FilteredVector3Event> {
        @Override
        public void handleEvent(FilteredVector3Event t) {
            notifyListeners(t);
        }
        
    }
}
