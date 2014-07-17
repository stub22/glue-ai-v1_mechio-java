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

import java.util.List;
import org.jflux.api.common.rk.property.PropertyChangeSource;

/**
 * Common interface for JointGroup configurations.
 * @param <Id> type of identifier for this JointGroup's Joints
 * @param <GroupConfig> type of JointGroupConfiguration used by children 
 * JointGroups
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface JointGroupConfig<Id, GroupConfig extends 
                JointGroupConfig<Id,? extends GroupConfig>> extends 
        PropertyChangeSource{    
    /**
     * Property String for name
     */
    public final static String PROP_NAME = "name";
    /**
     * Property String for enabled
     */
    public final static String PROP_ENABLED = "enabled";
    /**
     * Property String for adding a JointId
     */
    public final static String PROP_ADD_JOINT_ID = "addJointId";
    /**
     * Property String for removing a JointId
     */
    public final static String PROP_REMOVE_JOINT_ID = "removeJointId";
    /**
     * Property String for adding a child JointGroup
     */
    public final static String PROP_ADD_JOINT_GROUP = "addJointGroup";
    /**
     * Property String for removing a child JointGroup
     */
    public final static String PROP_REMOVE_JOINT_GROUP = "removeJointGroup";
    
    /**
     * Sets the name of the JointGroup
     * @param name new name to use
     */
    public void setName(String name);
    
    /**
     * Returns the name to use
     * @return the name to use
     */
    public String getName();
    
    /**
     * Sets the default enabled state of the JointGroup
     * @param enabled default enabled state to use
     */
    public void setEnabled(boolean enabled);
    
    /**
     * Returns the default enabled state
     * @return the default enabled state
     */
    public boolean getEnabled();
    
    /**
     * Adds a JointId to the JointGroupConfig
     * @param jointId id to add
     */
    public void addJointId(Id jointId);
    
    /**
     * Adds a JointId at the given index
     * @param jointId id to add
     * @param index index for the jointId
     */
    public void insertJointId(Id jointId, int index);
    
    /**
     * Removes a JointId
     * @param jointId id to remove
     */
    public void removeJointId(Id jointId);
    
    /**
     * Removes the JointId with the given index.
     * @param index position of the JointId in this JointGroupConfig's JointIds
     */
    public void removeJointIdAt(int index);
    
    /**
     * Returns a List of JointIds for this JointGroup
     * @return a List of JointIds for this JointGroup
     */
    public List<Id> getJointIds();
    
    /**
     * Returns the JointId at the given index.
     * @param index position of the JointId in the JointGroupConfig's JointIds
     * @return the JointId at the given index
     */
    public Id getJointId(int index);
    
    /**
     * Returns the number of JointIds for this JointGroupConfig.
     * @return the number of JointIds for this JointGroupConfig
     */
    public int getJointCount();
    
    /**
     * Add a child JointGroupConfig.
     * @param group child JointGroupConfig to add
     */
    public void addGroup(GroupConfig group);
    
    /**
     * Add a child JointGroupConfig at the given index.
     * @param group child JointGroupConfig to add
     * @param index position to add the child
     */
    public void insertGroup(GroupConfig group, int index);
    
    /**
     * Removes a child JointGroupConfig.
     * @param group the child to remove
     */
    public void removeGroup(GroupConfig group);
    
    /**
     * Removes a child JointGroupConfig at the given index.
     * @param index position of the child to remove
     */
    public void removeGroupAt(int index);
    
    /**
     * Returns a List of the children JointGroupConfigs.
     * @return a List of the children JointGroupConfigs
     */
    public List<GroupConfig> getJointGroups();
    
    /**
     * Returns the child JointGroupConfig at the given index.
     * @param index position to the child to remove
     * @return the child JointGroupConfig removed
     */
    public GroupConfig getJointGroup(int index);
    
    /**
     * Return the number of child JointGroupConfigs.
     * @return the number of child JointGroupConfigs
     */
    public int getGroupCount();
}
