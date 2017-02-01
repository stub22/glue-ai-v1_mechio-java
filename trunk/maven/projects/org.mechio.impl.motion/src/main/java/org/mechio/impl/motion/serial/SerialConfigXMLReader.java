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

package org.mechio.impl.motion.serial;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.mechio.api.motion.servos.ServoController;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.api.motion.servos.utils.ServoIdReader;
import org.mechio.impl.motion.config.ServoControllerConfigXMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads a SerialServoControllerConfig from a HierarchicalConfiguration using the
 * given VersionProperty.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class SerialConfigXMLReader<
		ServoIdType,
		ServoConf extends ServoConfig<ServoIdType>,
		SerialConf extends SerialServoControllerConfig<ServoIdType, ServoConf>> extends
		ServoControllerConfigXMLReader<SerialConf> {
	private static final Logger theLogger = LoggerFactory.getLogger(SerialConfigXMLReader.class);

	/**
	 * Connection parameters XML Element.
	 */
	public final static String XML_SERVO_CONTROLLER_ID = "ServoControllerId";

	/**
	 * Connection parameters XML Element.
	 */
	public final static String XML_CONNECTION_PARAMS = "ConnectionParameters";
	/**
	 * Port name XML Element.
	 */
	public final static String XML_PORT_NAME = "PortName";
	/**
	 * Baud rate XML Element.
	 */
	public final static String XML_BAUD_RATE = "BaudRate";
	/**
	 * Servo parameters XML Element.
	 */
	public final static String XML_JOINT_PARAMS = "ServoParameters";
	/**
	 * Servo XML Element.
	 */
	public final static String XML_SERVO = "Servo";
	/**
	 * Servo physical id XML Element.
	 */
	public final static String XML_SERVO_ID = "ServoId";
	/**
	 * Servo name XML Element.
	 */
	public final static String XML_JOINT_NAME = "Name";
	/**
	 * Servo min position XML Element.
	 */
	public final static String XML_MIN_POSITION = "MinPosition";
	/**
	 * Servo max position XML Element.
	 */
	public final static String XML_MAX_POSITON = "MaxPosition";
	/**
	 * Servo default position XML Element.
	 */
	public final static String XML_DEFAULT_POSITON = "DefaultPosition";

	private ServoIdReader<ServoIdType> myServoIdReader;

	public SerialConfigXMLReader(
			ServoIdReader<ServoIdType> reader) {
		if (reader == null) {
			throw new NullPointerException();
		}
		myServoIdReader = reader;
	}

	@Override
	public SerialConf loadConfiguration(HierarchicalConfiguration param) {
		String controllerId = param.getString(XML_SERVO_CONTROLLER_ID);
		if (controllerId == null) {
			throw new NullPointerException("Could not load ServoController.Id");
		}
		HierarchicalConfiguration connectionConf = param.configurationAt(XML_CONNECTION_PARAMS);
		HierarchicalConfiguration servoConf = param.configurationAt(XML_JOINT_PARAMS);
		SerialConf config = newConfig();
		config.setServoControllerId(new ServoController.Id(controllerId));
		readConnectionParams(config, connectionConf);
		List<ServoConf> servoConfigs = readServoList(servoConf);
		for (ServoConf servoConfig : servoConfigs) {
			config.addServoConfig(servoConfig);
		}
		return config;
	}

	private void readConnectionParams(SerialServoControllerConfig config, HierarchicalConfiguration xml) {
		String port = xml.getString(XML_PORT_NAME);
		config.setPortName(port);
		Integer rate = xml.getInt(XML_BAUD_RATE);
		config.setBaudRate(BaudRate.get(rate, BaudRate.BR115200));
	}

	private List<ServoConf> readServoList(HierarchicalConfiguration xml) {
		List<ServoConf> params = new ArrayList();
		List<HierarchicalConfiguration> servos = xml.configurationsAt(XML_SERVO);
		for (HierarchicalConfiguration conf : servos) {
			params.add(readServoParameters(conf));
		}
		return params;
	}

	private ServoConf readServoParameters(HierarchicalConfiguration xml) {
		ServoIdType id = readServoId(xml);
		int minPos = xml.getInt(XML_MIN_POSITION);
		int maxPos = xml.getInt(XML_MAX_POSITON);
		int defPos = xml.getInt(XML_DEFAULT_POSITON);
		String name = xml.getString(XML_JOINT_NAME, "Servo " + id);
		return newServoConfig(id, name, minPos, maxPos, defPos);
	}

	private ServoIdType readServoId(HierarchicalConfiguration xml) {
		String idStr = xml.getString(XML_SERVO_ID);
		if (idStr == null) {
			return null;
		}
		return myServoIdReader.read(idStr);
	}

	protected abstract SerialConf newConfig();

	protected abstract ServoConf newServoConfig(
			ServoIdType id, String name, int minPos, int maxPos, int defPos);
}
