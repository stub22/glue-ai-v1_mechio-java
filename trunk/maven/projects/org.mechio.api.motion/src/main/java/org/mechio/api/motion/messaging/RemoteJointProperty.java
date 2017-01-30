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
package org.mechio.api.motion.messaging;

import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.mechio.api.motion.JointProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */

public class RemoteJointProperty<T>
		extends PropertyChangeNotifier implements JointProperty<T> {
	private static final Logger theLogger = LoggerFactory.getLogger(RemoteJointProperty.class);

	private String myPropertyName;
	private String myDisplayName;
	private Class<T> myPropertyClass;
	private T myPropertyValue;
	private NormalizableRange<T> myPropertyRange;

	public RemoteJointProperty(
			String propertyName, String displayName,
			Class<T> propertyClass, T val, NormalizableRange<T> range) {
		if (propertyName == null || displayName == null
				|| propertyClass == null || val == null || range == null) {
			throw new NullPointerException();
		}
		myPropertyName = propertyName;
		myDisplayName = displayName;
		myPropertyClass = propertyClass;
		myPropertyValue = val;
		myPropertyRange = range;
	}


	@Override
	public String getPropertyName() {
		return myPropertyName;
	}

	@Override
	public String getDisplayName() {
		return myDisplayName;
	}

	@Override
	public Class<T> getPropertyClass() {
		return myPropertyClass;
	}

	@Override
	public boolean getWriteable() {
		return true;
	}

	@Override
	public T getValue() {
		return myPropertyValue;
	}

	@Override
	public void setValue(T val) {
		myPropertyValue = val;
//        theLogger.info("Property [{}] set to: {}",
//                getDisplayName(), val.toString());
	}

	@Override
	public NormalizableRange<T> getNormalizableRange() {
		return myPropertyRange;
	}
}
