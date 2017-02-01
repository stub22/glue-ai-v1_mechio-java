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

package org.mechio.api.animation;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.playable.Playable;
import org.jflux.api.common.rk.services.addon.ServiceAddOn;
import org.mechio.api.animation.compiled.CompiledMap;
import org.mechio.api.animation.compiled.CompiledPath;
import org.mechio.api.animation.editor.features.SyncPointGroupConfig.SyncGroupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * An Animation holds a map of servo IDs and Channel.
 * This also stores the JointParameters corresponding to each Channel.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class Animation {
	private static final Logger theLogger = LoggerFactory.getLogger(Animation.class);
	private List<Channel> myChannels;
	private Map<Integer, Channel> myChannelMap;
	private VersionProperty myVersion;
	private Long myStartTime;
	private Long myStopTime;
	private List<ServiceAddOn<Playable>> myAddOns;
	private List<SyncGroupConfig> mySyncGroupConfigs;

	/**
	 * Creates an empty Animation.
	 */
	public Animation() {
		this(new VersionProperty("New Animation", "1.0"));
	}

	/**
	 * Creates an Animation with the given VersionProperty.
	 *
	 * @param version the Animation's Version
	 */
	public Animation(VersionProperty version) {
		myChannels = new ArrayList();
		myChannelMap = new HashMap();
		myAddOns = new ArrayList<>();
		myVersion = version;
	}

	/**
	 * Returns The Animation's Version.
	 *
	 * @return the Animation's Version
	 */
	public VersionProperty getVersion() {
		return myVersion;
	}

	/**
	 * Set the Animation's Version with the given name and version number.
	 *
	 * @param name          the Animations new name
	 * @param versionNumber the Animations new version number
	 */
	public void setVersion(String name, String versionNumber) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Unable to set empty Version Name.");
		}
		myVersion = new VersionProperty(name, versionNumber);
	}

	/**
	 * Adds a given Channel for the given servo parameters.
	 *
	 * @param channel Channel to add
	 * @throws IllegalArgumentException if servo id has already been added
	 * @throws NullPointerException     if channel is null
	 */
	public void addChannel(Channel channel) throws IllegalArgumentException {
		if (channel == null) {
			throw new NullPointerException("Unable to add null channel.");
		}
		int id = channel.getId();
		if (myChannelMap.containsKey(id)) {
			throw new IllegalArgumentException("Unable to add channel.  Channel for servo " + id + " already exists.");
		}
		myChannelMap.put(id, channel);
		myChannels.add(channel);
	}

	/**
	 * Adds a list of Channels.
	 *
	 * @param channels Channels to add
	 * @throws NullPointerException if channels is null
	 */
	public void addChannels(List<Channel> channels) throws NullPointerException {
		if (channels == null) {
			throw new NullPointerException("Unable to add null channels.");
		}
		for (Channel channel : channels) {
			try {
				addChannel(channel);
			} catch (IllegalArgumentException ex) {
				theLogger.warn(ex.getMessage(), ex);
			}
		}
	}

	/**
	 * Sets the start time
	 *
	 * @param time start time
	 */
	public void setStartTime(Long time) {
		if (time < 0) {
			throw new IllegalArgumentException("start time must be positive");
		}
		myStartTime = time;
		setChannelStartTime(time);
	}

	private void setChannelStartTime(Long time) {
		for (Channel c : myChannels) {
			c.setStartTime(time);
		}
	}

	/**
	 * Returns the start time
	 *
	 * @return start time
	 */
	public Long getStartTime() {
		return myStartTime;
	}

	/**
	 * Sets the stop time
	 *
	 * @param time stop time
	 */
	public void setStopTime(Long time) {
		if (time < 0) {
			throw new IllegalArgumentException("start time must be positive");
		}
		myStopTime = time;
		setChannelStopTime(time);
	}

	private void setChannelStopTime(Long time) {
		for (Channel c : myChannels) {
			c.setStopTime(time);
		}
	}

	/**
	 * Returns the stop time
	 *
	 * @return stop time
	 */
	public Long getStopTime() {
		return myStopTime;
	}

	/**
	 * Adds a given Channel for the given servo parameters, and orders it by the given index.
	 *
	 * @param channel Channel to add
	 * @param i       the index to insert the channel
	 * @throws IllegalArgumentException if servo id has already been added
	 * @throws NullPointerException     if channel is null
	 */
	public void insertChannel(int i, Channel channel) throws IllegalArgumentException {
		if (channel == null) {
			throw new NullPointerException("Unable to add null channel.");
		}
		int id = channel.getId();
		if (myChannelMap.containsKey(id)) {
			throw new IllegalArgumentException("Unable to add channel.  Channel for servo " + id + " already exists.");
		}
		myChannelMap.put(id, channel);
		myChannels.add(i, channel);
	}

	/**
	 * Returns true if there exists a channel for the given id.
	 *
	 * @param id servo id
	 * @return true if there exists a channel for the given id.
	 */
	public boolean containsLogicalId(int id) {
		return myChannelMap.containsKey(id);
	}

	/**
	 * Returns the Channel for id, ordered by when the Channels were added.
	 *
	 * @param id for Channel
	 * @return Channel for given id
	 */
	public Channel getChannel(int id) {
		return myChannels.get(id);
	}

	/**
	 * Returns the Channel for the given id.
	 *
	 * @param id Servo id for Channel
	 * @return Channel for given id, null if none exists
	 */
	public Channel getChannelByLogicalId(int id) {
		return myChannelMap.get(id);
	}

	/**
	 * Returns a set of entries of servo ids and Channels
	 *
	 * @return a set of entries of servo ids and Channels
	 */
	public Set<Entry<Integer, Channel>> getEntrySet() {
		return myChannelMap.entrySet();
	}

	/**
	 * Returns a set of entries of servo ids and Channels
	 *
	 * @return a set of entries of servo ids and Channels
	 */
	public List<Channel> getChannels() {
		return myChannels;
	}

	/**
	 * Removes the i<sup>th</sup> Channel, ordered by when the channels were added.
	 *
	 * @param i the index of the channel to remove
	 * @return the removed Channel, null if i is out of bounds
	 */
	public Channel removeChannelByListOrder(int i) {
		if (i < 0 || i > myChannels.size()) {
			return null;
		}
		Channel channel = myChannels.remove(i);
		int logicalId = channel.getId();
		myChannelMap.remove(logicalId);
		return channel;
	}

	/**
	 * Removes the Channel with the given logical id.
	 *
	 * @param logicalId the logicalId of the Channel to remove
	 * @return the removed Channel, null if the logicalId is not found.
	 */
	public Channel removeChannelByLogicalId(int logicalId) {
		if (!myChannelMap.containsKey(logicalId)) {
			return null;
		}
		Channel channel = myChannelMap.remove(logicalId);
		myChannels.remove(channel);
		return channel;
	}

	/**
	 * Creates a CompiledMap from contained Channels' CompiledPaths.
	 *
	 * @param stepLength milliseconds between positions
	 * @return CompiledMap from contained Channels' CompiledPaths
	 */
	public CompiledMap getCompiledMap(long stepLength) {
		long start = myStartTime == null ? -1 : myStartTime;
		long stop = myStopTime == null ? -1 : myStopTime;
		return compileMap(start, stop, stepLength);
	}

	/**
	 * Creates a composite CompiledPath from all MotionPaths for given times.
	 * Start and end constraints ignored only when (start == -1 && end == -1).
	 *
	 * @param start      path start time
	 * @param end        path end time
	 * @param stepLength milliseconds between positions
	 * @return combined path from MotionPaths for given times
	 */
	public CompiledMap compileMap(long start, long end, long stepLength) {
		Map<Integer, CompiledPath> paths = new HashMap();
		for (Entry<Integer, Channel> e : myChannelMap.entrySet()) {
			CompiledPath cp = e.getValue().compilePath(start, end, stepLength);
			if (cp != null && !cp.isEmpty()) {
				paths.put(e.getKey(), cp);
			}
		}
		CompiledMap cm = new CompiledMap(stepLength, start, end);
		cm.putAll(paths);
		return cm;
	}

	public long getLength() {
		double max = 0;
		for (Channel c : myChannels) {
			for (MotionPath m : c.getMotionPaths()) {
				if (m.myXVals.isEmpty()) {
					continue;
				}
				double end = m.myXVals.get(m.myXVals.size() - 1);
				if (end > max) {
					max = end;
				}
			}
		}
		if (myStopTime == null || myStopTime < 0 || myStopTime > max) {
			return (long) max;
		} else {
			return myStopTime;
		}
	}

	public void setSyncGroupConfigs(List<SyncGroupConfig> configs) {
		mySyncGroupConfigs = configs;
	}

	public List<SyncGroupConfig> getSyncGroupConfigs() {
		return mySyncGroupConfigs;
	}

	/**
	 * Returns a deep copy of the Animation.
	 *
	 * @return a deep copy of the Animation.
	 */
	@Override
	public Animation clone() {
		Animation a = new Animation();
		a.setVersion(myVersion.getName(), myVersion.getNumber());
		for (Channel c : myChannels) {
			a.addChannel(c.clone());
		}
		for (ServiceAddOn<Playable> addon : myAddOns) {
			a.myAddOns.add(addon);
		}
		if (mySyncGroupConfigs != null) {
			a.mySyncGroupConfigs =
					new ArrayList<>(mySyncGroupConfigs);
		}
		return a;
	}

	public void addAddOn(ServiceAddOn<Playable> addOn) {
		if (addOn == null) {
			throw new NullPointerException();
		}
		if (myAddOns.contains(addOn)) {
			return;
		}
		myAddOns.add(addOn);
	}

	public void removeAddOn(ServiceAddOn<Playable> addOn) {
		myAddOns.remove(addOn);
	}

	public List<ServiceAddOn<Playable>> getAddOns() {
		return myAddOns;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Animation other = (Animation) obj;
		if (this.myChannelMap != other.myChannelMap && (this.myChannelMap == null || !this.myChannelMap.equals(other.myChannelMap))) {
			return false;
		}
		if (this.myVersion != other.myVersion && (this.myVersion == null || !this.myVersion.equals(other.myVersion))) {
			return false;
		}
		if (this.myStartTime != other.myStartTime && (this.myStartTime == null || !this.myStartTime.equals(other.myStartTime))) {
			return false;
		}
		if (this.myStopTime != other.myStopTime && (this.myStopTime == null || !this.myStopTime.equals(other.myStopTime))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + (this.myChannelMap != null ? this.myChannelMap.hashCode() : 0);
		hash = 23 * hash + (this.myVersion != null ? this.myVersion.hashCode() : 0);
		hash = 23 * hash + (this.myStartTime != null ? this.myStartTime.hashCode() : 0);
		hash = 23 * hash + (this.myStopTime != null ? this.myStopTime.hashCode() : 0);
		return hash;
	}
}
