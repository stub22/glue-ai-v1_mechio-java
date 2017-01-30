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
import org.mechio.api.motion.Joint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract class providing common functionality for JointGroups.
 *
 * @param <Id> Type used to identify Joints
 * @param <G>  Type of child JointGroups
 * @param <J>  Type of Joints used by this JointGroup
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class AbstractJointGroup<Id, G extends JointGroup, J extends Joint>
		extends PropertyChangeNotifier implements JointGroup<Id, G, J> {
	private static final Logger theLogger = LoggerFactory.getLogger(AbstractJointGroup.class);

	/**
	 * Name of this JointGroup
	 */
	protected String myName;
	/**
	 * JointIds used by this JointGroup
	 */
	protected List<Id> myJointIds;
	/**
	 * Child JointGroups
	 */
	protected List<G> myGroups;

	private boolean myEnabledFlag;

	/**
	 * Creates an empty AbstractJointGroup
	 */
	public AbstractJointGroup() {
		myJointIds = new ArrayList();
		myGroups = new ArrayList();
		myEnabledFlag = true;
	}

	/**
	 * @param name   name of this JointGroup
	 * @param ids    initial JointIds
	 * @param groups initial child JointGroups
	 */
	public AbstractJointGroup(
			String name, List<Id> ids, List<G> groups) {
		myName = name;
		myJointIds = ids == null ? new ArrayList() :
				new ArrayList<>(ids);
		myGroups = groups == null ? new ArrayList() : new ArrayList<>(groups);
		myEnabledFlag = true;
	}

	/**
	 * Retrieves a Joint with the given jointId.
	 *
	 * @param jointId id of the joint to retrieve
	 * @return Joint with the given jointId, null if no Joint is found
	 */
	protected abstract J getJointById(Id jointId);

	@Override
	public void setName(String name) {
		String oldName = myName;
		myName = name;
		firePropertyChange(PROP_NAME, oldName, myName);
	}

	@Override
	public String getName() {
		return myName;
	}

	@Override
	public void setEnabled(boolean enabled) {
		boolean old = myEnabledFlag;
		myEnabledFlag = enabled;
		for (J joint : getJoints()) {
			if (joint == null) {
				continue;
			}
			joint.setEnabled(enabled);
		}
		for (G group : myGroups) {
			group.setEnabled(enabled);
		}
		firePropertyChange(PROP_ENABLED, old, myEnabledFlag);
	}

	/**
	 * Returns the Joint at the given index, or null if out of bounds.
	 *
	 * @param index index of the Joint to return
	 * @return Joint at the given index, or null if out of bounds
	 */
	@Override
	public J getJoint(int index) {
		if (index < 0 || index >= myJointIds.size()) {
			return null;
		}
		Id jointId = myJointIds.get(index);
		return getJointById(jointId);
	}

	/**
	 * Returns a List of Joints belonging to this JointGroup.
	 *
	 * @return List of Joints belonging to this JointGroup
	 */
	@Override
	public List<J> getJoints() {
		List<J> joints = new ArrayList();
		for (Id id : myJointIds) {
			J joint = getJointById(id);
			joints.add(joint);
		}
		return joints;
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
		firePropertyChange(PROP_ADD_JOINT_ID, null, jointId);
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
		firePropertyChange(PROP_ADD_JOINT_ID, null, jointId);
	}

	@Override
	public void removeJointId(Id jointId) {
		if (!myJointIds.contains(jointId)) {
			theLogger.warn("Unable to remove Joint Id.  Id ''{}'' not found.", jointId);
			return;
		}
		myJointIds.remove(jointId);
		firePropertyChange(PROP_REMOVE_JOINT_ID, null, jointId);
	}

	@Override
	public void removeJointIdAt(int index) {
		Id jointId = myJointIds.remove(index);
		firePropertyChange(PROP_REMOVE_JOINT_ID, null, jointId);
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
	public void addGroup(G group) {
		if (this == group) {
			theLogger.warn("Unable to add JointGroup.  JointGroup cannot contain itself.");
			return;
		}
		if (myGroups.contains(group)) {
			theLogger.warn("Unable to add JointGroup.  JointGroup already exists.");
			return;
		}
		myGroups.add(group);
		firePropertyChange(PROP_ADD_JOINT_GROUP, null, group);
	}

	@Override
	public void insertGroup(G group, int index) {
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
		firePropertyChange(PROP_ADD_JOINT_GROUP, null, group);
	}

	@Override
	public void removeGroup(G group) {
		if (!myGroups.contains(group)) {
			theLogger.warn("Unable to remove JointGroup.  Cannot find JointGroup.");
			return;
		}
		myGroups.remove(group);
		firePropertyChange(PROP_REMOVE_JOINT_GROUP, null, group);
	}

	@Override
	public void removeGroupAt(int index) {
		G group = myGroups.remove(index);
		firePropertyChange(PROP_REMOVE_JOINT_GROUP, null, group);
	}

	@Override
	public List<G> getJointGroups() {
		return Collections.unmodifiableList(myGroups);
	}

	@Override
	public G getJointGroup(int index) {
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
		Iterator<G> groupIt = myGroups.iterator();
		while (groupIt.hasNext()) {
			if (!groups.isEmpty()) {
				groups += ",";
			}
			groups += groupIt.next().toString();
		}
		return String.format(format, myName, ids, groups);
	}
}
