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

package org.mechio.api.animation.utils;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.impl.services.rk.osgi.ClassTracker;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.library.AnimationLibrary;
import org.mechio.api.animation.library.AnimationLibraryLoader;
import org.mechio.api.animation.player.AnimationJob;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.xml.AnimationFileReader;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilenameFilter;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationUtils {
	private static final Logger theLogger = LoggerFactory.getLogger(AnimationUtils.class);
	private static ClassTracker<ChannelsParameterSource> theChannelParamTracker;
	private static ClassTracker<AnimationEditListener> theListeners;

	private static ClassTracker<ChannelsParameterSource> getChanParamTracker() {
		if (theChannelParamTracker != null) {
			return theChannelParamTracker;
		}
		BundleContext context = OSGiUtils.getBundleContext(ChannelsParameterSource.class);
		if (context == null) {
			return null;
		}
		theChannelParamTracker = new ClassTracker<>(
				context, ChannelsParameterSource.class.getName(), null, null);
		return theChannelParamTracker;
	}

	public static ChannelsParameterSource getChannelsParameterSource() {
		ClassTracker<ChannelsParameterSource> tracker = getChanParamTracker();
		return tracker == null ? null : tracker.getTopService();
	}

	public static void writePositions(long x, Map<Integer, Double> channelPositions) {
		List<AnimationEditListener> listeners = getListeners();
		if (listeners == null) {
			return;
		}
		for (AnimationEditListener listener : listeners) {
			listener.handlePositions(x, channelPositions);
		}
	}

	private static List<AnimationEditListener> getListeners() {
		ClassTracker<AnimationEditListener> tracker = getAELTracker();
		return tracker == null ? null : tracker.getServices();
	}

	private static ClassTracker<AnimationEditListener> getAELTracker() {
		if (theListeners != null) {
			return theListeners;
		}
		BundleContext context = OSGiUtils.getBundleContext(AnimationEditListener.class);
		if (context == null) {
			return null;
		}
		theListeners = new ClassTracker<>(
				context, AnimationEditListener.class.getName(), null, null);
		return theListeners;
	}

	/**
	 * Returns a ServiceReference for an AnimationPlayer matching the filter if
	 * it is not null.
	 *
	 * @param context BundleContext to use
	 * @param filter  option OSGi filter String
	 * @return ServiceReference for an AnimationPlayer matching the filter if it is not null
	 */
	public static ServiceReference getAnimationPlayerReference(
			BundleContext context, String filter) {
		if (context == null) {
			throw new NullPointerException();
		}
		ServiceReference[] refs = null;
		try {
			refs = context.getServiceReferences(AnimationPlayer.class.getName(), filter);
		} catch (InvalidSyntaxException ex) {
			theLogger.warn("Invalid filter: {}" +
					".  Could not fetch AnimationPlayer.", filter, ex);
		}
		if (refs == null) {
			theLogger.warn("Could not find AnimationPlayer");
			return null;
		}
		return refs[0];
	}

	/**
	 * Fetches an AnimationPlayer and plays the given Animation.  If a filter
	 * String is provided, it is used to match an AnimationPlayer.
	 *
	 * @param context BundleContext to use
	 * @param filter  option OSGi filter String
	 * @param anim    Animation to play
	 * @return AnimationJob created from playing the Animation, returns null if unsuccessful
	 */
	public static AnimationJob playAnimation(
			BundleContext context, String filter, Animation anim) {
		return _playAnimation(context, filter, anim, null, null);
	}

	/**
	 * Fetches an AnimationPlayer and plays the given Animation with the given
	 * start and stop time.  If a filter
	 * String is provided, it is used to match an AnimationPlayer.
	 *
	 * @param context   BundleContext to use
	 * @param filter    option OSGi filter String
	 * @param anim      Animation to play
	 * @param startTime Animation start time in milliseconds from the beginning of the animation
	 * @param stopTime  Animation stop time in milliseconds from the beginning of the animation
	 * @return AnimationJob created from playing the Animation, returns null if unsuccessful
	 */
	public static AnimationJob playAnimation(
			BundleContext context, String filter, Animation anim,
			long startTime, long stopTime) {
		return _playAnimation(context, filter, anim, startTime, stopTime);
	}

	private static AnimationJob _playAnimation(
			BundleContext context, String filter, Animation anim,
			Long startTime, Long stopTime) {
		if (context == null || anim == null) {
			throw new NullPointerException();
		}
		ServiceReference ref =
				AnimationUtils.getAnimationPlayerReference(context, filter);
		if (ref == null) {
			return null;
		}
		Object obj = context.getService(ref);
		if (obj == null || !(obj instanceof AnimationPlayer)) {
			context.ungetService(ref);
			return null;
		}
		AnimationPlayer player = (AnimationPlayer) obj;
		AnimationJob job = null;
		if (player != null) {
			if (startTime != null && stopTime != null) {
				job = player.playAnimation(anim, startTime, stopTime);
			} else {
				job = player.playAnimation(anim);
			}
		}
		context.ungetService(ref);
		return job;
	}

	public static AnimationLibrary loadAnimationLibrary(BundleContext context,
														String libraryId, String animationPath,
														boolean recursive, FilenameFilter filenameFilter) {
		if (context == null || animationPath == null) {
			throw new NullPointerException();
		}
		ServiceReference ref = context.getServiceReference(
				AnimationFileReader.class.getName());
		if (ref == null) {
			throw new NullPointerException();
		}
		Object obj = context.getService(ref);
		if (obj == null) {
			throw new NullPointerException();
		}
		AnimationFileReader reader = (AnimationFileReader) obj;
		AnimationLibraryLoader fact = new AnimationLibraryLoader();
		AnimationLibrary library =
				fact.loadAnimationFolder(libraryId, reader, animationPath, recursive);
		return library;
	}


	public static ServiceRegistration registerAnimationLibrary(BundleContext context, AnimationLibrary library, Properties serviceProps) {
		if (context == null) {
			throw new NullPointerException();
		}

		Dictionary<String, Object> props = new Hashtable<>();
		if (serviceProps != null) {
			for (Object prop : serviceProps.keySet()) {
				props.put(prop.toString(), serviceProps.get(prop));
			}
		}

		String filter = OSGiUtils.createServiceFilter(serviceProps);
		if (OSGiUtils.serviceExists(context, AnimationLibrary.class, filter)) {
			theLogger.warn("Unable to register AnimationLibrary.  " +
							"AnimationLibrary with filter: {}, already exists.",
					filter);
			return null;
		}
		ServiceRegistration reg =
				context.registerService(
						AnimationLibrary.class.getName(), library, props);
		return reg;
	}

	public static void addAnimationToLibrary(BundleContext context, Animation animation, String filter) {
		if (context == null || animation == null) {
			throw new NullPointerException();
		}
		ServiceReference[] refs;
		try {
			refs = context.getServiceReferences(
					AnimationLibrary.class.getName(), filter);
		} catch (InvalidSyntaxException ex) {
			theLogger.warn("Bad filter: {}", filter, ex);
			return;
		}
		if (refs == null || refs.length == 0) {
			theLogger.warn("Could not find AnimationLibrary matching: {}", filter);
			return;
		}
		ServiceReference ref = refs[0];
		Object obj = context.getService(ref);
		if (obj == null || !(obj instanceof AnimationLibrary)) {
			theLogger.warn("Error retrieving AnimationLibrary Service.");
			return;
		}
		AnimationLibrary lib = (AnimationLibrary) obj;
		lib.add(animation);
	}

	public static Animation fetchAnimation(BundleContext context, VersionProperty animVersion, String filter) {
		if (context == null || animVersion == null) {
			throw new NullPointerException();
		}
		ServiceReference[] refs;
		try {
			refs = context.getServiceReferences(
					AnimationLibrary.class.getName(), filter);
		} catch (InvalidSyntaxException ex) {
			theLogger.warn("Bad filter: {}", filter, ex);
			return null;
		}
		if (refs == null || refs.length == 0) {
			theLogger.warn("Could not find AnimationLibrary matching: {}", filter);
			return null;
		}
		ServiceReference ref = refs[0];
		Object obj = context.getService(ref);
		if (obj == null || !(obj instanceof AnimationLibrary)) {
			theLogger.warn("Error retrieving AnimationLibrary Service.");
			return null;
		}
		AnimationLibrary lib = (AnimationLibrary) obj;
		return lib.getAnimation(animVersion);
	}

	public static Animation fetchAnimation(BundleContext context, String animVersionName, String filter) {
		if (context == null || animVersionName == null) {
			throw new NullPointerException();
		}
		ServiceReference[] refs;
		try {
			refs = context.getServiceReferences(
					AnimationLibrary.class.getName(), filter);
		} catch (InvalidSyntaxException ex) {
			theLogger.warn("Bad filter: {}", filter, ex);
			return null;
		}
		if (refs == null || refs.length == 0) {
			theLogger.warn("Could not find AnimationLibrary matching: {}", filter);
			return null;
		}
		ServiceReference ref = refs[0];
		Object obj = context.getService(ref);
		if (obj == null || !(obj instanceof AnimationLibrary)) {
			theLogger.warn("Error retrieving AnimationLibrary Service.");
			return null;
		}
		AnimationLibrary lib = (AnimationLibrary) obj;
		List<VersionProperty> properties = lib.getAnimationVersions();
		for (VersionProperty property : properties) {
			if (animVersionName.equals(property.getName())) {
				return lib.getAnimation(property);
			}
		}
		return null;
	}
}
