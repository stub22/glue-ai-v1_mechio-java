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
package org.mechio.api.motion.servos;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.property.PropertyChangeAction;
import org.jflux.api.common.rk.property.PropertyChangeMonitor;
import org.jflux.api.common.rk.services.ServiceContext;
import org.mechio.api.motion.AbstractRobot;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.servos.ServoController.ServoId;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.api.motion.servos.config.ServoControllerConfig;
import org.mechio.api.motion.servos.config.ServoRobotConfig;
import org.mechio.api.motion.servos.utils.EmptyServoJoint;
import org.mechio.api.motion.servos.utils.ServoJointAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Robot implementation using Servos.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ServoRobot extends AbstractRobot<ServoJoint> {
	private static final Logger theLogger = LoggerFactory.getLogger(ServoRobot.class);
	/**
	 * Controller type version name.
	 */
	public final static String VERSION_NAME = "Servo Robot Implementation";
	/**
	 * Controller type version number.
	 */
	public final static String VERSION_NUMBER = "1.0";
	/**
	 * Controller type VersionProperty.
	 */
	public final static VersionProperty VERSION = new VersionProperty(VERSION_NAME, VERSION_NUMBER);

	private Map<Robot.JointId, ServoController.ServoId> myServoIdMap;
	private Map<ServoController.Id, ServoJointAdapter> myServoJointAdapters;
	private Map<ServoController.Id, ServoController> myControllers;
	private List<ServoController> myControllerList;
	private List<Robot.JointId> myMissingJoints;
	private PropertyChangeMonitor myChangeMonitor;
	private boolean myConnectionFlag;
	private ServoRobotConfig myRobotConfig;

	/**
	 * Creates a new ServoRobot with the given BundleContext and RobotConfig.
	 *
	 * @param config Robot's configuration parameters
	 */
	public ServoRobot(ServoRobotConfig config) {
		super(config.getRobotId());
		myRobotConfig = config;
		myServoIdMap = new HashMap();
		myControllers = new HashMap();
		myControllerList = new ArrayList<>();
		myServoJointAdapters = new HashMap();
		myMissingJoints = new ArrayList();
		myConnectionFlag = false;
		Map<Joint.Id, ServoController.ServoId> ids = config.getIdMap();
		if (ids == null) {
			throw new NullPointerException();
		}
		for (Entry<Joint.Id, ServoController.ServoId> e : ids.entrySet()) {
			Joint.Id jointId = e.getKey();
			ServoController.ServoId servoId = e.getValue();
			if (jointId == null || servoId == null) {
				throw new NullPointerException();
			}
			Robot.JointId jId = new Robot.JointId(getRobotId(), jointId);
			myServoIdMap.put(jId, servoId);
		}
		for (ServoControllerContext scc : config.getControllerContexts()) {
			if (scc == null) {
				throw new NullPointerException();
			}
			ServoController controller = scc.getServoController();
			ServoJointAdapter adapter = scc.getServoJointAdapter();
			if (controller == null || adapter == null) {
				throw new NullPointerException();
			}
			ServoController.Id scId = controller.getId();
			myControllers.put(scId, controller);
			myControllerList.add(controller);
			myServoJointAdapters.put(scId, adapter);
		}
		initChangeMonitor();
		setEnabled(true);
	}

	@Override
	public void setEnabled(boolean val) {
		super.setEnabled(val);
		for (ServoController sc : myControllerList) {
			sc.setEnabled(val);
		}
	}

	private void initChangeMonitor() {
		myChangeMonitor = new PropertyChangeMonitor();
		PropertyChangeAction pca = new PropertyChangeAction() {
			@Override
			protected void run(PropertyChangeEvent event) {
				jointsChanged(event);
			}
		};
		myChangeMonitor.addAction(ServoController.PROP_SERVOS, pca);
		myChangeMonitor.addAction(ServoController.PROP_SERVO_ADD, pca);
		myChangeMonitor.addAction(ServoController.PROP_SERVO_REMOVE, pca);
		for (ServoController c : myControllerList) {
			c.addPropertyChangeListener(myChangeMonitor);
		}
	}

	@Override
	public boolean connect() {
		for (ServoController<?, ?, ?, ?> controller : myControllerList) {
			try {
				controller.connect();
			} catch (Throwable t) {
				theLogger.warn("Unable to connect to ServoController.", t);
			}
		}
		setServos();
		boolean oldVal = myConnectionFlag;
		myConnectionFlag = true;
		firePropertyChange(PROP_CONNECTED, oldVal, myConnectionFlag);
		return true;
	}

	private void setServos() {
		clearJoints();
		myMissingJoints.clear();
		for (Entry<JointId, ServoId> e : myServoIdMap.entrySet()) {
			JointId jId = e.getKey();
			ServoId sId = e.getValue();
			ServoController.Id scId = sId.getControllerId();
			ServoController sc = myControllers.get(scId);
			ServoJointAdapter sja = myServoJointAdapters.get(scId);
			if (sc == null || sja == null) {
				myMissingJoints.add(jId);
				continue;
			}
			Servo s = sc.getServo(sId);
			if (s == null) {
				myMissingJoints.add(jId);
				continue;
			}
			ServoJoint j = sja.getJoint(jId.getJointId(), s);
			if (j == null) {
				myMissingJoints.add(jId);
				continue;
			}
			addJoint(j);
		}

		for (JointId robotJointId : myMissingJoints) {
			Joint.Id jointId = robotJointId.getJointId();
			ServoId sId = myServoIdMap.get(robotJointId);
			ServoController.Id scId = sId.getControllerId();
			ServoConfig servoConf = null;
			for (ServoControllerContext scc : myRobotConfig.getControllerContexts()) {
				ServiceContext<
						ServoController<?, ?, ?, ?>,
						ServoControllerConfig, ?> context = scc.myConnectionContext;
				if (context == null) {
					continue;
				}
				ServoControllerConfig controllerConfig = context.getServiceConfiguration();
				ServoController.Id controllerId = controllerConfig.getServoControllerId();
				if (!scId.equals(controllerId)) {
					continue;
				}
				Map<Object, ServoConfig> servoConfs = controllerConfig.getServoConfigs();
				if (!servoConfs.containsKey(sId.getServoId())) {
					continue;
				}
				servoConf = servoConfs.get(sId.getServoId());
			}
			if (servoConf == null) {
				String name = "Joint - " + jointId.toString();
				ServoJoint j = new EmptyServoJoint(jointId, name, new NormalizedDouble(0.5));
				addJoint(j);
			} else {
				String name = servoConf.getName();
				int min = servoConf.getMinPosition();
				int max = servoConf.getMaxPosition();

				ServoJoint j = new EmptyServoJoint(jointId, name, new NormalizedDouble(0.5), min, max);
				addJoint(j);
			}
		}
	}

	@Override
	public void disconnect() {
		for (ServoController jc : myControllerList) {
			jc.disconnect();
		}
		boolean oldVal = myConnectionFlag;
		myConnectionFlag = false;
		firePropertyChange(PROP_CONNECTED, oldVal, myConnectionFlag);
	}

	@Override
	public boolean isConnected() {
		return myConnectionFlag;
	}

	@Override
	public void move(RobotPositionMap positions, long lenMillisec) {
		if (!isConnected() || !isEnabled()) {
			return;
		}
		if (myJointMap == null) {
			throw new NullPointerException();
		}
		for (Entry<Robot.JointId, NormalizedDouble> e : positions.entrySet()) {
			Robot.JointId id = e.getKey();
			ServoJoint j = myJointMap.get(id);
			if (j == null) {
				continue;
			}
			NormalizedDouble pos = e.getValue();
			j.setGoalPosition(pos);
		}
		move(lenMillisec, positions.keySet().toArray(new Robot.JointId[0]));
	}


	private void move(long lenMillisec, Robot.JointId... ids) {
		Map<ServoController.Id, List<ServoId>> map = getServoIds(ids);
		for (Entry<ServoController.Id, List<ServoId>> e : map.entrySet()) {
			ServoController.Id scId = e.getKey();
			List<ServoId> sIds = e.getValue();
			moveController(scId, sIds, lenMillisec);
		}
	}

	private void moveController(
			ServoController.Id controllerId,
			List<ServoId> servoIds, long lenMillisec) {
		ServoController sc = myControllers.get(controllerId);
		if (sc == null) {
			return;
		}
		Class idType = sc.getServoIdClass();
		ServoId[] ids = new ServoId[servoIds.size()];
		int i = 0;
		for (ServoId sId : servoIds) {
			Object id = sId.getServoId();
			if (!idType.isAssignableFrom(id.getClass())) {
				continue;
			}
			ids[i] = sId;
			i++;
		}
		sc.moveServos(ids, i, 0, lenMillisec);
	}

	/**
	 * Returns an unmodifiable Map of ServoController.Ids and ServoControllers.
	 *
	 * @return unmodifiable Map of ServoController.Ids and ServoControllers
	 */
	public Map<ServoController.Id, ServoController> getControllers() {
		return Collections.unmodifiableMap(myControllers);
	}

	/**
	 * Returns a List of  ServoControllers.
	 *
	 * @return List of ServoControllers
	 */
	public List<ServoController> getControllerList() {
		return myControllerList;
	}

	private Map<ServoController.Id, List<ServoId>> getServoIds(JointId... ids) {
		Map<ServoController.Id, List<ServoId>> map = new HashMap();
		for (JointId jointId : ids) {
			if (!jointId.getRobotId().equals(this.getRobotId())) {
				continue;
			}
			ServoController.ServoId sId = myServoIdMap.get(jointId);
			if (sId == null) {
				continue;
			}
			List<ServoId> servoIds = map.get(sId.getControllerId());
			if (servoIds == null) {
				servoIds = new ArrayList<>();
				map.put(sId.getControllerId(), servoIds);
			}
			servoIds.add(sId);
		}
		return map;
	}

	private void jointsChanged(PropertyChangeEvent event) {
		setServos();
	}

	/**
	 * Contains the objects used for loading and creating a ServoController.
	 *
	 * @param <T> Type of Servo to use
	 */
	public static class ServoControllerContext<T extends Servo<?, ?>> {
		private ServiceContext<
				ServoController<?, ?, T, ?>,
				ServoControllerConfig, ?> myConnectionContext;
		private ServoJointAdapter<T, ?> myServoJointAdapter;
		private boolean myInitializedFlag;

		/**
		 * Creates a new ServoControllerContext.
		 *
		 * @param connContext  ConnectionContext for the ServoController
		 * @param jointAdapter JointAdapter for creating Joints from Servos
		 */
		public ServoControllerContext(ServiceContext<
				ServoController<?, ?, T, ?>, ServoControllerConfig, ?> connContext,
									  ServoJointAdapter<T, ?> jointAdapter) {
			if (connContext == null || jointAdapter == null) {
				throw new NullPointerException();
			}
			myInitializedFlag = false;
			myConnectionContext = connContext;
			myServoJointAdapter = jointAdapter;
		}

		/**
		 * Initializes the ServoControllerContext, creating the ServoController.
		 */
		public void initialize() {
			if (myInitializedFlag) {
				return;
			}
			try {
				myConnectionContext.buildService();
			} catch (Exception ex) {
				theLogger.warn("Unable to build Service.", ex);
				return;
			}
			ServoController controller = myConnectionContext.getService();
			if (controller == null) {
				return;
			}
			myInitializedFlag = true;
		}

		/**
		 * Returns the ServoControllerContext's ServoController
		 *
		 * @return ServoControllerContext's ServoController
		 */
		public ServoController getServoController() {
			if (!myInitializedFlag) {
				initialize();
			}
			return myConnectionContext.getService();
		}

		/**
		 * Returns the associated JointAdapter
		 *
		 * @return associated JointAdapter
		 */
		public ServoJointAdapter getServoJointAdapter() {
			return myServoJointAdapter;
		}
	}
}
