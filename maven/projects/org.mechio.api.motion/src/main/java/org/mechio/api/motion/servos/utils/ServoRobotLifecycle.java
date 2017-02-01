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
import org.jflux.api.common.rk.osgi.lifecycle.ConfiguredServiceLifecycle;
import org.jflux.api.common.rk.osgi.lifecycle.ConfiguredServiceParams;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceFactory;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.servos.ServoController;
import org.mechio.api.motion.servos.ServoRobot;
import org.mechio.api.motion.servos.config.ServoControllerConfig;
import org.mechio.api.motion.servos.config.ServoRobotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ServoRobotLifecycle<P> extends
		ConfiguredServiceLifecycle<Robot, ServoRobotConfig, P> {
	private static final Logger theLogger = LoggerFactory.getLogger(ServoRobotLifecycle.class);
	private ManagedServiceFactory myServiceFactory;
	private List<ManagedService> myServoControllerServices;

	public ServoRobotLifecycle(
			ConfiguredServiceParams<Robot, ServoRobotConfig, P> params,
			ManagedServiceFactory mangedServiceFactory) {
		super(params);
		if (mangedServiceFactory == null) {
			throw new NullPointerException();
		}
		myServiceFactory = mangedServiceFactory;
		myServoControllerServices = new ArrayList<>();
	}

	@Override
	protected Robot create(Map dependencies) {
		Robot r = super.create(dependencies);
		if (r == null) {
			return null;
		}
		r.connect();
		if (r instanceof ServoRobot) {
			ServoRobot sr = (ServoRobot) r;
			for (ServoController sc : sr.getControllerList()) {
				ServoControllerConfig config =
						(sc == null) ? null : sc.getConfig();
				VersionProperty prop =
						(config == null) ? null : config.getControllerTypeVersion();
				Properties props = new Properties();
				if (prop != null) {
					props.put(ServoController.PROP_VERSION, prop.toString());
				} else {
					props.put(ServoController.PROP_VERSION, "UNKNOWN");
				}
				Robot.Id robotId = r.getRobotId();
				if (r != null) {
					props.put(Robot.PROP_ID, robotId.getRobtIdString());
				} else {
					props.put(Robot.PROP_ID, "UNKNOWN");
				}
				ServiceLifecycleProvider lifecycle =
						new SimpleLifecycle(sc, ServoController.class);
				ManagedService service =
						myServiceFactory.createService(lifecycle, props);
				myServoControllerServices.add(service);
				service.start();
			}
		}
		return r;
	}

	@Override
	protected void cleanupService(Robot service) {
		try {
			service.disconnect();
			for (ManagedService s : myServoControllerServices) {
				try {
					s.dispose();
				} catch (Throwable t) {
					theLogger.warn("Error disposing controller service.", t);
				}
			}
		} catch (Exception ex) {
			theLogger.warn(ex.getMessage(), ex);
		}
		super.cleanupService(service);
	}

}
