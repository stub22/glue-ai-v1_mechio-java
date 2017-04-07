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
package org.mechio.impl.animation.cleanup;

import org.jflux.api.core.Listener;
import org.jflux.api.messaging.rk.services.ServiceCommand;

/**
 * An AnimationCleanupHost listens to ServiceCommands and stop's animations when it receives one.
 *
 * @author ben
 * @since 3/29/2017.
 */
public interface AnimationCleanupHost extends Listener<ServiceCommand> {
	/**
	 * Used for registering an AnimationCleanupHost to OSGI.
	 */
	String PROPERTY_ID = "animationStopperHostId";

	@Override
	void handleEvent(ServiceCommand serviceCommand);


}
