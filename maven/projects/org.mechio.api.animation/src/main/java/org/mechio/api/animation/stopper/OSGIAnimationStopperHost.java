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

import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.mechio.api.animation.messaging.ServiceCommandHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;

/**
 * Listen for ServiceCommands to stop animations and stop the specified animations accordingly.
 *
 * @author Ben Jenkins <benjenkinsv95@gmail.com>
 */
public class OSGIAnimationStopperHost extends ServiceCommandHost implements AnimationStopperHost {

	private static final Logger theLogger = LoggerFactory.getLogger(OSGIAnimationStopperHost.class);
	public static final String DESTINATION_NAME = "r25StopAnimationCommand";
	public static final String STOP_ALL_COMMAND = "all";
	private final AnimationStopper myAnimationStopper;

	public static OSGIAnimationStopperHost create(final AnimationStopper myAnimationStopper, final MessageAsyncReceiver<ServiceCommand> messageReceiver) {
		checkNotNull(messageReceiver);
		checkNotNull(myAnimationStopper);
		return new OSGIAnimationStopperHost(myAnimationStopper, messageReceiver);
	}

	private OSGIAnimationStopperHost(final AnimationStopper animationStopper,
									 final MessageAsyncReceiver<ServiceCommand> messageReceiver) {
		super(messageReceiver);
		myAnimationStopper = animationStopper;
	}

	@Override
	public void handleEvent(final ServiceCommand serviceCommand) {

		theLogger.info("Received service command: " + serviceCommand);
		final String command = serviceCommand.getCommand();

		if (command.equals(STOP_ALL_COMMAND)) {
			myAnimationStopper.stopAllAnimations();

		} else if (!command.contains("=")) {
			theLogger.error("Unknown command '{}'. Expected the stop all command '{}' or key-value property pairs ex. 'robotId=RKR25 10000152'.", command, STOP_ALL_COMMAND);

		} else {
			final Map<String, String> animationProperties = parsePropertiesCommand(command);
			myAnimationStopper.stopSpecificAnimations(animationProperties);
		}

	}

	Map<String, String> parsePropertiesCommand(final String command) {
		// Regex: http://stackoverflow.com/a/7488676/3500171
		final List<String> animationProperties = Arrays.asList(command.split("\\s*,\\s*"));
		final TreeMap<String, String> propertiesMap = new TreeMap<>();

		for (final String keyValuePair : animationProperties) {
			verify(keyValuePair.contains("="), "Key Value pair '%s' doesnt have equal sign.", keyValuePair);

			final String[] keyValueArray = keyValuePair.split("=");
			propertiesMap.put(keyValueArray[0], keyValueArray[1]);
		}

		return propertiesMap;
	}

	@Override
	public String toString() {
		return "OSGIAnimationStopperHost{" +
				"animationStopper=" + myAnimationStopper +
				'}';
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final OSGIAnimationStopperHost that = (OSGIAnimationStopperHost) o;

		return myAnimationStopper.equals(that.myAnimationStopper);

	}

	@Override
	public int hashCode() {
		return myAnimationStopper.hashCode();
	}
}
