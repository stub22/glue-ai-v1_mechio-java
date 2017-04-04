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

import org.mechio.api.animation.player.AnimationJob;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stop's animations in a {@link BundleContext}.
 *
 * @author Ben Jenkins <benjenkinsv95@gmail.com>
 */
public class OSGIAnimationStopper implements AnimationStopper {

	private static final Logger theLogger = LoggerFactory.getLogger(OSGIAnimationStopper.class);
	private final BundleContext myBundleContext;

	public static AnimationStopper create(final BundleContext bundleContext) {
		return new OSGIAnimationStopper(bundleContext);
	}

	OSGIAnimationStopper(final BundleContext bundleContext) {
		myBundleContext = checkNotNull(bundleContext);
	}

	private List<AnimationJob> getAnimationJobs(final List<ServiceReference> serviceReferences) {
		final ArrayList<AnimationJob> animationJobs = new ArrayList<>();

		for (final ServiceReference serviceReference : serviceReferences) {
			final Object service = myBundleContext.getService(serviceReference);

			if (service instanceof AnimationJob) {
				final AnimationJob animationJob = (AnimationJob) service;
				animationJobs.add(animationJob);
			} else {
				theLogger.error("{} is not an {}", serviceReference, AnimationJob.class.getName());
			}
		}

		return animationJobs;

	}


	private List<ServiceReference> getServiceReferences(final Map<String, String> animationProperties) {
		try {
			final String filter = LDAPFilters.createLDAPFilter(animationProperties, LDAPFilters.BooleanOperator.AND);
			final ServiceReference[] serviceReferences = myBundleContext.getServiceReferences(AnimationJob.class.getName(), filter);
			if (serviceReferences == null) {
				return Collections.EMPTY_LIST;
			}
			return Arrays.asList(serviceReferences);
		} catch (final InvalidSyntaxException ex) {
			theLogger.error("Couldnt get service references", ex);
			return Collections.EMPTY_LIST;
		}
	}

	private List<ServiceReference> getAllServiceReferences() {
		try {
			final ServiceReference[] serviceReferences = myBundleContext.getAllServiceReferences(AnimationJob.class.getName(), null);
			if (serviceReferences == null) {
				return Collections.EMPTY_LIST;
			}
			return Arrays.asList(serviceReferences);
		} catch (final InvalidSyntaxException ex) {
			theLogger.error("Couldnt get service references", ex);
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public void stopAllAnimations() {
		final List<ServiceReference> allServiceReferences = getAllServiceReferences();
		final List<AnimationJob> animationJobs = getAnimationJobs(allServiceReferences);
		stopAnimationJobs(animationJobs);
	}

	/**
	 * Stop all of the animations that match every property in {@code animationProperties}.
	 *
	 * @param animationProperties Properties to match an animation against. Ex.
	 *                            "robotId=RKR25&nbsp;10000152"
	 */
	@Override
	public void stopSpecificAnimations(final Map<String, String> animationProperties) {
		final List<ServiceReference> serviceReferences = getServiceReferences(animationProperties);
		final List<AnimationJob> animationJobs = getAnimationJobs(serviceReferences);
		stopAnimationJobs(animationJobs);
	}

	private void stopAnimationJobs(final List<AnimationJob> animationJobs) {
		if (animationJobs.isEmpty()) {
			theLogger.info("No animations to stop. Ending now.");
			return;
		}

		theLogger.info("Stopping all animations.");

		for (final AnimationJob animationJob : animationJobs) {
			final long timestamp = System.currentTimeMillis();

			final boolean successfulStop = animationJob.stop(timestamp);
			if (successfulStop) {
				theLogger.info("Stopping animation job {} succeeded.", animationJob);
			} else {
				theLogger.error("Stopping animation job {} failed!", animationJob);
			}
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final OSGIAnimationStopper that = (OSGIAnimationStopper) o;

		return myBundleContext.equals(that.myBundleContext);

	}

	@Override
	public int hashCode() {
		return myBundleContext.hashCode();
	}

	@Override
	public String toString() {
		return "OSGIAnimationStopper{" +
				"bundleContext=" + myBundleContext +
				'}';
	}
}
