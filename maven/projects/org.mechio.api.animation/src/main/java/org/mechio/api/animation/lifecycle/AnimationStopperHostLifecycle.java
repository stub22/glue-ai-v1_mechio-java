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
package org.mechio.api.animation.lifecycle;

import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.mechio.api.animation.stopper.AnimationStopper;
import org.mechio.api.animation.stopper.AnimationStopperHost;
import org.mechio.api.animation.stopper.OSGIAnimationStopper;
import org.mechio.api.animation.stopper.OSGIAnimationStopperHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * {@link ServiceLifecycle} for {@link OSGIAnimationStopperHost}
 *
 * @author Ben Jenkins <benjenkinsv95@gmail.com>
 */
public class AnimationStopperHostLifecycle implements ServiceLifecycle<OSGIAnimationStopperHost> {

	private static final Logger theLogger = LoggerFactory.getLogger(AnimationStopperHostLifecycle.class);

	private final static String theAnimationStopper = AnimationStopper.PROPERTY_ID;
	public final static ServiceDependency theAnimationStopperDep = new ServiceDependency(theAnimationStopper,
			OSGIAnimationStopper.class.getName(),
			ServiceDependency.Cardinality.MANDATORY_UNARY,
			ServiceDependency.UpdateStrategy.STATIC,
			null);

	private final static String theMessageReceiver = "animationStopperMessageReceiver";
	public final static ServiceDependency theMessageReceiverDep = new ServiceDependency(theMessageReceiver,
			MessageAsyncReceiver.class.getName(),
			ServiceDependency.Cardinality.MANDATORY_UNARY,
			ServiceDependency.UpdateStrategy.STATIC,
			null);

	private final static List<ServiceDependency> theDependencies = Arrays.asList(theAnimationStopperDep, theMessageReceiverDep);

	@Override
	public List<ServiceDependency> getDependencySpecs() {
		return theDependencies;
	}

	@Override
	public OSGIAnimationStopperHost createService(final Map<String, Object> dependencyMap) {
		final AnimationStopper animationStopper = (AnimationStopper) dependencyMap.get(theAnimationStopper);
		final MessageAsyncReceiver<ServiceCommand> messageReceiver = (MessageAsyncReceiver) dependencyMap.get(theMessageReceiver);

		final OSGIAnimationStopperHost animationStopperHost = OSGIAnimationStopperHost.create(animationStopper, messageReceiver);
		animationStopperHost.start();

		theLogger.info("Creating and starting {}.", animationStopperHost);
		return animationStopperHost;
	}


	/**
	 * Unused since all dependencies are
	 * {@link org.jflux.api.service.ServiceDependency.UpdateStrategy#STATIC}
	 */
	@Override
	public OSGIAnimationStopperHost handleDependencyChange(
			final OSGIAnimationStopperHost service, final String changeType, final String dependencyName,
			final Object dependency, final Map<String, Object> availableDependencies) {
		throw new UnsupportedOperationException("All dependencies have static update strategies.");
	}

	@Override
	public void disposeService(final OSGIAnimationStopperHost service,
							   final Map<String, Object> availableDependencies) {
		theLogger.info("Stopping {}.", service);
		service.stop();
	}

	@Override
	public String[] getServiceClassNames() {
		return new String[]{AnimationStopperHost.class.getName(), OSGIAnimationStopperHost.class.getName()};
	}
}
