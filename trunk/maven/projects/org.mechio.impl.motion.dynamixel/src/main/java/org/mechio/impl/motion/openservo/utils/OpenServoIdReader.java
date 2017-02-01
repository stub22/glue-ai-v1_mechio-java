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

package org.mechio.impl.motion.openservo.utils;

import org.mechio.api.motion.servos.ServoController;
import org.mechio.api.motion.servos.ServoController.ServoId;
import org.mechio.api.motion.servos.utils.ServoIdReader;
import org.mechio.impl.motion.openservo.OpenServo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class OpenServoIdReader implements
		ServoIdReader<OpenServo.Id> {
	private static final Logger theLogger = LoggerFactory.getLogger(OpenServoIdReader.class);

	@Override
	public ServoId<OpenServo.Id> read(
			ServoController.Id controllerId, String servoIdStr) {
		if (controllerId == null || servoIdStr == null) {
			throw new NullPointerException();
		}
		OpenServo.Id dId = read(servoIdStr);
		return new ServoId<>(controllerId, dId);
	}

	@Override
	public OpenServo.Id read(String servoIdStr) {
		if (servoIdStr == null) {
			throw new NullPointerException();
		}
		int splitIndex = servoIdStr.indexOf("::");
		if (splitIndex < 0 || splitIndex >= (servoIdStr.length() - 2)) {
			throw new IllegalArgumentException();
		}
		String rs485Str = servoIdStr.substring(0, splitIndex).trim();
		String i2cStr = servoIdStr.substring(splitIndex + 2).trim();
		try {
			int rs485Id = Integer.parseInt(rs485Str);
			int i2cId = Integer.parseInt(i2cStr);
			return new OpenServo.Id(rs485Id, i2cId);
		} catch (NumberFormatException ex) {
			theLogger.error("Could not read OpenServo.Id", ex);
			throw ex;
		}
	}

	@Override
	public Class<OpenServo.Id> getServoIdClass() {
		return OpenServo.Id.class;
	}

}
