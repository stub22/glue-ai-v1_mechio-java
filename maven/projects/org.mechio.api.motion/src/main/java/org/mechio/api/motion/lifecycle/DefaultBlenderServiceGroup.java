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
package org.mechio.api.motion.lifecycle;

import org.jflux.api.common.rk.utils.Utils;
import org.jflux.api.core.Source;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceGroup;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionHashMap;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.blending.Blender;
import org.mechio.api.motion.blending.FrameCombiner;
import org.mechio.api.motion.blending.FrameSourceTracker;
import org.mechio.api.motion.blending.NaiveMotionFrameAverager;
import org.mechio.api.motion.blending.OSGiFrameSourceTracker;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DefaultBlenderServiceGroup extends ManagedServiceGroup {
	private static final Logger theLogger = LoggerFactory.getLogger(DefaultBlenderServiceGroup.class);

	private static String getIdBase(Robot.Id robotId) {
		String base = "robot/" + robotId + "/blender";
		//TODO: sanitize base
		return base;
	}

	public DefaultBlenderServiceGroup(
			BundleContext context, Robot.Id robotId,
			long blenderIntervalMillisec, Properties registrationProperties) {
		super(new OSGiComponentFactory(context),
				getBlenderLifecycles(context, robotId, blenderIntervalMillisec),
				getIdBase(robotId),
				registrationProperties);
	}

	private static List<ServiceLifecycleProvider> getBlenderLifecycles(
			BundleContext context, Robot.Id robotId, long blenderInterval) {
		blenderInterval = validateInterval(blenderInterval);
		List<ServiceLifecycleProvider> services = new ArrayList();
		services.add(new RobotBlenderLifecycle(robotId));
		services.add(new RobotOutputLifecycle(robotId));
		services.add(new TimedBlenderDriverLifecycle(robotId, blenderInterval));
		services.add(buildFrameSourceTrackerLauncher(context, robotId));
		services.add(buildFrameCombinerLauncher(robotId));
		return services;
	}

	private static long validateInterval(long interval) {
		return Utils.bound(interval, 1, Integer.MAX_VALUE - 1);
	}

	private static ServiceLifecycleProvider<FrameSourceTracker>
	buildFrameSourceTrackerLauncher(
			BundleContext context, Robot.Id robotId) {
		Properties props = new Properties();
		props.put(Robot.PROP_ID, robotId.getRobtIdString());
		OSGiFrameSourceTracker tracker = new OSGiFrameSourceTracker();
		tracker.init(context, OSGiUtils.createServiceFilter(props));
		props.put(Blender.PROP_POSITION_MAP_TYPE,
				RobotPositionMap.class.getName());
		return new SimpleLifecycle<>(
				tracker, FrameSourceTracker.class, props);
	}

	private static ServiceLifecycleProvider<FrameCombiner>
	buildFrameCombinerLauncher(Robot.Id robotId) {
		Properties props = new Properties();
		props.put(Robot.PROP_ID, robotId.getRobtIdString());
		props.put(Blender.PROP_POSITION_MAP_TYPE,
				RobotPositionMap.class.getName());
		return new SimpleLifecycle<>(
				getFrameCombiner(), FrameCombiner.class, props);
	}

	private static FrameCombiner getFrameCombiner() {
		Source<RobotPositionMap> factory = new Source<RobotPositionMap>() {
			@Override
			public RobotPositionMap getValue() {
				return new RobotPositionHashMap();
			}
		};
		return new NaiveMotionFrameAverager<>(factory);
	}
}
