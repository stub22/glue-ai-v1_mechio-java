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
package org.mechio.api.animation.stopper;

/**
 * Implementors will offer the following methods to stop animations.
 *
 * @author ben
 * @since 3/28/2017.
 */
public interface AnimationStopper {

	/**
	 * Used for registering an AnimationStopper to OSGI.
	 */
	String PROPERTY_ID = "animationStopperId";

	void stopAll();
}
