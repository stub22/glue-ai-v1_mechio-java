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
package org.mechio.api.motion.joint_properties;

import org.jflux.api.common.rk.types.Voltage;
import org.mechio.api.motion.JointProperty;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class ReadVoltage extends 
        JointProperty.ReadOnly<Voltage> {
    /**
     * JointProperty Property Name
     */
    public final static String PROPERTY_NAME = "voltage";
    /**
     * Name used for display purposes
     */
    public final static String DISPLAY_NAME = "Voltage";

    @Override
    public String getPropertyName() {
        return PROPERTY_NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public Class<Voltage> getPropertyClass() {
        return Voltage.class;
    }
}
