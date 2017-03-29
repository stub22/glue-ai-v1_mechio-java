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
import org.mechio.api.animation.player.AnimationPlayer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stop's animations in a BundleContext.
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

	@Override
	public void stopAll() {
		theLogger.info("Stopping all animations.");

		final ServiceReference[] serviceReferences;
		try {
			serviceReferences = myBundleContext.getAllServiceReferences(AnimationJob.class.getName(), null);
		} catch (final InvalidSyntaxException ex) {
			theLogger.error("Couldn't get service references", ex);
			return;
		}

		if (serviceReferences == null) {
			theLogger.info("No animations to stop. Ending now.");
			return;
		}

		for (final ServiceReference serviceReference : serviceReferences) {
			final AnimationJob animationJob = (AnimationJob) myBundleContext.getService(serviceReference);

			final long timestamp = System.currentTimeMillis();
			final boolean successfulStop = animationJob.stop(timestamp);
			if (!successfulStop) {
				theLogger.error("Stopping animation job {} was unsuccessful!", animationJob);
				continue;
			}

			final AnimationPlayer animationPlayer = animationJob.getSource();
			if (animationPlayer == null) {
				theLogger.error("Could not remove animation job {}, its source animation player was null.", animationJob);
				continue;
			}
			animationPlayer.removeAnimationJob(animationJob);
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
