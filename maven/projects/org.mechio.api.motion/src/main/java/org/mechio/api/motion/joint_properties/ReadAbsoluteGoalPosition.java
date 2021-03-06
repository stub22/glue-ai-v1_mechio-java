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

import org.mechio.api.motion.JointProperty;

/**
 *
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
public abstract class ReadAbsoluteGoalPosition
    extends JointProperty.ReadOnly<Double> {
    /**
     * JointProperty Property Name
     */
    public final static String PROPERTY_NAME = "absoluteGoalPosition";
    /**
     * Name used for display purposes
     */
    public final static String DISPLAY_NAME = "Absolute Goal Position";

    @Override
    public String getPropertyName() {
        return PROPERTY_NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public Class<Double> getPropertyClass() {
        return Double.class;
    }
}
