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

import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation providing much of the functionality need to implement
 * various Joints.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class AbstractJoint extends PropertyChangeNotifier implements
		Joint, PropertyChangeListener {
	private static final Logger theLogger = LoggerFactory.getLogger(AbstractJoint.class);

	private Joint.Id myJointId;

	/**
	 * JointProperties available from this Joint
	 */
	protected Map<String, JointProperty> myProperties;

	/**
	 * Creates a new AbstractJoint with the given Id.
	 *
	 * @param id Joint.Id of the new Joint
	 */
	public AbstractJoint(Joint.Id id) {
		if (id == null) {
			throw new NullPointerException();
		}
		myJointId = id;
		myProperties = new HashMap<>();
	}

	@Override
	public Joint.Id getId() {
		return myJointId;
	}

	/**
	 * Adds a JointProperty to the Joint
	 *
	 * @param prop JointProperty to add
	 */
	protected void addProperty(JointProperty prop) {
		if (myProperties == null) {
			myProperties = new HashMap<>();
		}
		String name = prop.getPropertyName();
		if (myProperties.containsKey(name)) {
			return;
		}
		prop.addPropertyChangeListener(this);
		myProperties.put(name, prop);
	}

	/**
	 * Removes a JointProperty from the Joint
	 *
	 * @param prop JointProperty to remove
	 */
	protected void removeProperty(JointProperty prop) {
		if (myProperties == null ||
				prop == null || prop.getPropertyName() == null) {
			return;
		}
		myProperties.remove(prop.getPropertyName());
	}

	@Override
	public <T> JointProperty<T> getProperty(String name, Class<T> propertyType) {
		if (myProperties == null) {
			return null;
		}
		JointProperty prop = myProperties.get(name);
		if (prop == null) {
			return null;
		}
		Class c = prop.getPropertyClass();
		if (!propertyType.isAssignableFrom(c)) {
			theLogger.warn("Found Joint property ({}) with bad type ({}).  "
							+ "Expected type ({}).",
					name, c, propertyType);
			return null;
		}
		return (JointProperty<T>) prop;
	}

	@Override
	public JointProperty getProperty(String name) {
		return getProperty(name, Object.class);
	}

	@Override
	public Collection<JointProperty> getProperties() {
		if (myProperties == null) {
			return Collections.EMPTY_LIST;
		}
		return myProperties.values();
	}

	/**
	 * Used to broadcast PropertyChangeEvents from internal sources such as
	 * JointProperties or, in the case of a ServoJoint, a Servo.
	 *
	 * @param pce internal PropertyChangeEvent to
	 */
	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		firePropertyChange(pce);
	}
}
