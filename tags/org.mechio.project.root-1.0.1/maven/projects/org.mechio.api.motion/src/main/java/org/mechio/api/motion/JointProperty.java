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

import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.jflux.api.common.rk.property.PropertyChangeSource;

/**
 * Defines additional properties or capabilities of a given Joint, such as \
 * position feedback or moving speed.  
 * 
 * @param <T> Value Type returned by this JointProperty.
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface JointProperty<T> extends PropertyChangeSource{
    /**
     * Returns the name of the JointProperty.
     * @return name of the JointProperty
     */
    public String getPropertyName();
    /**
     * Returns the display name of the JointProperty.
     * @return display name of the JointProperty
     */
    public String getDisplayName();
    /**
     * Returns the JointProperty's value Type.
     * @return  JointProperty's value Type
     */
    public Class<T> getPropertyClass();
    /**
     * Returns true if getValue() is supported.
     * @return true if getValue() is supported
     */
    public boolean getWriteable();
    /**
     * Returns the value for this JointProperty, and caches the value.
     * @return the value for this JointProperty, and caches the value
     * @throws UnsupportedOperationException if getValue is not supported
     */
    public T getValue();
    /**
     * Sets the value of the JointProperty.
     * @param val the new value to set
     * @throws UnsupportedOperationException if setValue is not supported
     */
    public void setValue(T val);
    
    public NormalizableRange<T> getNormalizableRange();
    
    /**
     * Defines a JointProperty which can read but not write.
     * @param <T> Value Type returned by this JointProperty
     */
    public abstract class ReadOnly<T> extends 
            PropertyChangeNotifier implements JointProperty<T>{
        @Override
        public boolean getWriteable() {
            return false;
        }

        @Override
        public void setValue(T val) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
