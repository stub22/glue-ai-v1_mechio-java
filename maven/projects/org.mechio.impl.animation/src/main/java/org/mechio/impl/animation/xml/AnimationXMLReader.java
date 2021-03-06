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

package org.mechio.impl.animation.xml;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.playable.Playable;
import org.jflux.api.common.rk.services.Constants;
import org.jflux.api.common.rk.services.addon.AddOnUtils;
import org.jflux.api.common.rk.services.addon.ServiceAddOn;
import org.jflux.api.common.rk.services.addon.ServiceAddOnDriver;
import org.jflux.extern.utils.apache_commons_configuration.rk.XMLConfigUtils;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.Channel;
import org.mechio.api.animation.MotionPath;
import org.mechio.api.animation.editor.features.SyncPointGroupConfig.SyncGroupConfig;
import org.mechio.api.animation.xml.AnimationFileReader;
import org.mechio.api.interpolation.InterpolatorDirectory;
import org.mechio.api.interpolation.InterpolatorFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mechio.api.animation.xml.AnimationXML.ADDON;
import static org.mechio.api.animation.xml.AnimationXML.ADDONS;
import static org.mechio.api.animation.xml.AnimationXML.ADDON_FILE;
import static org.mechio.api.animation.xml.AnimationXML.ANIMATION_VERSION_TYPE;
import static org.mechio.api.animation.xml.AnimationXML.CHANNEL;
import static org.mechio.api.animation.xml.AnimationXML.CHANNELS;
import static org.mechio.api.animation.xml.AnimationXML.CHANNEL_ID;
import static org.mechio.api.animation.xml.AnimationXML.CHANNEL_NAME;
import static org.mechio.api.animation.xml.AnimationXML.CONTROL_POINT;
import static org.mechio.api.animation.xml.AnimationXML.CONTROL_POINTS;
import static org.mechio.api.animation.xml.AnimationXML.INTERPOLATION_VERSION_TYPE;
import static org.mechio.api.animation.xml.AnimationXML.MOTION_PATH;
import static org.mechio.api.animation.xml.AnimationXML.MOTION_PATHS;
import static org.mechio.api.animation.xml.AnimationXML.MOTION_PATH_NAME;
import static org.mechio.api.animation.xml.AnimationXML.POSITION;
import static org.mechio.api.animation.xml.AnimationXML.SYNC_POINT_GROUPS;
import static org.mechio.api.animation.xml.AnimationXML.TIME;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationXMLReader implements AnimationFileReader {
	private static final Logger theLogger = LoggerFactory.getLogger(AnimationXMLReader.class);

	/**
	 * Adding support for URLs so we can load animations from bundles.
	 */
	public Animation readAnimation(final URL url) throws Exception {
		return AnimationXMLReader.loadAnimation(url);
	}

	@Override
	public Animation readAnimation(final String path) throws Exception {
		return AnimationXMLReader.loadAnimation(path);
	}

	/**
	 * Adding support for URLs so we can load animations from bundles.
	 */
	static Animation loadAnimation(final URL url) throws ConfigurationException, Exception {
		try {
			final HierarchicalConfiguration config = new XMLConfiguration(url);
			return loadAnimation(url.getPath(), config);
		} catch (final ConfigurationException t) {
			// Since we are rethrowing the exception, we should not log its stack trace.
			theLogger.warn("Cannont open XML animation file at: {}", url);
			throw t;
		}
	}

	static Animation loadAnimation(final String path) throws ConfigurationException, Exception {
		try {
			final HierarchicalConfiguration config = new XMLConfiguration(path);
			return loadAnimation(path, config);
		} catch (final ConfigurationException t) {
			// Since we are rethrowing the exception, we should not log its stack trace.
			theLogger.warn("Cannont open XML animation file at: {}", path);
			throw t;
		}
	}

	static Animation loadAnimation(final String path, final HierarchicalConfiguration config) throws ConfigurationException, Exception {
		Animation anim = null;
		try {
			anim = readAnimation(config);
			return anim;
		} catch (final Exception t) {
			// Since we are rethrowing the exception, we should not log its stack trace.
			theLogger.error("Error reading animation at {}", path, t);
			throw t;
		}
	}

	public static Animation readAnimation(final HierarchicalConfiguration config) {
		final VersionProperty version =
				XMLConfigUtils.readVersion(config, ANIMATION_VERSION_TYPE);
		final Animation anim = new Animation(version);
		final HierarchicalConfiguration channelsConfig =
				config.configurationAt(CHANNELS);
		if (channelsConfig != null && !channelsConfig.isEmpty()) {
			anim.addChannels(readChannels(channelsConfig));
		}
		if (config.getKeys(SYNC_POINT_GROUPS).hasNext()) {
			final HierarchicalConfiguration groupsConfig =
					config.configurationAt(SYNC_POINT_GROUPS);
			final List<SyncGroupConfig> groups =
					SyncPointGroupXML.ApacheReader.readSyncPointGroupConfigs(
							groupsConfig);
			anim.setSyncGroupConfigs(groups);
		}
		if (config.getKeys(ADDONS).hasNext()) {
			final HierarchicalConfiguration addonsConfig =
					config.configurationAt(ADDONS);
			final List<ServiceAddOn<Playable>> addons = readAddOns(addonsConfig);
			for (final ServiceAddOn<Playable> addon : addons) {
				anim.addAddOn(addon);
			}
		}
/*        HierarchicalConfiguration channelsParamsConfig = config.configurationAt(CHANNELS_PARAMETERS);
		if(channelsParamsConfig != null && !channelsParamsConfig.isEmpty()){
            readChannels(channelsConfig);
        }*/
		return anim;
	}

	public static List<Channel> readChannels(final HierarchicalConfiguration config) {
		final List<HierarchicalConfiguration> nodes = (List<HierarchicalConfiguration>) config.configurationsAt(CHANNEL);
		if (nodes == null || nodes.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		final List<Channel> channels = new ArrayList(nodes.size());
		for (final HierarchicalConfiguration node : nodes) {
			final Channel channel = readChannel(node);
			if (channel != null) {
				channels.add(channel);
			}
		}
		return channels;
	}

	public static Channel readChannel(final HierarchicalConfiguration config) {
		Integer id = XMLConfigUtils.getIntegerAttribute(config, CHANNEL_ID, null);
		if (id == null) {
			theLogger.warn("Unable to find {} attribute for Channel, using -1.", CHANNEL_ID);
			id = -1;
		}
		final String name = XMLConfigUtils.getStringAttribute(config, CHANNEL_NAME, null);
		final Channel channel = new Channel(id, name);
		final HierarchicalConfiguration pathsConfig = config.configurationAt(MOTION_PATHS);
		if (pathsConfig == null || pathsConfig.isEmpty()) {
			return channel;
		}
		final List<MotionPath> paths = readMotionPaths(pathsConfig);
		channel.addPaths(paths);
		return channel;
	}

	public static List<MotionPath> readMotionPaths(final HierarchicalConfiguration config) {
		final List<HierarchicalConfiguration> nodes = (List<HierarchicalConfiguration>) config.configurationsAt(MOTION_PATH);
		if (nodes == null || nodes.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		final List<MotionPath> paths = new ArrayList(nodes.size());
		for (final HierarchicalConfiguration node : nodes) {
			paths.add(readMotionPath(node));
		}
		return paths;
	}

	public static MotionPath readMotionPath(final HierarchicalConfiguration config) {
		final String name = XMLConfigUtils.getStringAttribute(config, MOTION_PATH_NAME, null);
		final InterpolatorFactory factory = readInterpolatorVersion(config);
		final MotionPath path = new MotionPath(factory);
		path.setName(name);
		final HierarchicalConfiguration pointsConfig = config.configurationAt(CONTROL_POINTS);
		if (pointsConfig != null && !pointsConfig.isEmpty()) {
			path.addPoints(readControlPoints(pointsConfig));
		}
		return path;
	}

	public static InterpolatorFactory readInterpolatorVersion(final HierarchicalConfiguration config) {
		final VersionProperty version = XMLConfigUtils.readVersion(config, INTERPOLATION_VERSION_TYPE);
		return InterpolatorDirectory.instance().getFactory(version);
	}

	public static List<Point2D> readControlPoints(final HierarchicalConfiguration config) {
		final List<HierarchicalConfiguration> nodes = (List<HierarchicalConfiguration>) config.configurationsAt(CONTROL_POINT);
		if (nodes == null || nodes.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		final List<Point2D> points = new ArrayList(nodes.size());
		for (final HierarchicalConfiguration node : nodes) {
			final Point2D point = readControlPoint(node);
			if (point.getX() < 0.0 || point.getY() < 0.0 || point.getY() > 1.0) {
				continue;
			}
			points.add(point);
		}
		return points;
	}

	public static Point2D readControlPoint(final HierarchicalConfiguration config) {
		final double x = config.getDouble(TIME, -1);
		final double y = config.getDouble(POSITION, -1);
		return new Point2D.Double(x, y);
	}

	public static List<ServiceAddOn<Playable>> readAddOns(final HierarchicalConfiguration config) {
		if (config == null) {
			return Collections.EMPTY_LIST;
		}
		final List<HierarchicalConfiguration> nodes = (List<HierarchicalConfiguration>) config.configurationsAt(ADDON);
		if (nodes == null || nodes.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		final BundleContext context = OSGiUtils.getBundleContext(ServiceAddOnDriver.class);
		if (context == null) {
			return Collections.EMPTY_LIST;
		}
		final ServiceReference[] refs = AddOnUtils.getAddOnDriverReferences(context);
		final List<ServiceAddOnDriver<Playable>> drivers =
				getAddOnDrivers(context, refs);
		final List<ServiceAddOn<Playable>> addons = new ArrayList(nodes.size());
		for (final HierarchicalConfiguration node : nodes) {
			final ServiceAddOn<Playable> addon = readAddOn(node, drivers);
			if (addon != null) {
				addons.add(addon);
			}
		}
		return addons;
	}

	public static List<ServiceAddOnDriver<Playable>> getAddOnDrivers(
			final BundleContext context, final ServiceReference[] refs) {
		final List<ServiceAddOnDriver<Playable>> drivers = new ArrayList();
		final List<ServiceAddOnDriver> allDrivers = AddOnUtils.getAddOnDriverList(context, refs);
		for (final ServiceAddOnDriver driver : allDrivers) {
			final Class addonClass = driver.getServiceClass();
			if (Playable.class.isAssignableFrom(addonClass)) {
				drivers.add(driver);
			}
		}
		return drivers;
	}

	public static ServiceAddOn<Playable> readAddOn(final HierarchicalConfiguration config, final List<ServiceAddOnDriver<Playable>> drivers) {
		final Map<String, VersionProperty> vers = XMLConfigUtils.readVersions(config,
				Constants.SERVICE_VERSION, Constants.CONFIG_FORMAT_VERSION);
		final VersionProperty serviceVers =
				vers.get(Constants.SERVICE_VERSION);
		final VersionProperty configFormat =
				vers.get(Constants.CONFIG_FORMAT_VERSION);
		final String path = config.getString(ADDON_FILE);
		if (serviceVers == null || configFormat == null || path == null) {
			return null;
		}
		final File file = new File(path);
		if (!file.exists()) {
			theLogger.warn("Could not find file for AddOn.  " +
							"File Path: {}, Service Version: {}, Config Format: {}.",
					path, serviceVers.display(), configFormat.display());
			return null;
		}
		final ServiceAddOnDriver<Playable> driver =
				getDriver(serviceVers, configFormat, drivers);
		if (driver == null) {
			theLogger.warn("Could not find ServiceAddOnDriver.  " +
							"File Path: {}, Service Version: {}, Config Format: {}.",
					path, serviceVers.display(), configFormat.display());
			return null;
		}
		try {
			return driver.loadAddOn(file);
		} catch (final Exception ex) {
			theLogger.warn("There was an error loading an AddOn."
							+ "  File Path: {}, "
							+ "Service Version: {}, "
							+ "Config Format: {}.",
					path, serviceVers.display(), configFormat.display(), ex);
			return null;
		}
	}

	private static ServiceAddOnDriver<Playable> getDriver(
			final VersionProperty serviceVers, final VersionProperty configFormat,
			final List<ServiceAddOnDriver<Playable>> drivers) {
		for (final ServiceAddOnDriver<Playable> driver : drivers) {
			if (serviceVers.equals(driver.getServiceVersion()) &&
					configFormat.equals(driver.getConfigurationFormat())) {
				return driver;
			}
		}
		return null;
	}
}
