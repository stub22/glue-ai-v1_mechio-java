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

package org.mechio.api.motion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.property.PropertyChangeSource;
import org.jflux.impl.services.rk.utils.GlobalIdentifier;
import org.jflux.impl.services.rk.utils.HashCodeUtil;
import org.mechio.api.motion.protocol.JointPositionMap;

/**
 *A Robot provides access to a List of Joints backed by a List of 
 * JointControllers.
 * 
 * @param <J> type of Joint used by this Robot
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface Robot<J extends Joint> extends PropertyChangeSource{
    /**
     * Property String for the Robot Id.
     */
    public final static String PROP_ID = "robotId";
    /**
     * Property String for the connection status.
     */
    public final static String PROP_CONNECTED = "isConnected";
    /**
     * Property String for the enabled status.
     */
    public final static String PROP_ENABLED = "isEnabled";
    /**
     * Returns a String uniquely identifying this Robot.
     * @return unique identifier for this Robot
     */
    public Id getRobotId();

    /**
     * The Robot will accept commands only after it is successfully connected.
     * @return true is successful
     */
    public boolean connect();
    /**
     * Disconnect the Robot.
     */
    public void disconnect();
    /**
     * Returns true if the Robot is connected.
     * @return true if the Robot is connected
     */
    public boolean isConnected();
    /**
     * Sets the enabled status of the Robot.  The Robot only accepts new 
     * movements when Enabled is set to true.
     * @param val enabled value
     */
    public void setEnabled(boolean val);
    /**
     * Returns true if the Robot is enabled and accepting commands.
     * @return true if the Robot is enabled and accepting commands
     */
    public boolean isEnabled();
    /**
     * Return the Joint with the given id.
     * @param id Joint's logical id
     * @return Joint with the given logical id, null if no Joint is found
     */
    public J getJoint(Robot.JointId id);
    /**
     * Returns a  set of the Robot's Joint ids.
     * @return set of the Robot's Joint ids
     */
    public Set<Robot.JointId> getJointIds();
    /**
     * Returns a List of the Robot's Joints.
     * @return List of the Robot's Joints
     */
    public List<J> getJointList();

    /**
     * The Robot's name for the Joint with the given logical id.
     * @param id Joint's logical id
     * @return Robot's name for the Joint with the given logical id
     */
    public String getJointName(Robot.JointId id);
    /**
     * Returns a map of the Robot's Joint's ids and their default positions.
     * @return map of the Robot's Joint's ids and their default positions
     */
    public RobotPositionMap getDefaultPositions();
    /**
     * Returns a map of the Robot's Joint's ids and their current positions.
     * @return map of the Robot's Joint's ids and their current positions
     */
    public RobotPositionMap getCurrentPositions();
    /**
     * Returns a map of the Robot's Joint's ids and their goal positions.
     * @return map of the Robot's Joint's ids and their goal positions
     */
    public RobotPositionMap getGoalPositions();
    /**
     * Move the Joints with the given ids to the corresponding positions.
     * @param positions map of Joint logical ids and positions
     * @param lenMillisec duration of the movement in milliseconds 
     */
    public void move(RobotPositionMap positions, long lenMillisec);
    
    /**
     * RobotPositionMap expected by the Robot.
     */
    public static interface RobotPositionMap extends
            JointPositionMap<Robot.JointId,NormalizedDouble> {}
    /**
     * RobotPositionMap backed by java.util.HashMap.
     */
    public static class RobotPositionHashMap extends 
        HashMap<Robot.JointId,NormalizedDouble> implements RobotPositionMap {
        /**
         * @see HashMap
         */
        public RobotPositionHashMap(){}
        /**
         * @param initialCapacity 
         * @see HashMap
         */
        public RobotPositionHashMap(int initialCapacity){
            super(initialCapacity);
        }
        /**
         * @param initialCapacity 
         * @param loadFactor 
         * @see HashMap
         */
        public RobotPositionHashMap(int initialCapacity, float loadFactor){
            super(initialCapacity, loadFactor);
        }
        /**
         * @param m 
         * @see HashMap
         */
        public RobotPositionHashMap(Map<? extends Robot.JointId, 
                ? extends NormalizedDouble> m){
            super(m);
        }
    }
    
    /**
     * Id is an immutable globally unique identifier for a Robot.
     */
    public static class Id implements GlobalIdentifier{
        private String myRobotIdString;
        private int myHashCode;
        
        /**
         * 
         * @param robotId
         */
        public Id(String robotId){
            if(robotId == null){
                throw new NullPointerException();
            }else if(robotId.isEmpty()){
                throw new NullPointerException("Robot.Id cannot be empty.");
            }
            myRobotIdString = robotId;
        }
        
        @Override
        public boolean equals(Object obj){
            if(obj == null ||  obj.getClass() != this.getClass()){
                return false;
            }
            Id id = (Id)obj;
            return myRobotIdString.equals(id.myRobotIdString);
        }

        @Override
        public int hashCode() {
            if(myHashCode == 0){
                myHashCode = 
                        HashCodeUtil.hash(HashCodeUtil.SEED, myRobotIdString);
            }
            return myHashCode;
        }
        /**
         * Returns the String value of this Robot.Id.
         * @return String value of this Robot.Id
         */
        public String getRobtIdString(){
            return myRobotIdString;
        }
        
        @Override
        public String toString() {
            return myRobotIdString;
        }
    }
    
    /**
     * JointId is an immutable globally unique identifier for a Joint 
     * belonging to a Robot.  The JointId is a combination of a 
     * Robot.Id and Joint.Id.
     */
    public static class JointId implements GlobalIdentifier{
        private Id myRobotId;
        private Joint.Id myJointId;
        private int myHashCode;
        
        /**
         * Creates a new Robot.JointId with the given Robot.Id and Joint.Id.
         * @param robotId Id of the Robot with the Joint
         * @param jointId Local Id of the Joint
         */
        public JointId(Id robotId, Joint.Id jointId){
            if(robotId == null || jointId == null){
                throw new NullPointerException();
            }
            myRobotId = robotId;
            myJointId = jointId;
        }
        
        /**
         * Returns the value of the Id.
         * @return the value of the Id
         */
        public final Joint.Id getJointId(){
            return myJointId;
        }
        
        /**
         * Returns the value of the Id.
         * @return the value of the Id
         */
        public final Robot.Id getRobotId(){
            return myRobotId;
        }
        
        @Override
        public boolean equals(Object obj){
            if(obj == null ||  obj.getClass() != this.getClass()){
                return false;
            }
            JointId id = (JointId)obj;
            return myRobotId.equals(id.myRobotId)
                    && myJointId.equals(id.myJointId);
        }

        @Override
        public int hashCode() {
            if(myHashCode == 0){
                myHashCode = HashCodeUtil.hash(
                        HashCodeUtil.SEED, myRobotId, myJointId);
            }
            return myHashCode;
        }

        @Override
        public String toString() {
            return myRobotId.toString() + "::" + myJointId.toString();
        }
    }
}
