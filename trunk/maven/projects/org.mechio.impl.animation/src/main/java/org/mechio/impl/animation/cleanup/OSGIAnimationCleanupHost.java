/*
 *   Copyright 2014 by the MechIO Project. (www.mechio.org).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.mechio.impl.animation.cleanup;

import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.messaging.ServiceCommandHost;
import org.mechio.api.animation.player.AnimationPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;

/**
 * Listen for ServiceCommands to stop animations and stop the specified animations accordingly.
 *
 * @author Ben Jenkins <benjenkinsv95@gmail.com>
 */
public class OSGIAnimationCleanupHost extends ServiceCommandHost implements AnimationCleanupHost {

	private static final Logger theLogger = LoggerFactory.getLogger(OSGIAnimationCleanupHost.class);
	public static final String DESTINATION_NAME = "r25CleanupAnimationCommand";
	public static final String STOP_COMMAND = "stop";
	public static final String RESET_COMMAND = "reset";
	public static final String STOP_AND_RESET_COMMAND = "stop_and_reset";
	public static final String STOP_ALL_ARGUMENT = "all";
	private final AnimationStopper myAnimationStopper;
	private final AnimationPlayer myAnimationPlayer;
	private final Animation myDefaultPositionAnimation;

	public static OSGIAnimationCleanupHost create(final AnimationStopper animationStopper,
												  final AnimationPlayer animationPlayer,
												  final Animation defaultPositionAnimation,
												  final MessageAsyncReceiver<ServiceCommand> messageReceiver) {
		checkNotNull(messageReceiver);
		checkNotNull(animationStopper);
		checkNotNull(animationPlayer);
		checkNotNull(defaultPositionAnimation);
		return new OSGIAnimationCleanupHost(animationStopper, animationPlayer, defaultPositionAnimation, messageReceiver);
	}

	private OSGIAnimationCleanupHost(final AnimationStopper animationStopper,
									 final AnimationPlayer animationPlayer,
									 final Animation defaultPositionAnimation,
									 final MessageAsyncReceiver<ServiceCommand> messageReceiver) {
		super(messageReceiver);
		myAnimationStopper = animationStopper;
		myAnimationPlayer = animationPlayer;
		myDefaultPositionAnimation = defaultPositionAnimation;
	}

	@Override
	public void handleEvent(final ServiceCommand serviceCommand) {

		theLogger.info("Received service command: " + serviceCommand);
		final String fullCommand = serviceCommand.getCommand();
		final List<String> commands = trimQuotationMarks(splitOnUnquotedSpaces(fullCommand));
		final String commandType = commands.get(0);

		switch (commandType) {
			case STOP_COMMAND:
				processStopCommand(commands);
				break;
			case RESET_COMMAND:
				if (commands.size() != 1) {
					theLogger.error("'{}' command does not expect any arguments, but got '{}'", RESET_COMMAND, commands);
					return;
				}

				myAnimationPlayer.playAnimation(myDefaultPositionAnimation);
				break;
			case STOP_AND_RESET_COMMAND:
				processStopCommand(commands);
				myAnimationPlayer.playAnimation(myDefaultPositionAnimation);
				break;
			default:
				theLogger.error("Unknown command: '{}'", fullCommand);
				break;
		}
	}

	private void processStopCommand(final List<String> commands) {
		final String stopArgument = commands.get(1);
		if (stopArgument.equals(STOP_ALL_ARGUMENT)) {
			myAnimationStopper.stopAllAnimations();

		} else if (!stopArgument.contains("=")) {
			theLogger.error("Unknown command '{}'. Expected the stop all command '{}' or key-value property pairs ex. 'robotId=RKR25 10000152'.", stopArgument, STOP_ALL_ARGUMENT);

		} else {
			final Map<String, String> animationProperties = parsePropertiesCommand(stopArgument);
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

	/**
	 * Given a phrase returns a list where the phrase split on spaces that aren't inside of
	 * quotation marks.
	 *
	 * Source: http://stackoverflow.com/a/7804472/3500171
	 *
	 * @param phrase the phrase that will be split
	 * @return a list where the phrase split on spaces that aren't inside of quotation marks.
	 */
	List<String> splitOnUnquotedSpaces(final String phrase) {

		final List<String> list = new ArrayList<>();
		final Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(phrase);
		while (m.find()) {
			list.add(m.group(1));
		}
		return list;
	}

	/**
	 * Given a list, trim the quotation marks from any element in it.
	 *
	 * Source: http://stackoverflow.com/a/2608682/3500171
	 *
	 * @param tokens a list of elements
	 * @return a list of elements with quotation marks trimmed from each element
	 * @throws NullPointerException if any tokens are null
	 */
	List<String> trimQuotationMarks(final List<String> tokens) {
		final List<String> trimmedTokens = new ArrayList<>();
		for (final String token : tokens) {
			checkNotNull(token, "%s cannot contain null tokens!", tokens);
			trimmedTokens.add(token.replaceAll("^\"|\"$", ""));
		}
		return trimmedTokens;
	}

	@Override
	public String toString() {
		return "OSGIAnimationCleanupHost{" +
				"animationStopper=" + myAnimationStopper +
				'}';
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final OSGIAnimationCleanupHost that = (OSGIAnimationCleanupHost) o;

		return myAnimationStopper.equals(that.myAnimationStopper);

	}

	@Override
	public int hashCode() {
		return myAnimationStopper.hashCode();
	}
}
