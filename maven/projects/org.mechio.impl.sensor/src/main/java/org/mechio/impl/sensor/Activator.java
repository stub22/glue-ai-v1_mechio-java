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

package org.mechio.impl.sensor;

import org.jflux.api.core.util.EmptyAdapter;
import org.jflux.impl.messaging.rk.JMSAvroServiceFacade;
import org.jflux.impl.messaging.rk.config.RKMessagingConfigUtils;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.mechio.api.sensor.AccelerometerConfigEvent;
import org.mechio.api.sensor.CompassConfigEvent;
import org.mechio.api.sensor.DeviceReadPeriodEvent;
import org.mechio.api.sensor.FilteredVector3Event;
import org.mechio.api.sensor.GpioConfigEvent;
import org.mechio.api.sensor.GyroConfigEvent;
import org.mechio.api.sensor.packet.channel.ChannelBoolEvent;
import org.mechio.api.sensor.packet.channel.ChannelDoubleEvent;
import org.mechio.api.sensor.packet.channel.TimedRGBOutputEvent;
import org.mechio.api.sensor.packet.num.Double2Event;
import org.mechio.api.sensor.packet.num.Double4Event;
import org.mechio.api.sensor.packet.num.StampedDouble2Event;
import org.mechio.api.sensor.packet.num.StampedDouble3Event;
import org.mechio.api.sensor.packet.num.StampedDouble4Event;
import org.mechio.api.sensor.packet.num.StampedDoubleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class Activator implements BundleActivator {
    private final static Logger theLogger = LoggerFactory.getLogger(Activator.class);
    
    @Override
    public void start(BundleContext context) throws Exception {
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                GpioConfigEvent.class, 
                GpioConfigRecord.class, 
                GpioConfigRecord.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                ChannelBoolEvent.class, 
                ChannelBoolRecord.class, 
                ChannelBoolRecord.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                DeviceReadPeriodEvent.class, 
                DeviceReadPeriodRecord.class, 
                DeviceReadPeriodRecord.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                AccelerometerConfigEvent.class, 
                AccelerometerConfigRecord.class, 
                AccelerometerConfigRecord.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                FilteredVector3Event.class, 
                FilteredVector3Record.class, 
                FilteredVector3Record.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                GyroConfigEvent.class, 
                GyroConfigRecord.class, 
                GyroConfigRecord.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                CompassConfigEvent.class, 
                CompassConfigRecord.class, 
                CompassConfigRecord.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                ChannelDoubleEvent.class, 
                ChannelDoubleRecord.class, 
                ChannelDoubleRecord.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                Double2Event.class, 
                Double2Record.class, 
                Double2Record.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                Double4Event.class, 
                Double4Record.class, 
                Double4Record.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                StampedDoubleEvent.class, 
                StampedDoubleRecord.class, 
                StampedDoubleRecord.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                StampedDouble2Event.class, 
                StampedDouble2Record.class, 
                StampedDouble2Record.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                StampedDouble3Event.class, 
                StampedDouble3Record.class, 
                StampedDouble3Record.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                StampedDouble4Event.class, 
                StampedDouble4Record.class, 
                StampedDouble4Record.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                TimedRGBOutputEvent.class, 
                TimedRGBOutputRecord.class, 
                TimedRGBOutputRecord.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
