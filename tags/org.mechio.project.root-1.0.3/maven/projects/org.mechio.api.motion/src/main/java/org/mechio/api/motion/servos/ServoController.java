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

package org.mechio.api.motion.servos;

import org.mechio.api.motion.servos.utils.ConnectionStatus;
import java.util.List;
import java.util.Set;
import org.jflux.api.common.rk.property.PropertyChangeSource;
import org.jflux.impl.services.rk.utils.HashCodeUtil;
import org.jflux.impl.services.rk.utils.LocalIdentifier;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.api.motion.servos.config.ServoControllerConfig;

/**
 * A ServoController provides control of a collection of Servos and the means of
 * directly controlling them.
 * @param <IdType> Identifier type used by this ServoController's Servos
 * @param <ServoType> ServoController's Servo type
 * @param <ServoConf> ServoConfig type
 * @param <ControllerConf> ServoControllerConfig type
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface ServoController<
            IdType, 
            ServoConf extends ServoConfig<IdType>,
            ServoType extends Servo<IdType,ServoConf>,
            ControllerConf extends ServoControllerConfig<IdType,ServoConf>> 
        extends PropertyChangeSource {
    /**
     * Used to specify the VersionProperty of a type of ServoController.
     * This is used when registering a Service for a ServoController with OSGi.
     */
    public final static String PROP_VERSION = "servoControllerVersion";
    /**
     * Property string for ConnectionStatus.
     */
    public final static String PROP_CONNECTION_STATUS = "connectionStatus";
    /**
     * Property string for ErrorMessages.
     */
    public final static String PROP_ERROR_MESSAGES = "errorMessages";
    /**
     * Property string for AddServo.
     */
    public final static String PROP_SERVO_ADD = "addServo";
    /**
     * Property string for RemoveServo.
     */
    public final static String PROP_SERVO_REMOVE = "removeServo";
    /**
     * Property string for Servos.
     */
    public final static String PROP_SERVOS = "Servos";
    /**
     * Property string for Enabled.
     */
    public final static String PROP_ENABLED = "enabled";

    /**
     * Returns the ServoController.Id identifying this ServoController. 
     * @return 
     */
    public ServoController.Id getId();
    /**
     * Returns a List of the controller's Servos.
     * @return List of the controller's Servos
     */
    public List<ServoType> getServos();
    /**
     * Returns the Servo with the given id.
     * @param id the Servo id
     * @return Servo with the given id
     */
    public ServoType getServo(ServoId<IdType> id);
    /**
     * Returns true if the controller contains a Servo for each of the given 
     * Servo ids.
     * @param ids Set of Servo ids to check
     * @return true if the controller contains a Servo for each of the given 
     * Servo ids
     */
    public boolean containsIds(Set<ServoId<IdType>> ids);
    /**
     * Returns true if the controller contains a Servo for the given Servo id.
     * @param id Servo id to check
     * @return true if the controller contains a Servo for the given Servo id
     */
    public boolean containsId(ServoId<IdType> id);

    /**
     * Connects the ServoController to the underlying device to begin using.
     * The ServoController is expected to be inoperable before calling connect.
     * @return true if successfully connected
     */
    public boolean connect();
    /**
     * Disconnects the underlying device, freeing any resources that have been 
     * locked.
     * @return true id successfully disconnected
     */
    public boolean disconnect();

    /**
     * Move the Servo with the given Servo id to its goal position in the given 
     * time.
     * @param id Servo id of the Servo to move
     * @param lenMillisec number of milliseconds for the movement to take
     * @return false if there is an error moving the Servo
     */
    public boolean moveServo(ServoId<IdType> id, long lenMillisec);
    /**
     * Move the Servos with the given Servo ids to their goal positions.  If
     * the controller does not contain a given id, that id is ignored.
     * @param ids Servo ids of the Servos to move
     * @param len number of ids
     * @param offset start index
     * @param lenMillisec number of milliseconds for the movement to take
     * @return false is there is an error moving a Servo
     */
    public boolean moveServos(ServoId<IdType>[] ids, int len, int offset, long lenMillisec);
    /**
     * Move all of the controller's Servos.
     * @param lenMillisec number of milliseconds for the movement to take
     * @return false if there is an error with one or more Servos
     */
    public boolean moveAllServos(long lenMillisec);

    /**
     * Return the current ConnectionStatus.
     * @return current ConnectionStatus
     */
    public ConnectionStatus getConnectionStatus();
    /**
     * Returns the current error messages.
     * @return current error messages
     */
    public List<String> getErrorMessages();

    /**
     * Returns the ServoController's ServoControllerConfig.
     * @return this ServoController's ServoControllerConfig
     */
    public ControllerConf getConfig();
    
    /**
     * If enabled, this ServoController should accept move commands.
     * If not enabled, this ServoController should not move.
     * @param enabled 
     */
    public void setEnabled(Boolean enabled);
    
    /**
     * If enabled, this ServoController should accept move commands.
     * If not enabled, this ServoController should not move.
     * @return true if enabled
     */
    public Boolean getEnabled();
    
    /**
     * Returns the Class of the Id used by the ServoController's Servos.
     * @return Class of the Id used by the ServoController's Servos
     */
    public Class<IdType> getServoIdClass();
    
    /**
     * Id is an immutable globally unique identifier for a ServoController.
     */
    public static class Id implements LocalIdentifier{
        private String myControllerId;
        private int myHashCode;
        
        /**
         * Creates a ServoController.Id from the given String
         * @param servoControllerId ServoController.Id as a String
         */
        public Id(String servoControllerId){
            if(servoControllerId == null){
                throw new NullPointerException();
            }else if(servoControllerId.isEmpty()){
                throw new NullPointerException("ServoController.Id cannot be empty.");
            }
            myControllerId = servoControllerId;
        }
        
        @Override
        public boolean equals(Object obj){
            if(obj == null ||  obj.getClass() != this.getClass()){
                return false;
            }
            Id id = (Id)obj;
            return myControllerId.equals(id.myControllerId);
        }

        @Override
        public int hashCode() {
            if(myHashCode == 0){
                myHashCode = HashCodeUtil.hash(HashCodeUtil.SEED, myControllerId);
            }
            return myHashCode;
        }

        @Override
        public String toString() {
            return myControllerId;
        }
    }
    
    /**
     * ServoId is an immutable globally unique identifier for a Servo 
     * belonging to a Controller.  The ServoId is a combination of a 
     * Robot.Id and Joint.Id.
     * @param <ServoIdType> Type of Id used by the Servos
     */
    public static class ServoId<ServoIdType> implements LocalIdentifier{
        private Id myControllerId;
        private ServoIdType myServoId;
        private int myHashCode;
        
        /**
         * Creates a ServoController.ServoId from the given ServoController.Id
         * and Servo Id.
         * @param controllerId ServoController.Id to use
         * @param servoId Servo Id to use
         */
        public ServoId(Id controllerId, ServoIdType servoId){
            if(controllerId == null || servoId == null){
                throw new NullPointerException();
            }
            myControllerId = controllerId;
            myServoId = servoId;
        }
        
        /**
         * Returns the value of the Id.
         * @return the value of the Id
         */
        public final ServoIdType getServoId(){
            return myServoId;
        }
        
        /**
         * Returns the value of the Id.
         * @return the value of the Id
         */
        public final Id getControllerId(){
            return myControllerId;
        }
        
        @Override
        public boolean equals(Object obj){
            if(obj == null ||  obj.getClass() != this.getClass()){
                return false;
            }
            ServoId id = (ServoId)obj;
            return myControllerId.equals(id.myControllerId)
                    && myServoId.equals(id.myServoId);
        }

        @Override
        public int hashCode() {
            if(myHashCode == 0){
                myHashCode = HashCodeUtil.hash(
                        HashCodeUtil.SEED, myControllerId, myServoId);
            }
            return myHashCode;
        }

        @Override
        public String toString() {
            return myControllerId.toString() + "::" + myServoId.toString();
        }
    }
}
