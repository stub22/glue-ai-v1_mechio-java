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

package org.mechio.impl.motion.jointgroup;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ConfigurationLoader;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.Id;
import org.mechio.api.motion.Robot.JointId;
import org.mechio.api.motion.jointgroup.RobotJointGroupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotJointGroupConfigXMLReader
		implements ConfigurationLoader<RobotJointGroupConfig, HierarchicalConfiguration> {
	private static final Logger theLogger = LoggerFactory.getLogger(RobotJointGroupConfigXMLReader.class);
	/**
	 * Config format version name.
	 */
	public final static String CONFIG_TYPE = "RobotJointGroup XML";
	/**
	 * Config format version number.
	 */
	public final static String CONFIG_VERSION = "1.0";
	/**
	 * Config format VersionProperty.
	 */
	public final static VersionProperty VERSION = new VersionProperty(CONFIG_TYPE, CONFIG_VERSION);
	/**
	 * JointGroup XML Element.
	 */
	public final static String XML_JOINT_GROUP = "JointGroup";
	/**
	 * JointGroup Name XML attribute.
	 */
	public final static String XML_JOINT_GROUP_NAME_ATTR = "name";
	/**
	 * ROBOTJointGroup Id XML Element.
	 */
	public final static String XML_ROBOT_ID = "RobotId";
	/**
	 * Id List XML Element.
	 */
	public final static String XML_JOINT_ID_LIST = "JointIds";
	/**
	 * Id XML Element.
	 */
	public final static String XML_JOINT_ID = "JointId";
	/**
	 * JointGroup List XML Element.
	 */
	public final static String XML_JOINT_GROUP_LIST = "JointGroups";

	@Override
	public VersionProperty getConfigurationFormat() {
		return VERSION;
	}

	@Override
	public RobotJointGroupConfig loadConfiguration(HierarchicalConfiguration param) {
		return readJointGroup(null, param);
	}

	@Override
	public Class<RobotJointGroupConfig> getConfigurationClass() {
		return RobotJointGroupConfig.class;
	}

	@Override
	public Class<HierarchicalConfiguration> getParameterClass() {
		return HierarchicalConfiguration.class;
	}

	public static RobotJointGroupConfig loadJointGroup(String path) throws ConfigurationException, Throwable {
		RobotJointGroupConfig group = null;
		try {
			HierarchicalConfiguration config = new XMLConfiguration(path);
			group = readJointGroup(null, config);
			return group;
		} catch (ConfigurationException t) {
			theLogger.warn("Cannot open XML file at: {}", path, t);
			throw t;
		} catch (Throwable t) {
			theLogger.warn("There was an error reading the JointGroup.", t);
			throw t;
		}
	}

	public static RobotJointGroupConfig readJointGroup(Id defDevId, HierarchicalConfiguration xml) {
		if (xml == null) {
			return null;
		}
		String nameKey = "[@" + XML_JOINT_GROUP_NAME_ATTR + "]";
		String name = xml.getString(nameKey, "Joint Group");
		List<JointId> ids = null;
		Id robotId = defDevId;
		if (robotId == null) {
			robotId = readRobotId(xml);
		}
		if (robotId == null) {
			throw new IllegalArgumentException("Could not find " +
					XML_ROBOT_ID + " element.");
		}
		List<HierarchicalConfiguration> jointIdsXml =
				xml.configurationsAt(XML_JOINT_ID_LIST);
		if (!jointIdsXml.isEmpty()) {
			ids = readJointIdList(robotId, jointIdsXml.get(0));
		}
		List<RobotJointGroupConfig> groups = null;
		List<HierarchicalConfiguration> groupsXml =
				xml.configurationsAt(XML_JOINT_GROUP_LIST);
		if (!groupsXml.isEmpty()) {
			groups = readGroupList(robotId, groupsXml.get(0));
		}
		RobotJointGroupConfig group = new RobotJointGroupConfig(name, robotId, ids, groups);
		return group;
	}

	private static Id readRobotId(HierarchicalConfiguration xml) {
		String idStr = xml.getString(XML_ROBOT_ID);
		if (idStr == null || idStr.isEmpty()) {
			return null;
		}
		return new Id(idStr);
	}

	private static List<JointId> readJointIdList(
			Id robotId, HierarchicalConfiguration xml) {
		List<HierarchicalConfiguration> idsXml =
				xml.configurationsAt(XML_JOINT_ID);
		List<JointId> ids = new ArrayList<>(idsXml.size());
		for (HierarchicalConfiguration conf : idsXml) {
			if (conf == null || conf.isEmpty()) {
				theLogger.warn(
						"Skipping empty {} element.", XML_JOINT_ID);
				continue;
			}
			Integer id = conf.getInteger("", null);
			if (id == null) {
				theLogger.warn(
						"Unable to parse {} element.", XML_JOINT_ID);
				continue;
			}
			ids.add(new Robot.JointId(robotId, new Joint.Id(id)));
		}
		return ids;
	}

	private static List<RobotJointGroupConfig> readGroupList(Id robotId, HierarchicalConfiguration xml) {
		List<HierarchicalConfiguration> groupsXml =
				xml.configurationsAt(XML_JOINT_GROUP);
		List<RobotJointGroupConfig> groups = new ArrayList(groupsXml.size());
		for (HierarchicalConfiguration conf : groupsXml) {
			if (conf == null || conf.isEmpty()) {
				theLogger.warn(
						"Skipping empty {} element.", XML_JOINT_GROUP);
				continue;
			}
			RobotJointGroupConfig group = readJointGroup(robotId, conf);
			if (group == null) {
				theLogger.warn(
						"Unable to parse {} element.", XML_JOINT_GROUP);
				continue;
			}
			groups.add(group);
		}
		return groups;
	}
}
