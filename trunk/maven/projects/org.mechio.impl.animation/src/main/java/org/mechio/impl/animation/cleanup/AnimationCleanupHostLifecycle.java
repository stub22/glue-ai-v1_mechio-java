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
import org.jflux.api.messaging.rk.qpid.QpidBrokerStarted;
import org.jflux.api.messaging.rk.qpid.QpidBrokerStatus;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.player.AnimationPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * {@link ServiceLifecycle} for {@link OSGIAnimationCleanupHost}
 *
 * @author Ben Jenkins <benjenkinsv95@gmail.com>
 */
public class AnimationCleanupHostLifecycle implements ServiceLifecycle<OSGIAnimationCleanupHost> {

	private static final Logger theLogger = LoggerFactory.getLogger(AnimationCleanupHostLifecycle.class);

	private final static String theAnimationStopper = AnimationStopper.PROPERTY_ID;
	public final static ServiceDependency theAnimationStopperDep = new ServiceDependency(theAnimationStopper,
			OSGIAnimationStopper.class.getName(),
			ServiceDependency.Cardinality.MANDATORY_UNARY,
			ServiceDependency.UpdateStrategy.STATIC,
			null);

	private final static String theAnimationPlayer = AnimationPlayer.PROP_PLAYER_ID;
	public final static ServiceDependency theAnimationPlayerDep = new ServiceDependency(theAnimationPlayer,
			AnimationPlayer.class.getName(),
			ServiceDependency.Cardinality.MANDATORY_UNARY,
			ServiceDependency.UpdateStrategy.STATIC,
			null);

	public final static String theDefaultAnimation = "defaultAnimationId";
	public final static ServiceDependency theDefaultAnimationDep = new ServiceDependency(theDefaultAnimation,
			Animation.class.getName(),
			ServiceDependency.Cardinality.MANDATORY_UNARY,
			ServiceDependency.UpdateStrategy.STATIC,
			null);

	private final static String theMessageReceiver = "animationCleanupMessageReceiver";
	public final static ServiceDependency theMessageReceiverDep = new ServiceDependency(theMessageReceiver,
			MessageAsyncReceiver.class.getName(),
			ServiceDependency.Cardinality.MANDATORY_UNARY,
			ServiceDependency.UpdateStrategy.STATIC,
			null);

	private final static String theQpidBrokerStarted = QpidBrokerStatus.PROPERTY_ID;
	public final static ServiceDependency theQpidBrokerStartedDep = new ServiceDependency(theQpidBrokerStarted,
			QpidBrokerStarted.class.getName(),
			ServiceDependency.Cardinality.MANDATORY_UNARY,
			ServiceDependency.UpdateStrategy.STATIC,
			null);

	private final static List<ServiceDependency> theDependencies =
			Arrays.asList(theAnimationStopperDep, theMessageReceiverDep, theQpidBrokerStartedDep,
					theAnimationPlayerDep, theDefaultAnimationDep);

	@Override
	public List<ServiceDependency> getDependencySpecs() {
		return theDependencies;
	}

	@Override
	public OSGIAnimationCleanupHost createService(final Map<String, Object> dependencyMap) {
		final AnimationStopper animationStopper = (AnimationStopper) dependencyMap.get(theAnimationStopper);
		final MessageAsyncReceiver<ServiceCommand> messageReceiver = (MessageAsyncReceiver) dependencyMap.get(theMessageReceiver);
		final QpidBrokerStarted qpidBrokerStarted = (QpidBrokerStarted) dependencyMap.get(theQpidBrokerStarted);
		final AnimationPlayer animationPlayer = (AnimationPlayer) dependencyMap.get(theAnimationPlayer);
		final Animation defaultAnimation = (Animation) dependencyMap.get(theDefaultAnimation);

		final OSGIAnimationCleanupHost animationCleanupHost = OSGIAnimationCleanupHost.create(
				animationStopper, animationPlayer, defaultAnimation, messageReceiver);
		animationCleanupHost.start();

		theLogger.info("Creating and starting {}. Qpid Broker Started: {}", animationCleanupHost, qpidBrokerStarted);
		return animationCleanupHost;
	}


	/**
	 * Unused since all dependencies are
	 * {@link org.jflux.api.service.ServiceDependency.UpdateStrategy#STATIC}
	 */
	@Override
	public OSGIAnimationCleanupHost handleDependencyChange(
			final OSGIAnimationCleanupHost service, final String changeType, final String dependencyName,
			final Object dependency, final Map<String, Object> availableDependencies) {
		throw new UnsupportedOperationException("All dependencies have static update strategies.");
	}

	@Override
	public void disposeService(final OSGIAnimationCleanupHost service,
							   final Map<String, Object> availableDependencies) {
		theLogger.info("Stopping {}.", service);
		service.stop();
	}

	@Override
	public String[] getServiceClassNames() {
		return new String[]{AnimationCleanupHost.class.getName(), OSGIAnimationCleanupHost.class.getName()};
	}
}
