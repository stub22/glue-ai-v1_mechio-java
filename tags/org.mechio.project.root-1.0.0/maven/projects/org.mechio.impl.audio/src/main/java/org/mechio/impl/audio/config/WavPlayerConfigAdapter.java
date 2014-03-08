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
package org.mechio.impl.audio.config;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ConfigurationAdapter;
import org.mechio.api.audio.WavBufferPlayer;
import org.mechio.api.audio.WavPlayer;
import org.mechio.api.audio.config.WavPlayerConfig;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class WavPlayerConfigAdapter 
        implements ConfigurationAdapter<WavPlayer, WavPlayerConfig> {

    @Override
    public WavPlayerConfig createConfig(WavPlayer service) {
        WavPlayerConfigRecord.Builder builder =
                WavPlayerConfigRecord.newBuilder();
        builder.setWavPlayerId(service.getWavPlayerId());
        builder.setWavLocation(service.getWavBuffer().getAudioLocation());
        builder.setStartTimeMicrosec((long)service.getStartPositionMicrosec());
        builder.setStopTimeMicrosec((long)service.getEndPositionMicrosec());
        builder.setStartDelayMillisec(service.getStartDelayMillisec());
        WavPlayerConfigRecord config = builder.build();
        return config;
    }

    @Override
    public VersionProperty getServiceVersion() {
        return WavBufferPlayer.VERSION;
    }

    @Override
    public VersionProperty getConfigurationFormat() {
        return WavPlayerConfigLoader.VERSION;
    }

    @Override
    public Class<WavPlayer> getSerivceClass() {
        return WavPlayer.class;
    }

    @Override
    public Class<WavPlayerConfig> getConfigurationClass() {
        return WavPlayerConfig.class;
    }
    
}
