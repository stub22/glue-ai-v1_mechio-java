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

import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.osgi.framework.BundleContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link ServiceLifecycle} for {@link OSGIAnimationStopper}
 *
 * @author Ben Jenkins <benjenkinsv95@gmail.com>
 */
public class AnimationStopperLifecycle implements ServiceLifecycle<AnimationStopper> {

	private final static String theBundleContext = "animationStopperBundleContextId";
	public final static ServiceDependency theBundleContextDependency = new ServiceDependency(theBundleContext,
			BundleContext.class.getName(),
			ServiceDependency.Cardinality.MANDATORY_UNARY,
			ServiceDependency.UpdateStrategy.STATIC,
			null);

	private final static List<ServiceDependency> theDependencies = Collections.singletonList(theBundleContextDependency);

	@Override
	public List<ServiceDependency> getDependencySpecs() {
		return theDependencies;
	}

	@Override
	public AnimationStopper createService(final Map<String, Object> dependencyMap) {
		final BundleContext bundleContext = (BundleContext) dependencyMap.get(theBundleContext);
		return OSGIAnimationStopper.create(bundleContext);
	}

	/**
	 * Unused since all dependencies are
	 * {@link org.jflux.api.service.ServiceDependency.UpdateStrategy#STATIC}
	 */
	@Override
	public AnimationStopper handleDependencyChange(
			final AnimationStopper service, final String changeType, final String dependencyName,
			final Object dependency, final Map<String, Object> availableDependencies) {
		throw new UnsupportedOperationException("All dependencies have static update strategies.");
	}

	@Override
	public void disposeService(final AnimationStopper service,
							   final Map<String, Object> availableDependencies) {
	}

	@Override
	public String[] getServiceClassNames() {
		return new String[]{AnimationStopper.class.getName(), OSGIAnimationStopper.class.getName()};
	}
}
