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

package org.mechio.impl.motion.pololu;

import org.jflux.api.common.rk.config.VersionProperty;
import org.mechio.api.motion.servos.Servo;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.api.motion.servos.utils.ConnectionStatus;
import org.mechio.impl.motion.rxtx.serial.SerialServoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ServoController for a Pololu Mini-Maestro servo control board.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class MaestroController extends SerialServoController<
		MaestroServo.Id,
		ServoConfig<MaestroServo.Id>,
		MaestroServo,
		MaestroControllerConfig> {
	/**
	 * Controller type version name.
	 */
	public final static String VERSION_NAME = "Pololu Mini Maestro";
	/**
	 * Controller type version number.
	 */
	public final static String VERSION_NUMBER = "1.0";
	/**
	 * Controller type version.
	 */
	public final static VersionProperty VERSION = new VersionProperty(VERSION_NAME, VERSION_NUMBER);
	private static final Logger theLogger = LoggerFactory.getLogger(MaestroController.class);

	private Map<MaestroServo.Id, MaestroServo> myPhysicalMap;
	private Boolean myEnabledFlag;
	private MaestroSuspendLoop mySuspendLoop;

	/**
	 * Creates a MaestroController from the given SerialServoControllerConfig.
	 *
	 * @param config SerialServoControllerConfig for creating the controller
	 */
	public MaestroController(MaestroControllerConfig config) {
		super(config);
		myPhysicalMap = new HashMap();
		myEnabledFlag = true;
		mySuspendLoop = new MaestroSuspendLoop(MaestroSuspendLoop.DEFAULT_LOOP_INTERVAL);
	}

	@Override
	public boolean connect() {
		boolean connect = super.connect();
		if (connect) {
			setServos();
//            ServoId<MaestroServo.Id>[] sIds = 
//                    myServoMap.keySet().toArray(new ServoId[0]);
//            moveServos(sIds, sIds.length, 0, 3000);
			mySuspendLoop.startLoop();
		}
		return connect;
	}

	@Override
	public boolean disconnect() {
		mySuspendLoop.stopLoop();
		return super.disconnect();
	}

	@Override
	protected boolean setServos() {
		myServos.clear();
		myServoMap.clear();
		if (myConfig == null) {
			return true;
		}
		Map<MaestroServo.Id, ServoConfig<MaestroServo.Id>> params =
				myConfig.getServoConfigs();
		if (params == null) {
			return true;
		}
		for (ServoConfig<MaestroServo.Id> param : params.values()) {
			MaestroServo servo = new MaestroServo(param, this);
			MaestroServo.Id id = servo.getId();
			ServoId<MaestroServo.Id> servoId = new ServoId(getId(), id);
			MaestroServo.Id pId = servo.getPhysicalId();
			if (myServoMap.containsKey(servoId) || myPhysicalMap.containsKey(pId)) {
				theLogger.warn("Unable to add Servo with duplicate Id - {}, {}.",
						id, pId);
				continue;
			}
			myServos.add(servo);
			myServoMap.put(servoId, servo);
			myPhysicalMap.put(servo.getPhysicalId(), servo);
			mySuspendLoop.addServo(servo, MaestroSuspendLoop.DEFAULT_TIMEOUT);
		}
		moveAllServos(3000);
		return true;
	}

	@Override
	public synchronized boolean moveServo(ServoId<MaestroServo.Id> id, long lenMillisec) {
		if (ConnectionStatus.CONNECTED != myConnectionStatus) {
			return false;
		}
		Boolean e = getEnabled();
		if (e == null || !e || !containsId(id)) {
			return true;
		}
		MaestroServo servo = (MaestroServo) getServo(id);
		if (!servo.getEnabled()) {
			return true;
		}
		byte physId = servo.getPhysicalId().getServoNumber();
		Integer goal = servo.getAbsoluteGoalPosition();
		if (goal == null) {
			return true;
		}
		/* We multiply the desired position by 4 because the Pololu Servo Controller has a resolution of 0.25us
		 * So to get a pulse width of 1000us, we want to go to position 2000.
         * The goal is to have all absolute PWM positions in 'us'(microseconds),
         * and then translate in the controller code.  */
		goal *= 4;
		if (!myPort.write((byte) 0xAA, //Signals Pololu protocol, also used to auto-detect baud rate.
				(byte) 0x0C, //Device number, default 12
				(byte) 0x04, //Move Command
				physId,
				(byte) (goal & 0x7F), // Second byte holds the lower 7 bits of target.
				(byte) ((goal >> 7) & 0x7F)
		) || !myPort.flushWriter()) {
			theLogger.error("Cannot move servo {}, unable to write to serial port {}",
					id, myConfig.getPortName());
			return false;
		}
		return true;
	}

	/**
	 * Moves the joints with the given ids.
	 * The Pololu protocol allows for consecutive servos to be moved with a
	 * single command.  To take advantage of this, we must find the consecutive
	 * blocks of physical ids from the given list of logical ids.
	 * First we mad logical ids to physical ids, and sort the array.
	 * Then we iterate through the sorted ids and find the consecutive runs.
	 *
	 * @param ids list of Logical Ids of Servos to move.
	 * @return true if the joints were successfully moved.
	 */
	@Override
	public synchronized boolean moveServos(
			ServoId<MaestroServo.Id>[] ids,
			int len, int offset, long lenMillisec) {
		if (ConnectionStatus.CONNECTED != myConnectionStatus) {
			return false;
		}
		Boolean e = getEnabled();
		if (e == null || !e) {
			return true;
		}
		if (len == 0) {
			return true;
		} else if (len == 1) {
			if (!containsId(ids[offset])) {
				return true;
			}
			return moveServo(ids[offset], lenMillisec);
		}
		MaestroServo.Id[] pIds =
				new MaestroServo.Id[len];
		byte pCnt = 0;
		for (int i = offset; i < offset + len; i++) {
			MaestroServo joint = myServoMap.get(ids[i]);
			if (joint == null || !joint.getEnabled() ||
					joint.getAbsoluteGoalPosition() == null) {
				continue;
			}
			pIds[pCnt] = joint.getPhysicalId();
			pCnt++;
		}
		Arrays.sort(pIds, 0, pCnt);
		boolean success = true;
		if (pCnt <= 0) {
			return success;
		}
		MaestroServo.Id prev = pIds[0];
		byte cnt = 1;
		for (byte i = 1; i < pCnt; cnt++, i++) {
			if (pIds[i - 1].isConsecutive(pIds[i])) {
				continue;
			}
			success = success && moveConsecutiveServos(prev, cnt);
			prev = pIds[i];
			cnt = 0;
		}
		return success && moveConsecutiveServos(prev, cnt);
	}

	/**
	 * Moves multiple Servos with consecutive physical ids in a single command.
	 * WARNING: This does not check if the the Servo is enabled.
	 *
	 * @param startId physical id of the first Servo in the sequence
	 * @param count   number of joint in the sequence to move
	 * @return true if successful
	 */
	protected synchronized boolean moveConsecutiveServos(
			MaestroServo.Id startId, byte count) {
		if (ConnectionStatus.CONNECTED != myConnectionStatus) {
			return false;
		}
		if (!getEnabled()) {
			return true;
		}
		int n = count * 2 + 5;
		byte[] bytes = new byte[n];
		bytes[0] = (byte) 0xAA;
		bytes[1] = (byte) 0x0C;
		bytes[2] = (byte) 0x1F;
		bytes[3] = count;
		bytes[4] = startId.getServoNumber();
		int k = 5;
		for (byte i = 0; i < count; i++) {
			MaestroServo.Id next = startId.getOffsetId(i);
			if (next == null) {
				continue;
			}
			MaestroServo servo = myPhysicalMap.get(next);
			int goal = servo.getAbsoluteGoalPosition() * 4;
			// Second byte holds the lower 7 bits of target.
			bytes[k++] = (byte) (goal & 0x7F);
			bytes[k++] = (byte) ((goal >> 7) & 0x7F);
		}
		return myPort.write(bytes, 0, k);
	}

	@Override
	public boolean moveAllServos(long lenMillisec) {
		if (ConnectionStatus.CONNECTED != myConnectionStatus) {
			return false;
		}
		ServoId[] ids = myServoMap.keySet().toArray(new ServoId[0]);
		return moveServos(ids, ids.length, 0, lenMillisec);
	}

	@Override
	protected MaestroServo connectServo(ServoConfig<MaestroServo.Id> config) {
		MaestroServo joint = new MaestroServo(config, this);
		return joint;
	}

	@Override
	protected boolean disconnectServo(ServoId<MaestroServo.Id> id) {
		return true;
	}

	/**
	 * Changes the physical id of the Servo and notifies listeners.
	 *
	 * @param oldId previous physical id
	 * @param newId new physical id
	 */
	protected void changeServoPhysicalId(
			MaestroServo.Id oldId,
			MaestroServo.Id newId) {
		MaestroServo joint = myPhysicalMap.remove(oldId);
		myPhysicalMap.put(newId, joint);
		firePropertyChange(PROP_SERVOS, null, myServoMap);
	}

	@Override
	public void setEnabled(Boolean enabled) {
		Boolean old = myEnabledFlag;
		myEnabledFlag = enabled;
		for (Servo servo : myServos) {
			servo.setEnabled(enabled);
		}
		firePropertyChange(PROP_ENABLED, old, enabled);
	}

	@Override
	public Boolean getEnabled() {
		return myEnabledFlag;
	}

	synchronized void disableServoPWM(MaestroServo.Id mId) {
		ServoId<MaestroServo.Id> id = sId(mId);
		if (ConnectionStatus.CONNECTED != myConnectionStatus) {
			return;
		}
		if (!containsId(id)) {
			return;
		}
		MaestroServo servo = (MaestroServo) getServo(id);
		byte physId = servo.getPhysicalId().getServoNumber();
		if (!myPort.write((byte) 0xAA, //Signals Pololu protocol, also used to auto-detect baud rate.
				(byte) 0x0C, //Device number, default 12
				(byte) 0x04, //Move Command
				physId,
				(byte) 0, //target of 0 to disable the servo
				(byte) 0
		) || !myPort.flushWriter()) {
			theLogger.error("Cannot move servo {}, unable to write to serial port {}",
					id, myConfig.getPortName());
		}
	}

	synchronized void enableServoPWM(MaestroServo.Id mId) {
		moveServo(sId(mId), 40);
	}

	private ServoId<MaestroServo.Id> sId(MaestroServo.Id mId) {
		return new ServoId<>(getId(), mId);
	}

	@Override
	public Class<MaestroServo.Id> getServoIdClass() {
		return MaestroServo.Id.class;
	}
}
