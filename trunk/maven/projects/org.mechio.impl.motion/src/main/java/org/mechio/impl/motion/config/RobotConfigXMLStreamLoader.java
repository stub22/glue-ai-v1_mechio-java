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

package org.mechio.impl.motion.config;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ConfigurationLoader;
import org.mechio.api.motion.servos.config.ServoRobotConfig;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotConfigXMLStreamLoader
		implements ConfigurationLoader<ServoRobotConfig, InputStream> {
	private static final Logger theLogger = LoggerFactory.getLogger(RobotConfigXMLStreamLoader.class);
	private BundleContext myContext;

	public RobotConfigXMLStreamLoader(BundleContext context) {
		if (context == null) {
			throw new NullPointerException();
		}
		myContext = context;
	}

	@Override
	public VersionProperty getConfigurationFormat() {
		return RobotConfigXMLReader.VERSION;
	}

	@Override
	public ServoRobotConfig loadConfiguration(InputStream param)
			throws ConfigurationException {
		XMLConfiguration config = new XMLConfiguration();
		config.load(param);
		return RobotConfigXMLReader.readConfig(myContext, config);
	}

	@Override
	public Class<ServoRobotConfig> getConfigurationClass() {
		return ServoRobotConfig.class;
	}

	@Override
	public Class<InputStream> getParameterClass() {
		return InputStream.class;
	}
}
