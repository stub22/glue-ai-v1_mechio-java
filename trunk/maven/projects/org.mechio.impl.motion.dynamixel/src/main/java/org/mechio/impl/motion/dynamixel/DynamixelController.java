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

package org.mechio.impl.motion.dynamixel;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.impl.motion.dynamixel.enums.Register;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlLoop;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlSettings;
import org.mechio.impl.motion.dynamixel.feedback.GoalUpdateValues;
import org.mechio.impl.motion.dynamixel.utils.DynamixelControllerConfig;
import org.mechio.impl.motion.rxtx.serial.RXTXSerialPort;
import org.mechio.impl.motion.rxtx.serial.SerialServoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServoController for a chain of DynamixelServos.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DynamixelController extends SerialServoController<
		DynamixelServo.Id,
		ServoConfig<DynamixelServo.Id>,
		DynamixelServo,
		DynamixelControllerConfig> {
	private static final Logger theLogger = LoggerFactory.getLogger(DynamixelController.class);
	/**
	 * Controller type version name.
	 */
	public final static String VERSION_NAME = "Dynamixel RX";
	/**
	 * Controller type version number.
	 */
	public final static String VERSION_NUMBER = "1.0";
	/**
	 * Controller type VersionProperty.
	 */
	public final static VersionProperty VERSION = new VersionProperty(VERSION_NAME, VERSION_NUMBER);
	/**
	 * DynamixelController broadcast id.  This is an invalid DynamixelServo
	 * physical id, reserved to indicate the controller to the joints.
	 */
	public final static DynamixelServo.Id BROADCAST_ID =
			new DynamixelServo.Id();

	private Map<DynamixelServo.Id, ServoConfig> myUnresponsiveServos;
	private Boolean myEnabledFlag;
	private DynamixelControlLoop myControlLoop;

	/**
	 * Creates a new DynamixelController from the given SerialServoControllerConfig.
	 *
	 * @param config SerialServoControllerConfig for the new controller
	 */
	public DynamixelController(DynamixelControllerConfig config) {
		super(config);
		DynamixelControlSettings settings =
				new DynamixelControlSettings(1, 0, 77, 69, 0, 30);
		setTimeoutLength(settings.getTimeoutLengthMillisec());
		myControlLoop = new DynamixelControlLoop(this, settings);
		myEnabledFlag = false;
	}

	/**
	 * Attempts to ping each Servo in the ServoControllerConfig.  A joint is
	 * added if it responds, otherwise it is added to the UnresponsiveServos
	 * List.
	 *
	 * @return true if successful
	 */
	@Override
	protected synchronized boolean setServos() {
		myServos.clear();
		myServoMap.clear();
		myUnresponsiveServos = new HashMap();
		for (ServoConfig<DynamixelServo.Id> param : myConfig.getServoConfigs().values()) {
			DynamixelServo.Id id = param.getServoId();
			if (!ping(id)) {
				myUnresponsiveServos.put(param.getServoId(), param);
				theLogger.warn("Unable to ping Dynamixel {}.",
						param.getServoId());
				continue;
			}
			DynamixelServo servo = new DynamixelServo(param, this);
			myServos.add(servo);
			DynamixelServo.Id sId = servo.getId();
			ServoId<DynamixelServo.Id> servoId =
					new ServoId<>(getId(), sId);
			myServoMap.put(servoId, servo);
			initServo(servo);
		}
		return true;
	}

	private void initServo(DynamixelServo servo) {
		servo.setEnabled(myEnabledFlag);
		NormalizedDouble def = servo.getDefaultPosition();
		servo.setGoalPosition(def);
	}

	@Override
	public boolean moveServo(ServoId<DynamixelServo.Id> id, long lenMillisec) {
		return moveServos(new ServoId[]{id}, 1, 0, lenMillisec);
	}

	@Override
	public boolean moveServos(
			ServoId<DynamixelServo.Id>[] ids,
			int len, int offset, long lenMillisec) {
		List<GoalUpdateValues<DynamixelServo.Id>> goals =
				new ArrayList<>(len);
		long goalTime = TimeUtils.now() + lenMillisec;
		for (int i = offset; i < offset + len; i++) {
			ServoId<DynamixelServo.Id> id = ids[i];
			DynamixelServo servo = myServoMap.get(id);
			if (servo == null) {
				continue;
			}
			Integer goalVal = servo.getAbsoluteGoalPosition();
			if (goalVal == null) {
				continue;
			}
			GoalUpdateValues<DynamixelServo.Id> goal =
					new GoalUpdateValues(id.getServoId(), goalVal, goalTime);
			goals.add(goal);
		}
		myControlLoop.setGoalPositions(goals);
		return true;
	}

	@Override
	public boolean moveAllServos(long lenMillisec) {
		ServoId[] ids = myServoMap.keySet().toArray(new ServoId[0]);
		return moveServos(ids, ids.length, 0, lenMillisec);
	}

	/**
	 * Pings the DynamixelServo at the given physical id.
	 *
	 * @param id physical id of the DynamixelServo to ping
	 * @return true if the DynamixelServo responds
	 */
	public synchronized boolean ping(DynamixelServo.Id id) {
		return DynamixelCommandSender.ping(myControlLoop, id, myTimeoutLength);
	}

	/**
	 * Reads the Register value from the DynamixelServo with the given physical
	 * id.
	 *
	 * @param id  physical id of the DynamixelServo to read
	 * @param reg Register to read
	 * @return unsigned value at the given register
	 */
	public int readRegister(DynamixelServo.Id id, Register reg) {
		int[] vals = readRegisters(id, reg, reg);
		if (vals == null || vals.length == 0) {
			return 0;
		}
		return vals[0];
	}

	/**
	 * Reads the values from the Register range specified by regFirst and
	 * regLast for the DynamixelServo with the given physical id.
	 *
	 * @param id       physical id of the DynamixelServo to read
	 * @param regFirst first Register in the range
	 * @param regLast  last Register in the range
	 * @return array of unsigned values from the Registers specified
	 */
	public synchronized int[] readRegisters(
			DynamixelServo.Id id, Register regFirst, Register regLast) {
		return DynamixelCommandSender.readRegisters(
				myControlLoop, id, regFirst, regLast, myTimeoutLength);
	}

	/**
	 * Write a value to the given register for the given DynamixelServo.
	 *
	 * @param id       physical id of the DynamixelServo
	 * @param reg      Register to write
	 * @param value    value to write
	 * @param deferred if true, data is written but the DynamixelServo state will not change until
	 *                 it receives an ACTION instruction.
	 * @return true if successful
	 */
	protected boolean writeRegister(
			DynamixelServo.Id id, Register reg,
			Integer value, Boolean deferred) {
		return DynamixelCommandSender.writeRegister(
				myControlLoop, id, reg, value, myTimeoutLength);
	}

	@Override
	protected DynamixelServo connectServo(ServoConfig config) {
		return new DynamixelServo(config, this);
	}

	@Override
	protected boolean disconnectServo(ServoId<DynamixelServo.Id> id) {
		return true;
	}

	@Override
	public void setEnabled(Boolean enabled) {
		Boolean old = myEnabledFlag;
		myEnabledFlag = enabled;
		for (DynamixelServo servo : myServos) {
			servo.setEnabled(enabled);
		}
		firePropertyChange(PROP_ENABLED, old, enabled);
	}

	@Override
	public Boolean getEnabled() {
		return myEnabledFlag;
	}

	@Override
	public boolean connect() {
		boolean connect = super.connect();
		if (connect) {
			setServos();
			List<DynamixelServo.Id> ids =
					new ArrayList<>();
			for (DynamixelServo servo :
					myServos) {
				ids.add(servo.getId());
			}
			myControlLoop.start(ids);
			ServoId<DynamixelServo.Id>[] sIds =
					myServoMap.keySet().toArray(new ServoId[0]);
			//moveServos(sIds, sIds.length, 0, 3000);
		}
		return connect;
	}

	@Override
	public boolean disconnect() {
		if (myControlLoop != null) {
			myControlLoop.stop();
		}
		return super.disconnect();
	}

	@Override
	public Class<DynamixelServo.Id> getServoIdClass() {
		return DynamixelServo.Id.class;
	}

	public RXTXSerialPort getPort() {
		return myPort;
	}

	public DynamixelControlLoop getControlLoop() {
		return myControlLoop;
	}
}