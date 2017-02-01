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

import org.jflux.api.common.rk.property.PropertyChangeAction;
import org.jflux.api.common.rk.property.PropertyChangeMonitor;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.api.motion.servos.config.ServoControllerConfig;
import org.mechio.api.motion.servos.utils.ConnectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides common functionality for ServoControllers.
 *
 * @param <IdType>         Id Type use by this ServoController's Servos
 * @param <S>              Servo type used by this AbstractServoController
 * @param <ControllerConf> ServoControllerConfig type used
 * @param <ServoConf>      ServoConfig type used by S and Conf
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class AbstractServoController<
		IdType,
		ServoConf extends ServoConfig<IdType>,
		S extends Servo<IdType, ServoConf>,
		ControllerConf extends ServoControllerConfig<IdType, ServoConf>
		> extends PropertyChangeNotifier implements
		ServoController<IdType, ServoConf, S, ControllerConf> {
	private static final Logger theLogger = LoggerFactory.getLogger(AbstractServoController.class);
	private ServoController.Id myServoControllerId;
	/**
	 * ServoController's Confgiuration parameters.
	 */
	protected ControllerConf myConfig;
	/**
	 * List of the Coontroller's Servos.
	 */
	protected List<S> myServos;
	/**
	 * Map of the Controller's Servos and their ids.
	 */
	protected Map<ServoId<IdType>, S> myServoMap;
	/**
	 * The Controller's ConnectionStatus.
	 */
	protected ConnectionStatus myConnectionStatus;
	/**
	 * The Controllers's PropertyChangeMonitor to listen for changes in Servos
	 * and configs.
	 */
	protected PropertyChangeMonitor myChangeMonitor;

	/**
	 * Creates a new AbstractServoController from the given config.
	 *
	 * @param config the Controller's cofig
	 */
	public AbstractServoController(ControllerConf config) {
		if (config == null) {
			throw new NullPointerException();
		}
		ServoController.Id id = config.getServoControllerId();
		if (id == null) {
			throw new NullPointerException();
		}
		myServoControllerId = id;
		initPropertyChangeMontior();
		myConfig = config;
		myConfig.addPropertyChangeListener(myChangeMonitor);
		for (ServoConfig sc : myConfig.getServoConfigs().values()) {
			sc.addPropertyChangeListener(myChangeMonitor);
		}
		myConnectionStatus = ConnectionStatus.DISCONNECTED;
		myServoMap = new HashMap();
		myServos = new ArrayList();
	}

	private void initPropertyChangeMontior() {
		myChangeMonitor = new PropertyChangeMonitor();
		myChangeMonitor.addAction(ServoControllerConfig.PROP_SERVO_ADD,
				new PropertyChangeAction() {
					@Override
					protected void run(PropertyChangeEvent event) {
						ServoConf config = (ServoConf) event.getNewValue();
						addingServo(config);
					}
				});
		myChangeMonitor.addAction(ServoControllerConfig.PROP_SERVO_REMOVE,
				new PropertyChangeAction() {
					@Override
					protected void run(PropertyChangeEvent event) {
						ServoConf config = (ServoConf) event.getNewValue();
						removingServo(servoId(config.getServoId()));
					}
				});
		myChangeMonitor.addAction(ServoConfig.PROP_ID,
				new PropertyChangeAction() {
					@Override
					protected void run(PropertyChangeEvent event) {
						changeServoId((ServoId<IdType>) event.getOldValue(),
								(ServoId<IdType>) event.getNewValue());
					}
				});
	}

	@Override
	public final ServoController.Id getId() {
		return myServoControllerId;
	}

	/**
	 * Called when a new Servo is being added.  The overriding method
	 * should make the Servo available from the given ServoConfig.
	 *
	 * @return a new Servo from the ServoConfig
	 */
	protected abstract S connectServo(ServoConf config);

	/**
	 * Called when a Servo is being removed.  The overriding method should
	 * remove references to the Servo from the ServoController.
	 *
	 * @param id Servo's id
	 * @return true if successful
	 */
	protected abstract boolean disconnectServo(ServoId<IdType> id);

	/**
	 * Add a Servo the ServoController.
	 *
	 * @param config Servo's configuration parameters
	 */
	public void addServo(ServoConf config) {
		myConfig.addServoConfig(config);
	}

	/**
	 * Remove a Servo from the ServoController.
	 *
	 * @param servo Servo to remove
	 */
	public void removeServo(S servo) {
		myConfig.removeServoConfig(servo.getConfig());
	}

	/**
	 * Set the ServoController's ConnectionStatus.
	 *
	 * @param status new ConnectionStatus
	 */
	protected void setConnectStatus(ConnectionStatus status) {
		ConnectionStatus oldStatus = myConnectionStatus;
		myConnectionStatus = status;
		firePropertyChange(PROP_CONNECTION_STATUS, oldStatus, status);
	}

	@Override
	public S getServo(ServoId<IdType> id) {
		if (ConnectionStatus.CONNECTED != myConnectionStatus) {
			theLogger.warn("Must be connected to access Servos");
			return null;
		}
		S Servo = myServoMap.get(id);
		return Servo;
	}

	@Override
	public List<S> getServos() {
		if (ConnectionStatus.CONNECTED != myConnectionStatus) {
			theLogger.warn("Must be connected to access Servos");
			return Collections.EMPTY_LIST;
		}
		return myServos;
	}

	@Override
	public boolean containsIds(Set<ServoId<IdType>> ids) {
		if (ConnectionStatus.CONNECTED != myConnectionStatus) {
			theLogger.warn("Must be connected to access Servos");
			return false;
		}
		return myServoMap.keySet().containsAll(ids);
	}

	@Override
	public boolean containsId(ServoId<IdType> id) {
		if (ConnectionStatus.CONNECTED != myConnectionStatus) {
			theLogger.warn("Must be connected to access Servos");
			return false;
		}
		return myServoMap.containsKey(id);
	}

	@Override
	public ConnectionStatus getConnectionStatus() {
		return myConnectionStatus;
	}

	/**
	 * Called when a Servo is added to the underlying ServoControllerConfig.
	 *
	 * @param config the Servo's configuration parameters
	 */
	protected void addingServo(ServoConf config) {
		S servo = connectServo(config);
		if (servo == null) {
			return;
		}
		myServos.add(servo);
		myServoMap.put(servoId(servo.getId()), servo);
		servo.getConfig().addPropertyChangeListener(myChangeMonitor);
		firePropertyChange(PROP_SERVO_ADD, null, servo);
	}

	/**
	 * Called when a Servo is removed from the underlying ServoControllerConfig.
	 *
	 * @param id id of the Servo being removed
	 */
	protected void removingServo(ServoId<IdType> id) {
		if (!disconnectServo(id)) {
			return;
		}
		S servo = myServoMap.remove(id);
		myServos.remove(servo);
		servo.getConfig().removePropertyChangeListener(myChangeMonitor);
		firePropertyChange(PROP_SERVO_REMOVE, null, servo);
	}

	/**
	 * Called when the id of a Servo changes in the underlying
	 * ServoControllerConfig.
	 *
	 * @param oldId old id
	 * @param newId new id
	 */
	protected void changeServoId(ServoId<IdType> oldId, ServoId<IdType> newId) {
		S servo = myServoMap.remove(oldId);
		myServoMap.put(newId, servo);
		firePropertyChange(PROP_SERVOS, null, myServoMap);
	}

	@Override
	public ControllerConf getConfig() {
		return myConfig;
	}

	/**
	 * Creates a ServoController.ServoId from this ServoController's Id and a
	 * Servo's Id
	 *
	 * @param id Servo Id to use
	 * @return ServoController.ServoId containing this ServoController's Id and the give Servo Id
	 */
	protected ServoId<IdType> servoId(IdType id) {
		if (id == null) {
			throw new NullPointerException();
		}
		return new ServoController.ServoId<>(myServoControllerId, id);
	}
}
