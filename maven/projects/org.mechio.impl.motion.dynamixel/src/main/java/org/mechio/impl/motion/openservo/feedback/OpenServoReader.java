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

import org.jflux.api.common.rk.utils.TimeUtils;
import org.mechio.api.motion.servos.utils.ConnectionStatus;
import org.mechio.impl.motion.openservo.OpenServo;
import org.mechio.impl.motion.openservo.OpenServoCommandSet;
import org.mechio.impl.motion.openservo.OpenServoCommandSet.Register;
import org.mechio.impl.motion.openservo.OpenServoPacket;
import org.mechio.impl.motion.rxtx.serial.RXTXSerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class OpenServoReader {
	private static final Logger theLogger = LoggerFactory.getLogger(OpenServoReader.class);

	public synchronized static OpenServoFeedbackValues getFeedback(
			RXTXSerialPort port, OpenServo.Id id) {
		if (port == null || ConnectionStatus.CONNECTED != port.getConnectionStatus()) {
			return null;
		}
		int[] vals =
				readServo(port, id,
						Register.POSITION_HI, Register.VOLTAGE_LO);
		if (vals == null) {
			return null;
		}
		long now = TimeUtils.now();
		if (validateValues(vals)) {
			return new OpenServoFeedbackValues(id, vals, now);
		}
		return null;
	}

	private static boolean validateValues(int[] vals) {
//        if(vals == null){
//            return false;
//        }
//        for(int i=0; i<vals.length; i++){
//            if(vals[i] != 0){
//                return true;
//            }
//        }
//        return false;
		return true;
	}

	private static int[] readServo(RXTXSerialPort port,
								   OpenServo.Id id, Register regFirst, Register regLast) {
		byte byteCount = (byte) (regLast.getRegister() - regFirst.getRegister() + 1);
		byte[] cmd = buildReadCommand(id, regFirst, byteCount);
		if (!port.write(cmd)
				|| !port.flushWriter()) {
			return null;
		}
		OpenServoPacket packet = readPacket(port, byteCount);
		if (packet == null) {
			return null;
		}
		return parsePacket(packet, byteCount);
	}

	private static byte[] buildReadCommand(OpenServo.Id id, Register startRegister, int regCount) {
		return OpenServoCommandSet.readRegisters(
				id.getRS485Addr(), id.getI2CAddr(), startRegister, regCount);
	}

	public static OpenServoPacket readPacket(
			RXTXSerialPort port, byte byteCount) {
		int packetLen = byteCount + 5;
		byte[] data = port.read(packetLen);
		return OpenServoPacket.parsePacket(data, 0);
	}

	public static int[] parsePacket(OpenServoPacket packet, byte byteCount) {
		if (packet == null || packet.getData().length < byteCount) {
			return null;
		}
		int[] vals = new int[byteCount / 2];
		byte[] data = packet.getData();
		int j = 0;
		for (int i = 0; j < vals.length && i < byteCount - 1; i += 2, j++) {
			int hi = (data[i] & 0xFF) << 8;
			int lo = data[i + 1] & 0xFF;
			vals[j] = hi + lo;
		}
		return vals;
	}
}
