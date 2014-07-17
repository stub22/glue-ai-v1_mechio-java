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

import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.property.PropertyChangeSource;
import org.mechio.api.motion.servos.config.ServoConfig;

/**
 * A Servo provides control of a physical servo belonging to a servo control 
 * board.
 * 
 * @param <Id> Id Type used by the Servo
 * @param <Conf> ServoConfig type for this Servo 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface Servo<Id, Conf extends ServoConfig> extends 
        PropertyChangeSource {
    /**
     * Property string for GoalPostion.
     */
    public final static String PROP_GOAL_POSITION = "goalPosition";
    /**
     * Property string for Enabled.
     */
    public final static String PROP_ENABLED = "enabled";
    /**
     * Returns the Servo's id.
     * @return Servo's id
     */
    public Id getId();
    /**
     * Returns the goal position.
     * @return current goal position
     */
    public NormalizedDouble getGoalPosition();
    /**
     * Sets the goal position.
     * @param pos the goal position
     */
    public void setGoalPosition(NormalizedDouble pos);
    /**
     * Returns the Servo's configuration values.
     * @return Servo's configuration values
     */
    public Conf getConfig();
    /**
     * Returns the Servo's parent ServoController.
     * @return Servo's parent ServoController
     */
    public ServoController getController();
    
    /**
     * If enabled, this Servo will accept move commands.
     * If not enabled, this Servo should not move.
     * @return true if enabled
     */
    public Boolean getEnabled();
    /**
     * Sets the enabled value for this Servo
     * @param enabled enabled value
     */
    public void setEnabled(Boolean enabled);
    /**
     * Returns the Servo name.
     * @return Servo name
     */
    public String getName();
    /**
     * Returns the Servo minimum position.
     * @return Servo minimum position
     */
    public int getMinPosition();
    /**
     * Returns the Servo maximum position.
     * @return Servo maximum position
     */
    public int getMaxPosition();
    /**
     * Returns the Servo default position.
     * @return Servo default position
     */
    public NormalizedDouble getDefaultPosition();
    /**
     * Returns the NormalizableRange describing the range of motion.
     * @return NormalizableRange describing the range of motion
     */
    public NormalizableRange<Double> getPositionRange();
}
