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
package org.mechio.api.motion.jointgroup;

import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Default implementation of a JointGroupConfig
 *
 * @param <Id>          type used to identify Joints
 * @param <GroupConfig> JointGroupConfig used to create children JointGroups
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DefaultJointGroupConfig<Id, GroupConfig extends
		JointGroupConfig<Id, ? extends GroupConfig>> extends
		PropertyChangeNotifier implements JointGroupConfig<Id, GroupConfig> {
	private static final Logger theLogger = LoggerFactory.getLogger(DefaultJointGroupConfig.class);

	private String myName;
	private List<Id> myJointIds;
	private List<GroupConfig> myGroups;
	private boolean myEnabledFlag;

	/**
	 * Creates an empty DefaultJointGroupConfig with the given name.
	 *
	 * @param name name for the JointGroup
	 */
	public DefaultJointGroupConfig(String name) {
		myName = name;
		myJointIds = new ArrayList();
		myGroups = new ArrayList();
		myEnabledFlag = true;
	}

	/**
	 * Creates a new DefaultJointGroupConfig with the given values
	 *
	 * @param name   name for the JointGroup
	 * @param ids    initial JointIds
	 * @param groups initial child JointGroupConfigs
	 */
	public DefaultJointGroupConfig(String name, List<? extends Id> ids, List<? extends GroupConfig> groups) {
		myName = name;
		myJointIds = ids == null ? new ArrayList() : new ArrayList(ids);
		myGroups = groups == null ? new ArrayList() : new ArrayList(groups);
		myEnabledFlag = true;
	}

	@Override
	public void setName(String name) {
		String oldName = name;
		myName = name;
		firePropertyChange(JointGroupConfig.PROP_NAME, oldName, myName);
	}

	@Override
	public String getName() {
		return myName;
	}

	@Override
	public void setEnabled(boolean enabled) {
		boolean old = myEnabledFlag;
		myEnabledFlag = enabled;
		firePropertyChange(JointGroupConfig.PROP_ENABLED, old, myEnabledFlag);
	}

	@Override
	public boolean getEnabled() {
		return myEnabledFlag;
	}

	@Override
	public void addJointId(Id jointId) {
		if (myJointIds.contains(jointId)) {
			theLogger.warn("Unable to add Joint Id.  Id ''{}'' already exists.", jointId);
			return;
		}
		myJointIds.add(jointId);
		firePropertyChange(JointGroupConfig.PROP_ADD_JOINT_ID, null, jointId);
	}

	@Override
	public void insertJointId(Id jointId, int index) {
		if (myJointIds.contains(jointId)) {
			theLogger.warn("Unable to add Joint Id.  Id ''{}'' already exists.", jointId);
			return;
		}
		if (index < 0 || index > myJointIds.size()) {
			throw new IllegalArgumentException("Unable to add JointId.  Index '" + index + "'out of range.");
		}
		myJointIds.add(index, jointId);
		firePropertyChange(JointGroupConfig.PROP_ADD_JOINT_ID, null, jointId);
	}

	@Override
	public void removeJointId(Id jointId) {
		if (!myJointIds.contains(jointId)) {
			theLogger.warn("Unable to remove Joint Id.  Id ''{}'' not found.", jointId);
			return;
		}
		myJointIds.remove(jointId);
		firePropertyChange(JointGroupConfig.PROP_REMOVE_JOINT_ID, null, jointId);
	}

	@Override
	public void removeJointIdAt(int index) {
		Id jointId = myJointIds.remove(index);
		firePropertyChange(JointGroupConfig.PROP_REMOVE_JOINT_ID, null, jointId);
	}

	@Override
	public List<Id> getJointIds() {
		return Collections.unmodifiableList(myJointIds);
	}

	@Override
	public Id getJointId(int index) {
		return myJointIds.get(index);
	}

	@Override
	public int getJointCount() {
		return myJointIds.size();
	}

	@Override
	public void addGroup(GroupConfig group) {
		if (this == group) {
			theLogger.warn("Unable to add JointGroup.  JointGroup cannot contain itself.");
			return;
		}
		if (myGroups.contains(group)) {
			theLogger.warn("Unable to add JointGroup.  JointGroup already exists.");
			return;
		}
		myGroups.add(group);
		firePropertyChange(JointGroupConfig.PROP_ADD_JOINT_GROUP, null, group);
	}

	@Override
	public void insertGroup(GroupConfig group, int index) {
		if (this == group) {
			theLogger.warn("Unable to add JointGroup.  JointGroup cannot contain itself.");
			return;
		}
		if (myGroups.contains(group)) {
			theLogger.warn("Unable to add JointGroup.  JointGroup already exists.");
			return;
		}
		if (index < 0 || index > myGroups.size()) {
			throw new IllegalArgumentException("Unable to add JointGroup.  Index '" + index + "'out of range.");
		}
		myGroups.add(index, group);
		firePropertyChange(JointGroupConfig.PROP_ADD_JOINT_GROUP, null, group);
	}

	@Override
	public void removeGroup(GroupConfig group) {
		if (!myGroups.contains(group)) {
			theLogger.warn("Unable to remove JointGroup.  Cannot find JointGroup.");
			return;
		}
		myGroups.remove(group);
		firePropertyChange(JointGroupConfig.PROP_REMOVE_JOINT_GROUP, null, group);
	}

	@Override
	public void removeGroupAt(int index) {
		JointGroupConfig group = myGroups.remove(index);
		firePropertyChange(JointGroupConfig.PROP_REMOVE_JOINT_GROUP, null, group);
	}

	@Override
	public List<GroupConfig> getJointGroups() {
		return Collections.unmodifiableList(myGroups);
	}

	@Override
	public GroupConfig getJointGroup(int index) {
		return myGroups.get(index);
	}

	@Override
	public int getGroupCount() {
		return myGroups.size();
	}

	@Override
	public String toString() {
		String format = "{\"name\":\"%s\", \"jointIds\":[%s], \"groups\":[%s]}";
		String ids = "";
		Iterator<Id> idIt = myJointIds.iterator();
		while (idIt.hasNext()) {
			if (!ids.isEmpty()) {
				ids += ",";
			}
			ids += idIt.next().toString();
		}
		String groups = "";
		Iterator<GroupConfig> groupIt = myGroups.iterator();
		while (groupIt.hasNext()) {
			if (!groups.isEmpty()) {
				groups += ",";
			}
			groups += groupIt.next().toString();
		}
		return String.format(format, myName, ids, groups);
	}
}
