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

package org.mechio.impl.motion.pololu;

import org.jflux.api.common.rk.config.VersionProperty;
import org.mechio.api.motion.servos.config.DefaultServoConfig;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.impl.motion.pololu.MaestroServo.Id;
import org.mechio.impl.motion.serial.SerialConfigXMLReader;
import org.mechio.impl.motion.serial.SerialServoControllerConfig;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class MaestroControllerConfig extends SerialServoControllerConfig<
        MaestroServo.Id, ServoConfig<MaestroServo.Id>> {
    
    public static class Reader extends SerialConfigXMLReader<
            MaestroServo.Id,
            ServoConfig<MaestroServo.Id>,
            MaestroControllerConfig>{
        /**
         * Config format version name.
         */
        public final static String CONFIG_TYPE = "Maestro XML Config";
        /**
         * Config format version number.
         */
        public final static String CONFIG_VERSION = "1.0";
        /**
         * Config format VersionProperty.
         */
        public final static VersionProperty VERSION = new VersionProperty(CONFIG_TYPE, CONFIG_VERSION);
        
        public Reader(){
            super(new MaestroServoIdReader());
        }

        @Override
        protected MaestroControllerConfig newConfig() {
            return new MaestroControllerConfig();
        }

        @Override
        protected ServoConfig<Id> newServoConfig(
                Id id, String name, int minPos, int maxPos, int defPos) {
            return new DefaultServoConfig<MaestroServo.Id>(
                    id, name, minPos, maxPos, defPos);
        }

        @Override
        public VersionProperty getConfigurationFormat() {
            return VERSION;
        }

        @Override
        public Class<MaestroControllerConfig> getConfigurationClass() {
            return MaestroControllerConfig.class;
        }
    }
    
}
