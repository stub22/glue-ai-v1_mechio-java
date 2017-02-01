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

package org.mechio.api.motion.servos.utils;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ServiceFactory;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.servos.ServoRobot;
import org.mechio.api.motion.servos.config.ServoRobotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServiceFactory for creating new ServoRobots from ServoRobotConfigs.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ServoRobotConnector implements ServiceFactory<Robot, ServoRobotConfig> {
	private static final Logger theLogger = LoggerFactory.getLogger(ServoRobotConnector.class);

	@Override
	public VersionProperty getServiceVersion() {
		return ServoRobot.VERSION;
	}

	@Override
	public Robot build(ServoRobotConfig config) {
		return new ServoRobot(config);
	}

	@Override
	public Class<Robot> getServiceClass() {
		return Robot.class;
	}

	@Override
	public Class<ServoRobotConfig> getConfigurationClass() {
		return ServoRobotConfig.class;
	}
}
