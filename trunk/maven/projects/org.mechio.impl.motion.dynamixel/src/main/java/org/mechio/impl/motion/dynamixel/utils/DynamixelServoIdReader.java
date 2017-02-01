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

package org.mechio.impl.motion.dynamixel.utils;

import org.mechio.api.motion.servos.ServoController;
import org.mechio.api.motion.servos.ServoController.ServoId;
import org.mechio.api.motion.servos.utils.ServoIdReader;
import org.mechio.impl.motion.dynamixel.DynamixelServo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DynamixelServoIdReader implements
		ServoIdReader<DynamixelServo.Id> {
	private static final Logger theLogger = LoggerFactory.getLogger(DynamixelServoIdReader.class);

	@Override
	public ServoId<DynamixelServo.Id> read(
			ServoController.Id controllerId, String servoIdStr) {
		if (controllerId == null || servoIdStr == null) {
			throw new NullPointerException();
		}
		DynamixelServo.Id dId = read(servoIdStr);
		return new ServoId<>(controllerId, dId);
	}

	@Override
	public DynamixelServo.Id read(String servoIdStr) {
		if (servoIdStr == null) {
			throw new NullPointerException();
		}
		try {
			int id = Integer.parseInt(servoIdStr);
			return new DynamixelServo.Id(id);
		} catch (NumberFormatException ex) {
			theLogger.error("Could not read DynamixelServo.Id", ex);
			throw ex;
		}
	}

	@Override
	public Class<DynamixelServo.Id> getServoIdClass() {
		return DynamixelServo.Id.class;
	}

}
