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
package org.mechio.api.audio.config;

import java.io.File;
import java.util.Properties;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.mechio.api.audio.WavBufferPlayer;
import org.mechio.api.audio.WavBuffer;
import org.mechio.api.audio.WavPlayer;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class WavPlayerLoader {

    public static ServiceRegistration registerWavPlayer(
            BundleContext context, WavPlayer player, Properties props){
        return OSGiUtils.registerUniqueService(context, 
                WavPlayer.class.getName(), 
                WavPlayer.PROP_WAV_PLAYER_ID, 
                player.getWavPlayerId(), 
                player, props);
    }
    
    public static WavPlayer loadPlayer(WavPlayerConfig conf) throws Exception{
        if(conf == null){
            throw new NullPointerException();
        }
        File wavFile = new File(conf.getWavLocation());
        WavBuffer buffer = new WavBuffer(wavFile);
        WavPlayer player = new WavBufferPlayer(conf.getWavPlayerId(), buffer);
        return player;
    }
}
