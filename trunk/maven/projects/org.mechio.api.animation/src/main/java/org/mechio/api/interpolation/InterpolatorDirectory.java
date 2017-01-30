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

package org.mechio.api.interpolation;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.impl.services.rk.osgi.ClassTracker;
import org.mechio.api.interpolation.linear.LinearInterpolatorFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * A class for tracking various InterpolatorFactories.  Capable of tracking
 * InterpolatorFactories with or without OSGi support.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class InterpolatorDirectory {
	private static final Logger theLogger = LoggerFactory.getLogger(InterpolatorDirectory.class);
	/**
	 * Interpolator VersionProperty XML type attribute value.
	 */
	public final static String INTERPOLATOR_VERSION = "InterpolatorVersion";
	private final static String theFilterTemplate = String.format("(&(%s=%s)(%s=%s))",
			Constants.OBJECTCLASS, InterpolatorFactory.class.getName(),
			INTERPOLATOR_VERSION, "%s");
	private static InterpolatorDirectory theInstance;
	private static List<InterpolatorFactory> theFactories;
	private static Map<VersionProperty, InterpolatorFactory> theFactoryMap;

	private BundleContext myContext = null;
	private ClassTracker<InterpolatorFactory> myFactoryTracker;
	private List<InterpolatorFactory> myFactories;
	private Map<VersionProperty, ServiceTracker> myOSGiFactories;
	private InterpolatorFactory myDefaultFactory = null;
	private boolean myIsUsingOSGi;

	/**
	 * Retrieves the InterpolatorDirectory instance.
	 *
	 * @return InterpolatorDirectory instance
	 */
	public static InterpolatorDirectory instance() {
		if (theInstance == null) {
			theInstance = new InterpolatorDirectory(new LinearInterpolatorFactory());
		}
		return theInstance;
	}

	private static Map<VersionProperty, InterpolatorFactory> getFactoryMap() {
		if (theFactoryMap == null) {
			theFactoryMap = new HashMap();
		}
		return theFactoryMap;
	}

	private static List<InterpolatorFactory> getAllFactories() {
		if (theFactories == null) {
			theFactories = new ArrayList();
		}
		return theFactories;
	}

	static void registerFactory(InterpolatorFactory fact) {
		getFactoryMap().put(fact.getVersion(), fact);
		getAllFactories().add(fact);
	}

	/**
	 * Registers an InterpolatorFactory to the directory using OSGi.
	 *
	 * @param context BundleContext to register with
	 * @param fact    InterpolatorFactory to register
	 * @return ServiceRegistration of the InterpolatorFactory
	 */
	public static ServiceRegistration registerFactory(BundleContext context, InterpolatorFactory fact) {
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(INTERPOLATOR_VERSION, fact.getVersion().toString());
		return context.registerService(InterpolatorFactory.class.getName(), fact, props);
	}

	private InterpolatorDirectory(InterpolatorFactory defaultFactory) {
		this();
		myDefaultFactory = defaultFactory;
	}

	/**
	 * Creates an empty InterpolatoryDirectory.  By default, does not utilize
	 * OSGi.
	 */
	public InterpolatorDirectory() {
		myOSGiFactories = new HashMap();
		myFactoryTracker = new ClassTracker(InterpolatorFactory.class.getName());
		myFactories = new ArrayList();
		myIsUsingOSGi = false;
	}

	/**
	 * Returns the InterpolatorFactory for creating default Interpolators.
	 *
	 * @return InterpolatorFactory for creating default Interpolators
	 */
	public InterpolatorFactory getDefaultFactory() {
		return myDefaultFactory;
	}

	/**
	 * Returns an InterpolatorFactory for the given VersionProperty.  If an
	 * InterpolatorFactory cannot be found for the VersionProperty, the default
	 * factory is returned.
	 *
	 * @param version Interpolator Version of the Factory to retrieve
	 * @return InterpolatorFactory for the given VersionProperty.  If an InterpolatorFactory cannot
	 * be found for the VersionProperty, the default factory is returned
	 */
	public InterpolatorFactory getFactory(VersionProperty version) {
		if (version == null) {
			return myDefaultFactory;
		}
		InterpolatorFactory factory = null;
		if (myIsUsingOSGi) {
			factory = getFactory(myContext, version);
		} else if (getFactoryMap().containsKey(version)) {
			factory = getFactoryMap().get(version);
		}
		if (factory == null && myDefaultFactory != null) {
			theLogger.warn("Unable to find InterpolatorFactory with Version={}. " +
					"Using default Factory.", version);
			factory = myDefaultFactory;
		}
		return factory;
	}

	/**
	 * Returns a List of all available InterpolatorFactories.
	 *
	 * @return List of all available InterpolatorFactories
	 */
	public List<InterpolatorFactory> buildFactoryList() {
		List<InterpolatorFactory> factories = null;
		if (myIsUsingOSGi) {
			factories = myFactoryTracker.getServices();
		} else {
			factories = getAllFactories();
		}
		myFactories.clear();
		if (factories != null && !factories.isEmpty()) {
			for (InterpolatorFactory factory : factories) {
				myFactories.add(factory);
			}
		}
		if (myFactories.isEmpty() && myDefaultFactory != null) {
			myFactories.add(myDefaultFactory);
		}
		return myFactories;
	}

	/**
	 * Sets the default InterpolatorFactory.
	 *
	 * @param factory InterpolatorFactory to set as default
	 */
	public void setDefault(InterpolatorFactory factory) {
		if (factory != null) {
			myDefaultFactory = factory;
		}
	}

	/**
	 * Sets the directory's BundleContext.
	 *
	 * @param context BundleContext to set
	 */
	public void setContext(BundleContext context) {
		myIsUsingOSGi = context != null;
		myContext = context;
		if (myIsUsingOSGi) {
			myFactoryTracker.setContext(myContext);
			myFactoryTracker.init();
		}
	}

	/**
	 * Sets if the InterpolatorDirectory should use OSGi or not.
	 *
	 * @param val if true, this InterpolatorDirectory will use OSGi
	 */
	public void useOSGi(boolean val) {
		myIsUsingOSGi = val;
	}

	private InterpolatorFactory getFactory(BundleContext context, VersionProperty version) {
		if (context == null) {
			return null;
		}
		InterpolatorFactory factory = buildTrackedFactory(myOSGiFactories.get(version));
		if (factory != null) {
			return factory;
		}
		String filterStr = String.format(theFilterTemplate, version.toString());
		try {
			Filter filter = context.createFilter(filterStr);
			ServiceTracker tracker = new ServiceTracker(context, filter, null);
			tracker.open();
			myOSGiFactories.put(version, tracker);
			return buildTrackedFactory(tracker);
		} catch (InvalidSyntaxException ex) {
			theLogger.warn("Could not get ServiceTracker for InterpolatorFactory.  Bad version filter ({}).", filterStr, ex);
		} catch (IllegalStateException ex) {
			theLogger.warn("Unable to create ServiceTracker.", ex);
		}
		return null;
	}

	private static InterpolatorFactory buildTrackedFactory(ServiceTracker tracker) {
		if (tracker == null) {
			return null;
		}
		Object obj = tracker.getService();
		if (obj == null || !(obj instanceof InterpolatorFactory)) {
			return null;
		}
		return (InterpolatorFactory) obj;
	}
}
