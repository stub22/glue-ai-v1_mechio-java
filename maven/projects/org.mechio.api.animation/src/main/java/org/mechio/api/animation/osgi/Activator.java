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

package org.mechio.api.animation.osgi;

import org.mechio.api.interpolation.InterpolatorDirectory;
import org.mechio.api.interpolation.bezier.BezierInterpolatorFactory;
import org.mechio.api.interpolation.cspline.CSplineInterpolatorFactory;
import org.mechio.api.interpolation.linear.LinearInterpolatorFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class Activator implements BundleActivator {
	private static final Logger theLogger = LoggerFactory.getLogger(Activator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		theLogger.info("AnimationControl Activation Begin.");
		InterpolatorDirectory.instance().setContext(context);
		InterpolatorDirectory.registerFactory(context, new CSplineInterpolatorFactory());
		theLogger.info("CSplineInterpolatorFactory Service Registered Successfully.");
		InterpolatorDirectory.registerFactory(context, new LinearInterpolatorFactory());
		theLogger.info("LinearInterpolatorFactory Service Registered Successfully.");
		InterpolatorDirectory.registerFactory(context, new BezierInterpolatorFactory());
		theLogger.info("BezierInterpolatorFactory Service Registered Successfully.");

		theLogger.info("AnimationControl Activation Complete.");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
