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

package org.mechio.impl.vision;

import org.jflux.api.core.util.EmptyAdapter;
import org.jflux.impl.messaging.rk.JMSAvroServiceFacade;
import org.jflux.impl.messaging.rk.config.RKMessagingConfigUtils;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.mechio.api.vision.ImageEvent;
import org.mechio.api.vision.ImageRegionList;
import org.mechio.api.vision.config.CameraServiceConfig;
import org.mechio.api.vision.config.FaceDetectServiceConfig;
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
        theLogger.info("VisionAPI Activation Begin.");
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                CameraServiceConfig.class, 
                CameraConfig.class, 
                CameraConfig.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.CONFIG_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                ImageEvent.class, 
                ImageRecord.class, 
                ImageRecord.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                FaceDetectServiceConfig.class, 
                FaceDetectConfig.class, 
                FaceDetectConfig.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.CONFIG_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        
        RKMessagingConfigUtils.registerAvroSerializationConfig(
                ImageRegionList.class, 
                ImageRegionListRecord.class, 
                ImageRegionListRecord.SCHEMA$, 
                new EmptyAdapter(), 
                new EmptyAdapter(), 
                JMSAvroServiceFacade.AVRO_MIME_TYPE, null, 
                new OSGiComponentFactory(context));
        theLogger.info("VisionAPI Activation Complete.");
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
