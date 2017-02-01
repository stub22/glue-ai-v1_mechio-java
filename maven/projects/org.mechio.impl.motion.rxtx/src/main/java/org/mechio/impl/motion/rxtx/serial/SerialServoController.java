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

package org.mechio.impl.motion.rxtx.serial;

import gnu.io.SerialPort;

import org.jflux.api.common.rk.property.PropertyChangeAction;
import org.mechio.api.motion.servos.AbstractServoController;
import org.mechio.api.motion.servos.Servo;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.api.motion.servos.utils.ConnectionStatus;
import org.mechio.impl.motion.serial.BaudRate;
import org.mechio.impl.motion.serial.SerialServoControllerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * Abstract ServoController which communicates with a Serial Port.
 *
 * @param <J> type of Servo
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class SerialServoController<
		Id,
		ServoConf extends ServoConfig<Id>,
		S extends Servo<Id, ServoConf>,
		ControllerConf extends SerialServoControllerConfig<Id, ServoConf>
		> extends AbstractServoController<Id, ServoConf, S, ControllerConf> {
	private static final Logger theLogger = LoggerFactory.getLogger(SerialServoController.class);

	/**
	 * Underlying serial port.
	 */
	protected RXTXSerialPort myPort;
	/**
	 * Port timeout length.
	 */
	protected int myTimeoutLength;

	/**
	 * Creates a new SerialServoController from the SerialServoControllerConfig.
	 *
	 * @param config ControllerConfig for the new controller
	 */
	public SerialServoController(ControllerConf config) {
		super(config);
		myChangeMonitor.addAction(PROP_ERROR_MESSAGES, new PropertyChangeAction() {
			@Override
			protected void run(PropertyChangeEvent event) {
				firePropertyChange(PROP_ERROR_MESSAGES, null, event.getNewValue());
			}
		});
		myTimeoutLength = 100;
	}

	/**
	 * Sets the port timeout length.
	 *
	 * @param timeout timeout length in milliseconds
	 */
	public void setTimeoutLength(int timeout) {
		myTimeoutLength = timeout;
		if (myPort == null) {
			return;
		}
		myPort.setTimeoutLength(timeout);
	}

	@Override
	public boolean connect() {
		if (ConnectionStatus.DISCONNECTED != myConnectionStatus) {
			theLogger.warn("Error: Port must be disconnected before connecting.");
			return false;
		}
		if (myConfig == null) {
			theLogger.warn("Unable to connect to Serial Port, null Config.");
			return false;
		}
		String portName = myConfig.getPortName();
		if (portName == null) {
			theLogger.warn("Unable to connect to Serial Port, no Port Name.");
			return false;
		}
		BaudRate baudRate = myConfig.getBaudRate();
		if (baudRate == null) {
			theLogger.warn("Unable to connect to Serial Port, no Baud Rate.");
			return false;
		}
		int rate = baudRate.getInt();
		myPort = new RXTXSerialPort(portName);
		myPort.setTimeoutLength(myTimeoutLength);
		myPort.addPropertyChangeListener(myChangeMonitor);
		boolean ret = myPort.connect(rate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		if (!ret) {
			return false;
		}
//        ret = setServos();
//        if(!ret){
//            return false;
//        }
		return setStatusAndReturn(ret, ConnectionStatus.CONNECTED);
	}

	/**
	 * Sets the Servo List and Servo Map of the underlying
	 * AbstractServoController.
	 *
	 * @return true if successful
	 */
	protected abstract boolean setServos();

	@Override
	public boolean disconnect() {
		if (ConnectionStatus.DISCONNECTED == myConnectionStatus) {
			return true;
		}
		boolean ret = myPort.disconnect();
		return setStatusAndReturn(ret, ConnectionStatus.DISCONNECTED);
	}

	private boolean setStatusAndReturn(boolean ret, ConnectionStatus status) {
		if (status == null) {
			return false;
		}
		if (ret && status == myPort.getConnectionStatus()) {
			ConnectionStatus oldStatus = myConnectionStatus;
			myConnectionStatus = status;
			firePropertyChange(PROP_CONNECTION_STATUS, oldStatus, status);
		} else {
			return false;
		}
		return ret;
	}

	@Override
	public List<String> getErrorMessages() {
		if (myPort == null) {
			return null;
		}
		return myPort.getErrors();
	}
}
