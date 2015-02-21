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

import org.jflux.api.common.rk.property.PropertyChangeSource;

/**
 * Configuration parameters defining a Servo.
 * 
 * @param <Id> Servo Id Type used
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface ServoConfig<Id> extends PropertyChangeSource {
    /**
     * Property string for Servo logical id.
     */
    public final static String PROP_ID = "logicalId";
    /**
     * Property string for Servo name.
     */
    public final static String PROP_NAME = "name";
    /**
     * Property string for Servo minimum position.
     */
    public final static String PROP_MIN_POSITION = "minPosition";
    /**
     * Property string for Servo maximum position.
     */
    public final static String PROP_MAX_POSITION = "maxPosition";
    /**
     * Property string for Servo default position.
     */
    public final static String PROP_DEF_POSITION = "defaultPosition";
    /**
     * Returns the Servo id.
     * @return Servo id
     */
    public Id getServoId();

    /**
     * Sets the Servo id.
     * @param id new Servo id
     */
    public void setServoId(Id id);

    /**
     * Returns the Servo name.
     * @return Servo name
     */
    public String getName();

    /**
     * Sets the Servo name.
     * @param name new Servo name
     */
    public void setName(String name);

    /**
     * Returns the Servo minimum position.
     * @return Servo minimum position
     */
    public int getMinPosition();

    /**
     * Sets the Servo minimum position.
     * @param pos new Servo minimum position
     */
    public void setMinPosition(Integer pos);

    /**
     * Returns the Servo maximum position.
     * @return Servo maximum position
     */
    public int getMaxPosition();

    /**
     * Sets the Servo maximum position.
     * @param pos new Servo maximum position
     */
    public void setMaxPosition(Integer pos);

    /**
     * Returns the Servo default position.
     * @return Servo default position
     */
    public int getDefaultPosition();

    /**
     * Sets the Servo default position.
     * @param pos new Servo default position
     */
    public void setDefaultPosition(Integer pos);
}
