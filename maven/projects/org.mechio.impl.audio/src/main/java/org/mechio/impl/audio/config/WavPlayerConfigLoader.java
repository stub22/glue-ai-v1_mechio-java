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

import java.io.File;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ConfigurationLoader;
import org.jflux.impl.messaging.rk.common.AvroUtils;
import org.mechio.api.audio.config.WavPlayerConfig;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class WavPlayerConfigLoader implements ConfigurationLoader<WavPlayerConfig, File> {
    /**
     * Config format version name.
     */
    public final static String VERSION_NAME = "AvroWavPlayerConfig";
    /**
     * Config format version number.
     */
    public final static String VERSION_NUMBER = "1.0";
    /**
     * Config format VersionProperty.
     */
    public final static VersionProperty VERSION = new VersionProperty(VERSION_NAME, VERSION_NUMBER);

    @Override
    public VersionProperty getConfigurationFormat() {
        return VERSION;
    }

    @Override
    public WavPlayerConfig loadConfiguration(File param) throws Exception {
        return AvroUtils.readFromFile(
                WavPlayerConfigRecord.class, null, 
                WavPlayerConfigRecord.SCHEMA$, param, true);
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
