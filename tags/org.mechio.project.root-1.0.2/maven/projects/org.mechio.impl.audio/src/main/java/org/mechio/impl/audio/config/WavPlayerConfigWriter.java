package org.mechio.impl.audio.config;


import java.io.File;
import java.io.IOException;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ConfigurationWriter;
import org.jflux.impl.messaging.rk.common.AvroUtils;
import org.mechio.api.audio.config.WavPlayerConfig;

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

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class WavPlayerConfigWriter implements ConfigurationWriter<WavPlayerConfig, File> {
    
    @Override
    public VersionProperty getConfigurationFormat() {
        return WavPlayerConfigLoader.VERSION;
    }

    @Override
    public boolean writeConfiguration(WavPlayerConfig config, File param) throws IOException{
        if(config == null || param == null){
            throw new NullPointerException();
        }
        WavPlayerConfigRecord record;
        if(config instanceof WavPlayerConfigRecord){
            record = (WavPlayerConfigRecord)config;
        }else{
            WavPlayerConfigRecord.Builder builder =
                    WavPlayerConfigRecord.newBuilder();
            builder.setWavPlayerId(config.getWavPlayerId());
            builder.setWavLocation(config.getWavLocation());
            builder.setStartTimeMicrosec(config.getStartTimeMicrosec());
            builder.setStopTimeMicrosec(config.getStopTimeMicrosec());
            builder.setStartDelayMillisec(config.getStartDelayMillisec());
            record = builder.build();
        }
        return AvroUtils.writeToFile( 
                record, WavPlayerConfigRecord.SCHEMA$, param, true);
    }

    @Override
    public Class<WavPlayerConfig> getConfigurationClass() {
        return WavPlayerConfig.class;
    }

    @Override
    public Class<File> getParameterClass() {
        return File.class;
    }
    
}
