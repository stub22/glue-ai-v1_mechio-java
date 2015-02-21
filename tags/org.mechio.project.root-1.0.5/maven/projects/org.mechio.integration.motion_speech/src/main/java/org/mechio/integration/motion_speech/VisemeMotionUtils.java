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
package org.mechio.integration.motion_speech;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.jflux.api.common.rk.osgi.lifecycle.ConfiguredServiceLifecycle;
import org.jflux.api.common.rk.osgi.lifecycle.ConfiguredServiceParams;
import org.jflux.api.common.rk.services.Constants;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceFactory;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceGroup;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.mechio.api.motion.Robot;
import org.mechio.api.speech.SpeechService;
import org.mechio.api.speech.viseme.VisemeBindingManager;
import org.mechio.api.speech.viseme.config.VisemeBindingManagerConfig;
import org.mechio.impl.speech.viseme.VisemeBindingManagerAvroConfigLoader;
import org.mechio.impl.speech.viseme.VisemeBindingManagerConfigAvroStreamLoader;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */


public class VisemeMotionUtils {    
    public static ManagedServiceGroup startVisemeFrameSourceGroup(
            ManagedServiceFactory fact, Robot.Id robotId, 
            String speechServiceId, String visemeConfigPath){
        String groupId = 
                "viseme/" + robotId.getRobtIdString() + "/" + speechServiceId;
        ManagedServiceGroup group = new ManagedServiceGroup(fact, 
                getLifecycles(groupId, robotId, speechServiceId, visemeConfigPath), 
                groupId, null);
        group.start();
        return group;
    }
    
    private static List<ServiceLifecycleProvider> getLifecycles(
            String groupId, Robot.Id robotId, String speechServiceId, 
            String visemeConfigPath){
        String configFileId = groupId +"/config/avro";
        return Arrays.asList(
                getBindingConfigLifecycle(configFileId, new File(visemeConfigPath)),
                getBindingManagerLifecycle(robotId, speechServiceId, configFileId),
                getVisemeFrameSourceLifecycle(robotId, speechServiceId));
    }
    
    private static ServiceLifecycleProvider getBindingManagerLifecycle(
            Robot.Id robotId, String speechServiceId, String configFileId){
        ConfiguredServiceParams<
                VisemeBindingManager, 
                VisemeBindingManagerConfig, 
                File> params = new ConfiguredServiceParams(
                        VisemeBindingManager.class, 
                        VisemeBindingManagerConfig.class, 
                        File.class, null, null, configFileId, 
                        VisemeBindingManager.VERSION, 
                        VisemeBindingManagerAvroConfigLoader.VERSION);
        Properties registrationProps = new Properties();
        registrationProps.put(Robot.PROP_ID, robotId.getRobtIdString());
        registrationProps.put(SpeechService.PROP_ID, speechServiceId);
        ConfiguredServiceLifecycle lifecycle = 
                new ConfiguredServiceLifecycle(params, registrationProps);
        return lifecycle;
    }
    
    private static ServiceLifecycleProvider getBindingConfigLifecycle(
            String paramId, File file){
        Properties props = new Properties();
        props.put(Constants.CONFIG_PARAM_ID, paramId);
        props.put(Constants.CONFIG_FORMAT_VERSION, 
                VisemeBindingManagerAvroConfigLoader.VERSION.toString());
        props.put("FilePath", file.getAbsolutePath());
        ServiceLifecycleProvider lifecycle = 
                new SimpleLifecycle(file, File.class, props);
        return lifecycle;
    }
    
    private static ServiceLifecycleProvider getVisemeFrameSourceLifecycle(
            Robot.Id robotId, String speechServiceId){
        return new VisemeFrameSourceLifecycle(robotId, speechServiceId);
    }
    
    public static String getNetBeansRootPath(){
        String brandingToken = "robokindcontroller";
        String cluster = "robokind";
        File f = new File("./");
        String path = f.getAbsolutePath();
        int len = path.length();
        if(len >= 5){
            String dir = path.substring(len-5, len-2).toLowerCase();
            if(dir.equals("bin")){
                return "../" + cluster + "/";
            }
        }
        int blen = brandingToken.length() + 2;
        if(len >= blen){
            String dir = path.substring(len-blen, len-2).toLowerCase();
            if(dir.equals(brandingToken)){
                return "./" + cluster + "/";
            }
        }
        return "./target/" + brandingToken + "/" + cluster + "/";
    }
    
    private static ServiceLifecycleProvider getBindingManagerStreamLifecycle(
            Robot.Id robotId, String speechServiceId, String configFileId){
        ConfiguredServiceParams<
                VisemeBindingManager, 
                VisemeBindingManagerConfig, 
                InputStream> params = new ConfiguredServiceParams(
                        VisemeBindingManager.class, 
                        VisemeBindingManagerConfig.class, 
                        InputStream.class, null, null, configFileId, 
                        VisemeBindingManager.VERSION, 
                        VisemeBindingManagerConfigAvroStreamLoader.VERSION);
        Properties registrationProps = new Properties();
        registrationProps.put(Robot.PROP_ID, robotId.getRobtIdString());
        registrationProps.put(SpeechService.PROP_ID, speechServiceId);
        ConfiguredServiceLifecycle lifecycle = 
                new ConfiguredServiceLifecycle(params, registrationProps);
        return lifecycle;
    }
    public static ManagedServiceGroup startVisemeFrameSourceStreamGroup(
            ManagedServiceFactory fact, Robot.Id robotId, 
            String speechServiceId, InputStream visemeConfigStream){
        String groupId = 
                "viseme/" + robotId.getRobtIdString() + "/" + speechServiceId;
        ManagedServiceGroup group = new ManagedServiceGroup(fact, 
                getStreamLifecycles(groupId, robotId, speechServiceId, visemeConfigStream), 
                groupId, null);
        group.start();
        return group;
    }
    
    private static List<ServiceLifecycleProvider> getStreamLifecycles(
            String groupId, Robot.Id robotId, String speechServiceId, 
            InputStream visemeConfigStream){
        String configFileId = groupId +"/config/avro";
        return Arrays.asList(
                getBindingConfigStreamLifecycle(configFileId,visemeConfigStream),
                getBindingManagerStreamLifecycle(robotId, speechServiceId, configFileId),
                getVisemeFrameSourceLifecycle(robotId, speechServiceId));
    }
    
    private static ServiceLifecycleProvider getBindingConfigStreamLifecycle(
            String paramId, InputStream stream){
        Properties props = new Properties();
        props.put(Constants.CONFIG_PARAM_ID, paramId);
        props.put(Constants.CONFIG_FORMAT_VERSION, 
                VisemeBindingManagerConfigAvroStreamLoader.VERSION.toString());
        ServiceLifecycleProvider lifecycle = 
                new SimpleLifecycle(stream, InputStream.class, props);
        return lifecycle;
    }
}
