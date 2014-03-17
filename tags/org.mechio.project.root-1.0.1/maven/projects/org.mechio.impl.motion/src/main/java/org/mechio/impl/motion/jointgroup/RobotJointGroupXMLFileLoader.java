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

package org.mechio.impl.motion.jointgroup;

import org.mechio.api.motion.jointgroup.RobotJointGroupConfig;
import java.io.File;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ConfigurationLoader;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotJointGroupXMLFileLoader
        implements ConfigurationLoader<RobotJointGroupConfig, File>{
    private final static Logger theLogger = 
            Logger.getLogger(RobotJointGroupXMLFileLoader.class.getName());

    @Override
    public VersionProperty getConfigurationFormat() {
        return RobotJointGroupConfigXMLReader.VERSION;
    }

    @Override
    public RobotJointGroupConfig loadConfiguration(File param) 
            throws ConfigurationException {
        HierarchicalConfiguration config = new XMLConfiguration(param);
        return RobotJointGroupConfigXMLReader.readJointGroup(null, config);
    }

    @Override
    public Class<RobotJointGroupConfig> getConfigurationClass() {
        return RobotJointGroupConfig.class;
    }

    @Override
    public Class<File> getParameterClass() {
        return File.class;
    }
}
