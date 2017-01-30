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
package org.mechio.api.motion.utils;

import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceGroup;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.jflux.impl.services.rk.osgi.SingleServiceListener;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.blending.FrameSource;
import org.mechio.api.motion.lifecycle.DefaultBlenderServiceGroup;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Common Utility methods for the Motion API
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotUtils {
	private static final Logger theLogger = LoggerFactory.getLogger(RobotUtils.class);
	/**
	 * Default milliseconds for a Blender interval
	 */
	public final static long DEFAULT_BLENDER_INTERVAL = 40L;

	private static SingleServiceListener<RobotManager> theManagerTracker;

	/**
	 * Returns a RobotPositionMap of the current positions for the Robot with
	 * the given Id.  If supported, this will communicate with the Joints
	 * themselves to get the current positions
	 *
	 * @param context BundleContext used to locate the robot
	 * @param robotId identifies the Robot to use
	 * @return RobotPositionMap of the current Positions for the Robot with the given Id
	 */
	public static RobotPositionMap getCurrentPositions(
			BundleContext context, Robot.Id robotId) {
		ServiceReference ref = getRobotReference(context, robotId);
		if (ref == null) {
			return null;
		}
		Robot robot = getRobot(context, ref);
		if (robot == null) {
			return null;
		}
		RobotPositionMap pos = robot.getCurrentPositions();
		context.ungetService(ref);
		return pos;
	}

	/**
	 * Returns a RobotPositionMap of the goal positions for the Robot with
	 * the given Id.
	 *
	 * @param context BundleContext used to locate the robot
	 * @param robotId identifies the Robot to use
	 * @return RobotPositionMap of the current Positions for the Robot with the given Id
	 */
	public static RobotPositionMap getGoalPositions(
			BundleContext context, Robot.Id robotId) {
		ServiceReference ref = getRobotReference(context, robotId);
		if (ref == null) {
			return null;
		}
		Robot dev = getRobot(context, ref);
		if (dev == null) {
			return null;
		}
		RobotPositionMap pos = dev.getGoalPositions();
		context.ungetService(ref);
		return pos;
	}

	/**
	 * Returns a RobotPositionMap of the default positions for the Robot with
	 * the given Id.
	 *
	 * @param context BundleContext used to locate the robot
	 * @param robotId identifies the Robot to use
	 * @return RobotPositionMap of the default Positions for the Robot with the given Id
	 */
	public static RobotPositionMap getDefaultPositions(
			BundleContext context, Robot.Id robotId) {
		ServiceReference ref = getRobotReference(context, robotId);
		if (ref == null) {
			return null;
		}
		Robot dev = getRobot(context, ref);
		if (dev == null) {
			return null;
		}
		RobotPositionMap pos = dev.getDefaultPositions();
		context.ungetService(ref);
		return pos;
	}

	private static Robot getRobot(
			BundleContext context, ServiceReference ref) {
		Object obj = context.getService(ref);
		if (!(obj instanceof Robot)) {
			context.ungetService(ref);
			return null;
		}
		return (Robot) obj;
	}

	/**
	 * Checks if the given robotId is available to use.
	 * Returns true if the robotId is not found in the OSGi Service Registry.
	 *
	 * @param context BundleContext to use
	 * @param robotId Robot.Id to check
	 * @return true if the robotId is not found in the OSGi Service Registry
	 */
	public static boolean isRobotIdAvailable(
			BundleContext context, Robot.Id robotId) {
		return getRobotReference(context, robotId) == null;
	}

	/**
	 * Adds a Robot to the OSGi ServiceRegistry with the robotId as a
	 * service property.  Returns the ServiceRegistration object or null if
	 * unable to register.
	 *
	 * @param context BundleContext to use
	 * @param robot   Robot to register
	 * @param props   option service properties to add to the registration
	 * @return ServiceRegistration object or null if unable to register
	 */
	public static ServiceRegistration registerRobot(
			BundleContext context, Robot robot, Properties props) {
		Robot.Id id = robot.getRobotId();
		Dictionary<String, Object> propTable = new Hashtable<>();
		if (!isRobotIdAvailable(context, id)) {
			theLogger.warn("Unable to register Robot.  Id in use or invalid");
			return null;
		}
		if (props != null) {
			for (Object prop : props.keySet()) {
				propTable.put(prop.toString(), props.get(prop));
			}
		}
		propTable.put(Robot.PROP_ID, id.toString());
		ServiceRegistration reg =
				context.registerService(Robot.class.getName(), robot, propTable);

		theLogger.info("Robot Service Registered.");
		RobotManager manager = getRobotManager(context);
		manager.addRobot(robot);
		return reg;
	}

	/**
	 * Finds ServiceReferences for a Robot with the given id.  Returns null if
	 * a ServiceReference could not be found.
	 * The ServiceReference can be used to fetch the registered robot using
	 * context.getService(serviceReference).  This should be followed with
	 * context.ungetService(serviceReference) when finished using the Robot.
	 * Runtime Exceptions should be expected when working with a
	 * ServiceReference as the Service can be unregistered at any time.
	 *
	 * @param context BundleContext used to retrieve a ServiceReference
	 * @param robotId the id to filter by
	 * @return ServiceReference to a Robot with a matching id, or null if a robot is not found.
	 * @throws NullPointerException     if context or robotId are null
	 * @throws IllegalArgumentException if robotId is empty
	 */
	public static ServiceReference getRobotReference(
			BundleContext context, Robot.Id robotId) {
		if (context == null || robotId == null) {
			throw new NullPointerException();
		}
		String filter = String.format(
				"(%s=%s)", Robot.PROP_ID, robotId.toString());
		try {
			ServiceReference[] refs = context.getAllServiceReferences(
					Robot.class.getName(), filter);
			if (refs == null || refs.length == 0) {
				return null;
			}
			if (refs.length > 1) {
				theLogger.warn("Found multiple Robots with given id: {}", robotId);
			}
			return refs[0];
		} catch (InvalidSyntaxException ex) {
			theLogger.warn("Unable to use robotId.  "
					+ "Given robotId results in invalid OSGi filter.", ex);
			return null;
		}
	}

	/**
	 * Creates a DefaultBlender for the Robot with the given robotId.
	 * The Blender runs on a timer with the given interval in milliseconds.
	 * If existing components are already registered, this will fail and return
	 * null.
	 *
	 * @param context             BundleContext to use
	 * @param robotId             Robot.Id to use
	 * @param blenderIntervalMsec Blender timer interval
	 * @return array of ServiceRegistrations from adding Blender components to the OSGi Service
	 * Registry, these are used to unregister the components if needed
	 */
	public static List<ManagedService> startDefaultBlender(
			BundleContext context, Robot.Id robotId, long blenderIntervalMsec) {
		if (context == null || robotId == null) {
			throw new NullPointerException();
		}
		return launchDefaultBlender(context, robotId, blenderIntervalMsec);
	}

	public static List<ManagedService> launchDefaultBlender(
			BundleContext context, Robot.Id robotId, long blenderIntervalMsec) {
		ManagedServiceGroup bsg = new DefaultBlenderServiceGroup(
				context, robotId, blenderIntervalMsec, null);

		bsg.start();
		return bsg.getServices();
	}

	/**
	 * Registers a FrameSource using the given Robot.Id as a property.
	 *
	 * @param context     BundleContext to use
	 * @param robotId     Robot.Id to associate with the FrameSource
	 * @param frameSource FrameSource to register
	 * @return ServiceRegistration for this FrameSource, this is used to unregister the FrameSource
	 */
	public static ServiceRegistration registerFrameSource(
			BundleContext context, Robot.Id robotId, FrameSource frameSource) {
		if (context == null || robotId == null || frameSource == null) {
			throw new NullPointerException();
		}
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(Robot.PROP_ID, robotId.toString());
		String clazz = FrameSource.class.getName();
		ServiceRegistration reg =
				context.registerService(clazz, frameSource, props);
		if (reg != null) {
			theLogger.info("FrameSource successfully registered for {}.",
					robotId.toString());
		} else {
			theLogger.info("Unable to register FrameSource for {}.",
					robotId.toString());
		}
		return reg;
	}

	/**
	 * Returns an OSGi filter String for matching the given Robot.Id.
	 *
	 * @param robotId the Robot.Id to match
	 * @return OSGi filter String for matching the given Robot.Id
	 */
	public static String getRobotFilter(Robot.Id robotId) {
		if (robotId == null) {
			throw new NullPointerException();
		}
		return OSGiUtils.createServiceFilter(Robot.PROP_ID, robotId.toString());
	}

	/**
	 * Returns an OSGi filter String for matching the given Robot.Id.  If
	 * serviceFilter is not null, the return filter String will match that
	 * filter as well.
	 *
	 * @param robotId       Robot.Id to match
	 * @param serviceFilter addition filter to match
	 * @return filter string matching the Robot.Id and given serviceFilter
	 */
	public static String getRobotFilter(Robot.Id robotId, String serviceFilter) {
		if (robotId == null) {
			throw new NullPointerException();
		}
		return OSGiUtils.createIdFilter(
				Robot.PROP_ID, robotId.toString(), serviceFilter);
	}

	/**
	 * Converts a RobotPositionMap to a Map of Integers to Doubles.
	 * If the Robot contains Joints with duplicate Joint Ids, the size of the
	 * RobotPositionMap will be added to the id until it is unique.
	 *
	 * @param posMap RobotPositionMap to convert
	 * @return Map of Integer ids to Doubles
	 */
	public static Map<Integer, Double> convertMap(RobotPositionMap posMap) {
		Map<Integer, Double> map = new HashMap<>();
		int size = posMap.size();
		for (Entry<Robot.JointId, NormalizedDouble> e : posMap.entrySet()) {
			int i = e.getKey().getJointId().getLogicalJointNumber();
			while (map.containsKey(i)) {
				i += size;
			}
			double pos = e.getValue().getValue();
			map.put(i, pos);
		}
		return map;
	}

	/**
	 * Returns a global RobotManager.  The RobotManager is retrieved from the
	 * OSGi Service Registry.
	 *
	 * @param context BundleContext for OSGi
	 * @return global RobotManager
	 */
	public static RobotManager getRobotManager(BundleContext context) {
		if (context == null) {
			throw new NullPointerException();
		}
		if (theManagerTracker == null) {
			theManagerTracker = new SingleServiceListener(
					RobotManager.class, context, null);
			theManagerTracker.start();
		}
		RobotManager manager = theManagerTracker.getService();
		if (manager == null) {
			RobotManager tmp = new RobotManager(context);
			context.registerService(RobotManager.class.getName(), tmp, null);
			manager = theManagerTracker.getService();
		}
		if (manager == null) {
			throw new NullPointerException(
					"Unknown error registering and tracking RobotManager.");
		}
		return manager;
	}
}
