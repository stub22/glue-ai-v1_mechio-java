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

import org.jflux.api.common.rk.utils.Utils;
import org.mechio.impl.motion.dynamixel.enums.ErrorStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DynamixelPacket {
	private static final Logger theLogger = LoggerFactory.getLogger(DynamixelPacket.class);
	private DynamixelServo.Id myId;
	private byte[] myData;
	private int myLength;
	private boolean myErrorFlag;
	private List<ErrorStatus> myErrors;

	public static DynamixelPacket parsePacket(byte[] data, int offset) {
		if (data.length - offset < 6) {
			theLogger.info("Error reading Dynamixel Packet, too few bytes.  "
							+ "Data Length: {}, offset: {}",
					data.length, offset);
			return null;
		}
		int first = Utils.unsign(data[offset]);
		int second = Utils.unsign(data[offset + 1]);
		if (first != 0xff || second != 0xff) {
			theLogger.info("Error reading Dynamixel Packet.  Incorrect header bytes."
							+ "Data Length: {}, offset: {}, first: {}, second: {}",
					data.length, offset, first, second);
			return null;
		}
		DynamixelPacket packet = new DynamixelPacket();
		int receivedId = Utils.unsign(data[offset + 2]);
		packet.myLength = Utils.unsign(data[offset + 3]) - 2;
		if (packet.myLength > data.length - (offset + 6)) {
			theLogger.info("Error reading Dynamixel Packet, too few bytes.  "
							+ "Data Length: {}, Offset: {}, Packet Length: {}",
					data.length, offset, packet.myLength);
			return null;
		}
		int errorBits = Utils.unsign(data[offset + 4]);
		packet.myErrors = ErrorStatus.getStatusList(errorBits);
		int plen = packet.myLength;
		if (plen < 0) {
			theLogger.info("Error reading Dynamixel Packet.  Negative packet length.");
			return null;
		}
		packet.myData = new byte[packet.myLength];
		System.arraycopy(data, offset + 5, packet.myData, 0, packet.myLength);
		packet.myErrorFlag = (packet.myLength != packet.myData.length);
		byte chkCalc = Utils.checksum(packet.myData, 0, packet.myData.length, true,
				(byte) receivedId, (byte) (packet.myLength + 2), (byte) errorBits);
		byte chkRec = data[offset + 5 + packet.myLength];
		boolean chksum = chkCalc != chkRec;
		boolean dataLen = packet.myLength != packet.myData.length;
		boolean errorFlag = !packet.myErrors.isEmpty();
		boolean badId = !DynamixelServo.Id.isValidId(receivedId);
		if (!badId) {
			packet.myId = new DynamixelServo.Id(receivedId);
		}
		packet.myErrorFlag = chksum || dataLen || errorFlag || badId;
		if (!packet.myErrorFlag) {
			return packet;
		}
		theLogger.info("Bad Packet Received:\n{}", str(packet));
		if (chksum) {
			theLogger.info("Error reading Dynamixel packet. Bad checksum.");
			return null;
		}
		if (dataLen) {
			theLogger.warn("Error reading Dynamixel packet.  Data not received.");
			return null;
		}
		if (errorFlag) {
			int len = packet.myErrors.size();
			String errorStr = "";
			for (int j = 0; j < len; j++) {
				ErrorStatus e = packet.myErrors.get(j);
				errorStr += e.name();
				errorStr += j < len - 1 ? ", " : ".";
			}
			theLogger.warn("Dynamixel Error - id: {}, error: {}",
					packet.myId.toString(), errorStr);
		}
		if (badId) {
			theLogger.warn("Error reading Dynamixel packet.  Invalid Id Received.");
			return null;
		}
		return packet;
	}

	private DynamixelPacket() {
		myId = DynamixelController.BROADCAST_ID;
	}

	public DynamixelServo.Id getId() {
		return myId;
	}

	public byte[] getData() {
		return myData;
	}

	public boolean hasError() {
		return myErrorFlag;
	}

	public List<ErrorStatus> getErrors() {
		return myErrors;
	}

	private static String str(DynamixelPacket packet) {
		StringBuilder builder = new StringBuilder();
		builder.append("id: ").append(packet.getId())
				.append(", length: ").append(packet.myLength);
		builder.append(", has error: ").append(packet.hasError());
		byte[] data = packet.getData();
		if (data != null) {
			builder.append("\ndata: ").append(Arrays.toString(data));
		} else {
			builder.append(", data: null");

		}
		List<ErrorStatus> errors = packet.getErrors();
		if (errors != null && !errors.isEmpty()) {
			builder.append("\nerrors: ").append(Arrays.toString(packet.myErrors.toArray()));
		}
		return builder.toString();
	}
}
