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

import org.jflux.api.core.Listener;
import org.jflux.impl.services.rk.osgi.SingleServiceListener;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.library.AnimationLibrary;
import org.mechio.api.animation.protocol.AnimationEvent;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class LibraryRemoteAnimationAdder implements Listener<AnimationEvent> {
	private static final Logger theLogger = LoggerFactory.getLogger(LibraryRemoteAnimationAdder.class);
	private SingleServiceListener<AnimationLibrary> myLibraryTracker;

	public LibraryRemoteAnimationAdder(
			BundleContext context, String libraryFilter) {
		if (context == null) {
			throw new NullPointerException();
		}
		myLibraryTracker = new SingleServiceListener<>(
				AnimationLibrary.class, context, libraryFilter);
		myLibraryTracker.start();
	}

	@Override
	public void handleEvent(AnimationEvent event) {
		if (event == null) {
			return;
		}
		Animation anim = event.getAnimation();
		if (anim == null) {
			return;
		}
		AnimationLibrary lib = myLibraryTracker.getService();
		if (lib == null) {
			return;
		}
		lib.add(anim);
		myLibraryTracker.releaseService();
	}
}
