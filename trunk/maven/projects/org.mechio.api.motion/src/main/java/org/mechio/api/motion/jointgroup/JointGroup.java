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
import org.mechio.api.motion.Joint;

/**
 * JointGroups give a hierarchical view of a set of Joints. This is especially
 * useful in the user interface for creating a Tree of Joints.  
 * A JointGroup provides a name, a list of Joints and a list of child 
 * JointGroups.
 * 
 * @param <Id> Joint Id Type used by this JointGroup
 * @param <G> JointGroup Type of child JointGroups
 * @param <J> Joint Type used by this JointGroup
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface JointGroup<Id, G extends JointGroup, J extends Joint> extends 
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
     * Property String for changing a joint
     */
    public final static String PROP_JOINT_CHANGED = "jointChanged";
    /**
     * Property String for adding a child JointGroup
     */
    public final static String PROP_ADD_JOINT_GROUP = "addJointGroup";
    /**
     * Property String for removing a child JointGroup
     */
    public final static String PROP_REMOVE_JOINT_GROUP = "removeJointGroup";
    /**
     * Property String for changing the structure of this JointGroup
     */
    public final static String PROP_STRUCTURE_CHANGED = "structureChanged";
    
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
     * Sets the enabled state of the JointGroup
     * @param enabled enabled state to use
     */
    public void setEnabled(boolean enabled);
    
    /**
     * Returns the enabled state
     * @return the enabled state
     */
    public boolean getEnabled();
    
    /**
     * Adds a JointId to this JointGroup.  If the given id already exists in
     * this JointGroup, it is ignored.
     * @param jointId the id to add
     */
    public void addJointId(Id jointId);
    
    /**
     * Adds a JointId to this JointGroup at the given index.  If the given id 
     * already exists in this JointGroup, it is ignored.
     * @param jointId the id to add
     * @param index index at which to insert the Joint Id
     */
    public void insertJointId(Id jointId, int index);
    
    /**
     * Removes a JointId
     * @param jointId id to remove
     */
    public void removeJointId(Id jointId);
    
    /**
     * Removes the JointId with the given index.
     * @param index position of the JointId in this JointGroup's JointIds
     */
    public void removeJointIdAt(int index);
    
    /**
     * Returns a List of JointIds for this JointGroup
     * @return a List of JointIds for this JointGroup
     */
    public List<Id> getJointIds();
    
    /**
     * Returns the JointId at the given index.
     * @param index position of the JointId in the JointGroup's JointIds
     * @return the JointId at the given index
     */
    public Id getJointId(int index);
    
    /**
     * Returns the Joint at the given index.
     * @param index index of the desired Joint
     * @return Joint at the given index
     */
    public J getJoint(int index);
    
    /**
     * Returns the Joints belonging to this JointGroup.
     * @return Joints belonging to this JointGroup
     */
    public List<J> getJoints();
    
    /**
     * Returns the number of JointIds for this JointGroup.
     * @return the number of JointIds for this JointGroup
     */
    public int getJointCount();
    
    /**
     * Add a child JointGroup.
     * @param group child JointGroup to add
     */
    public void addGroup(G group);
    
    /**
     * Add a child JointGroup at the given index.
     * @param group child JointGroup to add
     * @param index position to add the child
     */
    public void insertGroup(G group, int index);
    
    /**
     * Removes a child JointGroup.
     * @param group the child to remove
     */
    public void removeGroup(G group);
    
    /**
     * Removes a child JointGroup at the given index.
     * @param index position of the child to remove
     */
    public void removeGroupAt(int index);
    
    /**
     * Returns a List of the children JointGroups.
     * @return a List of the children JointGroups
     */
    public List<G> getJointGroups();
    
    /**
     * Returns the child JointGroup at the given index.
     * @param index position to the child to remove
     * @return the child JointGroup removed
     */
    public G getJointGroup(int index);
    
    /**
     * Return the number of child JointGroups.
     * @return the number of child JointGroups
     */
    public int getGroupCount();    
}
