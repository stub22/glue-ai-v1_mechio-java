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

package org.mechio.api.motion.servos.config;

import java.util.Map;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.property.PropertyChangeSource;
import org.mechio.api.motion.servos.ServoController;

/**
 * Parameters needed to initialize a ServoController.
 * @param <Id> Servo Id Type
 * @param <ServoConf> ServoConfig type used by the ServoControllerConfig
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface ServoControllerConfig<
        Id, ServoConf extends ServoConfig<Id>> extends PropertyChangeSource{
    /**
     * Property string for the ServoControllerConfig ControllerTypeVersion.
     */
    public final static String PROP_CONTROLLER_TYPE = "controllerTypeVersion";
    /**
     * Property string for the ServoControllerConfig AddServo.
     */
    public final static String PROP_SERVO_ADD = "addServo";
    /**
     * Property string for the ServoControllerConfig RemoveServo.
     */
    public final static String PROP_SERVO_REMOVE = "removeServo";
    /**
     * Property string for the ServoControllerConfig Servos.
     */
    public final static String PROP_SERVOS = "servos";

    /**
     * Returns the Id of the ServoController.
     * @return Id of the ServoController
     */
    public ServoController.Id getServoControllerId();
    /**
     * Returns ControllerType VersionProperty.
     * @return ControllerType VersionProperty
     */
    public VersionProperty getControllerTypeVersion();
    /**
     * Sets ControllerType VersionProperty.
     * @param version new ControllerType VersionProperty
     */
    public void setControllerTypeVersion(VersionProperty version);
    
    /**
     * Returns the number of Servos in the ServoControllerConfig.
     * @return number of Servos in the ServoControllerConfig
     */
    public int getServoCount();
    /**
     * Returns a map of ids and ServoConfigs.
     * @return map of ids and ServoConfigs
     */
    public Map<Id, ServoConf> getServoConfigs();
    /**
     * Adds a ServoConfig to the ServoControllerConfig.
     * @param config ServoConfig to add
     */
    public void addServoConfig(ServoConf config);
    /**
     * Removes a ServoConfig from the ServoControllerConfig.
     * @param config ServoConfig to remove
     */
    public void removeServoConfig(ServoConf config);
}
