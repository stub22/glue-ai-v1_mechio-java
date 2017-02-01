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
package org.mechio.impl.motion.openservo.feedback;

import org.jflux.api.core.Listener;
import org.jflux.api.core.Source;
import org.mechio.api.motion.servos.ServoController.ServoId;
import org.mechio.impl.motion.dynamixel.DynamixelPacket;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlLoop.DynamixelCommand;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlSettings;
import org.mechio.impl.motion.dynamixel.feedback.MoveParams;
import org.mechio.impl.motion.openservo.OpenServo;
import org.mechio.impl.motion.openservo.OpenServoCommandSet;
import org.mechio.impl.motion.openservo.OpenServoController;
import org.mechio.impl.motion.rxtx.serial.RXTXSerialPort;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class OpenServoControlLoop {
	private static final org.slf4j.Logger theLogger = LoggerFactory.getLogger(OpenServoControlLoop.class);
	private ConcurrentOpenServoCache myCache;
	private OpenServoController myController;
	private List<OpenServo.Id> myServoIds;
	private Queue<DynamixelCommand> myCommandQueue;
	private DynamixelControlSettings mySettings;
	private int myReadIndex;
	private boolean myRunFlag;
	private Source<RXTXSerialPort> myPortSource;

	public OpenServoControlLoop(
			OpenServoController controller,
			DynamixelControlSettings settings) {
		if (controller == null || settings == null) {
			throw new NullPointerException();
		}
		mySettings = settings;
		myController = controller;
		myCache = controller.getCache();
		myReadIndex = 0;
		myRunFlag = false;
		myCommandQueue = new ConcurrentLinkedQueue<>();
	}

	public void setPortSource(Source<RXTXSerialPort> port) {
		myPortSource = port;
	}

	public DynamixelControlSettings getSettings() {
		return mySettings;
	}

	public void start(List<OpenServo.Id> ids) {
		if (myRunFlag || ids == null) {
			return;
		}
		mySettings.setRunFlag(true);
		myRunFlag = true;
		myServoIds = ids;
	}

	public void stop() {
		myRunFlag = false;
		mySettings.setRunFlag(false);
	}

	public boolean getMoveFlag() {
		return myCache.getMoveFlag();
	}

	public synchronized void move() {
		if (myPortSource == null || myPortSource.getValue() == null) {
			return;
		}
		Collection<MoveParams<OpenServo.Id>> params =
				myCache.acquireMoveParams();
		try {
			if (params == null || params.isEmpty()) {
				return;
			}
			if (!moveServos(myController, params, mySettings)) {
				theLogger.warn("There was an error moving the OpenServos.");
			} else {
				myCache.setMoveFlag(false);
			}
		} finally {
			myCache.releaseMoveParams();
		}
	}

	private static int THE_OPEN_SERVO_READ_DELAY_COUNT = 0;
	private static int THE_OPEN_SERVO_READ_DELAY_THRESHOLD = 10;

	public synchronized boolean update() {
		if (myPortSource == null || myPortSource.getValue() == null) {
			return false;
		}

		if (THE_OPEN_SERVO_READ_DELAY_COUNT < THE_OPEN_SERVO_READ_DELAY_THRESHOLD) {
			THE_OPEN_SERVO_READ_DELAY_COUNT++;
			return false;
		}
		THE_OPEN_SERVO_READ_DELAY_COUNT = 0;
		int from = myReadIndex;
		int to = Math.min(
				myReadIndex + mySettings.getReadCount(),
				myServoIds.size());
		List<OpenServo.Id> ids = myServoIds.subList(from, to);
		if (ids.isEmpty()) {
			return false;
		}

		OpenServoFeedbackValues feedback = OpenServoReader.getFeedback(myPortSource.getValue(), ids.get(0));
		if (feedback == null) {
			return false;
		}
		List<OpenServoFeedbackValues> vals = Arrays.asList(feedback);
		myCache.addFeedbackValues(vals);
		myReadIndex += mySettings.getReadCount();
		boolean ret = myReadIndex < myServoIds.size();
		if (!ret) {
			myReadIndex = 0;
		}
		updateServoValues(vals);
		//return ret;
		/* Returning false signals the Dynamixel loop to get feedback from all
		 * the dynamixels before calling this method again.
         */
		return false;
	}

	private void updateServoValues(List<OpenServoFeedbackValues> feedbackVals) {
		for (OpenServoFeedbackValues val : feedbackVals) {
			if (val == null || val.getCurrentVoltage() == 0) {
				continue;
			}
			ServoId<OpenServo.Id> id =
					new ServoId<>(
							myController.getId(), val.getServoId());
			OpenServo servo = myController.getServo(id);
			if (servo != null) {
				servo.setFeedbackVals(val);
			}
		}
	}

	public void queueCommand(DynamixelCommand cmd) {
		if (cmd == null) {
			return;
		}
		myCommandQueue.add(cmd);
		if (!myRunFlag) {
			command();
		}
	}

	public synchronized boolean command() {
		if (myCommandQueue.isEmpty()) {
			return false;
		} else if (myPortSource == null || myPortSource.getValue() == null) {
			return false;
		}
		DynamixelCommand cmd = myCommandQueue.poll();
		if (cmd == null) {
			return false;
		}
		if (myPortSource == null || myPortSource.getValue() == null) {
			if (cmd.myPacketCallback != null) {
				cmd.myPacketCallback.handleEvent(null);
			}
			return false;
		}
		boolean write = myPortSource.getValue().write(cmd.myCommandBytes)
				&& myPortSource.getValue().flushWriter();
		if (!write) {
			if (cmd.myPacketCallback != null) {
				cmd.myPacketCallback.handleEvent(null);
			}
			return false;
		}
		if (cmd.myPacketCount > 0) {
			if (!read(cmd.myPacketCount,
					cmd.myPacketDataSize,
					cmd.myPacketCallback)) {
				return false;
			}
		}
		return true;
	}

	private boolean read(int i, byte packetSize,
						 final Listener<DynamixelPacket[]> callback) {
		final DynamixelPacket[] packets = readPackets(myController, i, packetSize);
		if (callback != null) {
			callback.handleEvent(packets);
		}
		if (packets == null || packets.length != i) {
			return false;
		}
		return true;
	}

	private boolean moveServos(
			OpenServoController controller,
			Collection<MoveParams<OpenServo.Id>> params,
			DynamixelControlSettings settings) {
		boolean ret = true;
		for (MoveParams<OpenServo.Id> p : params) {
			ret = moveServo(p) && ret;
		}
		return ret;
	}

	private boolean moveServo(MoveParams<OpenServo.Id> params) {
		if (myPortSource == null) {
			return false;
		}
		OpenServo.Id id = params.getServoId();
		byte rs485Addr = (byte) id.getRS485Addr();
		byte i2cAddr = (byte) id.getI2CAddr();
		byte[] cmd = OpenServoCommandSet.move(
				rs485Addr, i2cAddr, params.getGoalPosition());
		myPortSource.getValue().write(cmd);
		myPortSource.getValue().flushWriter();
		return true;
	}

	private List<OpenServoFeedbackValues> getFeedback(
			OpenServoController controller, List<OpenServo.Id> ids) {
		return null;
	}

	private DynamixelPacket[] readPackets(
			OpenServoController controller, int count, byte packetSize) {
		return null;
	}
}
