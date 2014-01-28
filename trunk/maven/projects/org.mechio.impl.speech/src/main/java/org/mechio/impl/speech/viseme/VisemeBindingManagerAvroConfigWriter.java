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
package org.mechio.impl.speech.viseme;

import java.io.File;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ConfigurationWriter;
import org.jflux.impl.messaging.rk.common.AvroUtils;
import org.mechio.api.speech.viseme.config.VisemeBindingManagerConfig;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeBindingManagerAvroConfigWriter implements ConfigurationWriter<VisemeBindingManagerConfig, File> {

    @Override
    public VersionProperty getConfigurationFormat() {
        return VisemeBindingManagerAvroConfigLoader.VERSION;
    }

    @Override
    public boolean writeConfiguration(VisemeBindingManagerConfig config, File param) throws Exception {
        if(config == null || param == null){
            throw new NullPointerException();
        }
        VisemeBindingManagerConfigRecord record = 
                (VisemeBindingManagerConfigRecord)config;
        return AvroUtils.writeToFile(
                record, VisemeBindingManagerConfigRecord.SCHEMA$, param, true);
    }

    @Override
    public Class<VisemeBindingManagerConfig> getConfigurationClass() {
        return VisemeBindingManagerConfig.class;
    }

    @Override
    public Class<File> getParameterClass() {
        return File.class;
    }
    
}
