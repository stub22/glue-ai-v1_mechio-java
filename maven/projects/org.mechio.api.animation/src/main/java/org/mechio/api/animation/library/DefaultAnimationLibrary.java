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
package org.mechio.api.animation.library;

import org.jflux.api.common.rk.config.VersionProperty;
import org.mechio.api.animation.Animation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DefaultAnimationLibrary implements AnimationLibrary {
	private static final Logger theLogger = LoggerFactory.getLogger(DefaultAnimationLibrary.class);
	private Map<VersionProperty, Animation> myAnimationMap;
	private List<VersionProperty> myAnimtionVersions;
	private String myLibraryId;

	public DefaultAnimationLibrary(String libId) {
		if (libId == null) {
			throw new NullPointerException();
		}
		myLibraryId = libId;
		myAnimationMap = new HashMap<>();
		myAnimtionVersions = new ArrayList<>();
	}

	@Override
	public List<VersionProperty> getAnimationVersions() {
		return myAnimtionVersions;
	}

	@Override
	public Animation getAnimation(VersionProperty version) {
		return myAnimationMap.get(version);
	}

	@Override
	public void add(Animation animation) {
		VersionProperty version = animation.getVersion();
		if (myAnimationMap.containsKey(version)) {
			theLogger.warn("Not adding Animation. "
							+ "Animation with given version ({}) already exists.",
					version);
			return;
		}
		myAnimationMap.put(version, animation);
		myAnimtionVersions.add(version);
	}

	@Override
	public void remove(Animation animation) {
		if (!myAnimationMap.containsValue(animation)) {
			theLogger.warn("Could not find given animation: {}", animation);
			return;
		}
		for (Entry<VersionProperty, Animation> e : myAnimationMap.entrySet()) {
			if (!animation.equals(e.getValue())) {
				continue;
			}
			VersionProperty prop = e.getKey();
			myAnimationMap.remove(prop);
			myAnimtionVersions.remove(prop);
		}
	}

	@Override
	public void clear() {
		myAnimationMap.clear();
		myAnimtionVersions.clear();
	}

}
