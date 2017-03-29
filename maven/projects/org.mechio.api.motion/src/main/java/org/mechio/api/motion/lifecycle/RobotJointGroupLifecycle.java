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

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.osgi.lifecycle.ConfiguredServiceLifecycle;
import org.jflux.api.common.rk.osgi.lifecycle.ConfiguredServiceParams;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorBuilder;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.jointgroup.JointGroup;
import org.mechio.api.motion.jointgroup.RobotJointGroup;
import org.mechio.api.motion.jointgroup.RobotJointGroupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
@Deprecated
public class RobotJointGroupLifecycle<P> extends
		ConfiguredServiceLifecycle<JointGroup, RobotJointGroupConfig, P> {
	private static final Logger theLogger = LoggerFactory.getLogger(RobotJointGroupLifecycle.class);
	private final static String theRobot = "robot";

	public RobotJointGroupLifecycle(Robot.Id robotId, Class<P> paramClass,
									String paramId, VersionProperty configFormat) {
		super(new ConfiguredServiceParams(
				JointGroup.class, RobotJointGroupConfig.class, paramClass,
				null, null, paramId, RobotJointGroup.VERSION, configFormat));
		getDependencyDescriptors().add(
				new DescriptorBuilder(theRobot, Robot.class)
						.with(Robot.PROP_ID, robotId.getRobtIdString())
						.getDescriptor());
		if (myRegistrationProperties == null) {
			myRegistrationProperties = new Properties();
		}
		myRegistrationProperties.put(Robot.PROP_ID, robotId.getRobtIdString());
	}

	@Override
	protected synchronized JointGroup create(Map<String, Object> dependencies) {
		JointGroup group = super.create(dependencies);
		if (group == null) {
			return null;
		}
		Robot robot = (Robot) dependencies.get(theRobot);
		setRobot(group, robot);
		return group;
	}

	@Override
	protected void handleChange(String name, Object dependency, Map<String, Object> availableDependencies) {
		super.handleChange(name, dependency, availableDependencies);
		if (myService != null && theRobot.equals(name)) {
			setRobot(myService, (Robot) dependency);
		}
	}

	private void setRobot(JointGroup group, Robot robot) {
		if (group == null) {
			return;
		}
		if (!(group instanceof RobotJointGroup)) {
			theLogger.warn("JointGroup is not a RobotJointGroup.  Found {}.  "
					+ "Unable to set Robot.", group.getClass());
			return;
		}
		((RobotJointGroup) group).setRobot(robot);
	}
}
