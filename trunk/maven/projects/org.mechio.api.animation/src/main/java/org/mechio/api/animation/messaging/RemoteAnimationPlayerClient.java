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
package org.mechio.api.animation.messaging;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.playable.Playable;
import org.jflux.api.common.rk.services.addon.ServiceAddOn;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Listener;
import org.jflux.api.core.util.DefaultNotifier;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.Channel;
import org.mechio.api.animation.MotionPath;
import org.mechio.api.animation.player.AnimationJob;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.protocol.AnimationEvent;
import org.mechio.api.animation.protocol.AnimationEvent.AnimationEventFactory;
import org.mechio.api.animation.protocol.AnimationSignal;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteAnimationPlayerClient
		extends DefaultNotifier<AnimationSignal>
		implements AnimationPlayer, Listener<AnimationSignal> {
	private static final Logger theLogger = LoggerFactory.getLogger(RemoteAnimationPlayerClient.class);
	private String myAnimationPlayerId;
	private String myRemotePlayerId;
	private MessageSender<AnimationEvent> myAnimationSender;
	private AnimationEventFactory myEventFactory;
	private List<RemoteAnimationJob> myAnimationJobs;
	private MessageAsyncReceiver<AnimationSignal> mySignalReceiver;
	private BundleContext myContext;
	private Map<AnimationJob, ServiceRegistration> myRegistry;

	public RemoteAnimationPlayerClient(
			BundleContext context, String animPlayerClientId,
			String animPlayerHostId) {
		if (animPlayerClientId == null || animPlayerHostId == null) {
			throw new NullPointerException();
		}
		myAnimationPlayerId = animPlayerClientId;
		myRemotePlayerId = animPlayerHostId;
		myContext = context;
		myRegistry = new HashMap();
		myAnimationJobs = new ArrayList<>();
	}

	@Override
	public String getAnimationPlayerId() {
		return myAnimationPlayerId;
	}

	public void setAnimationEventFactory(AnimationEventFactory factory) {
		myEventFactory = factory;
	}

	public void setAnimationEventSender(MessageSender<AnimationEvent> sender) {
		myAnimationSender = sender;
	}

	public void setAnimationSignalReceiver(
			MessageAsyncReceiver<AnimationSignal> receiver) {
		if (mySignalReceiver != null) {
			mySignalReceiver.removeListener(this);
		}

		mySignalReceiver = receiver;

		if (mySignalReceiver != null) {
			mySignalReceiver.addListener(this);
		}
	}

	public AnimationJob playAnimation(Animation animation, boolean register) {
		if (myEventFactory == null || myAnimationSender == null) {
			return null;
		}
		String[] names = new String[]{
				AnimationJob.class.getName(),
		};
		AnimationEvent event =
				myEventFactory.createAnimationEvent(
						myAnimationPlayerId, myRemotePlayerId, animation);
		myAnimationSender.notifyListeners(event);
		for (ServiceAddOn<Playable> add : animation.getAddOns()) {
			add.getAddOn().start(TimeUtils.now());
		}
		RemoteAnimationJob job = new RemoteAnimationJob(this, animation,
				animation.getStartTime(), animation.getStopTime(), 800);
		if (register && myContext != null) {
			Dictionary props = new Properties();
			ServiceRegistration reg =
					myContext.registerService(names, job, props);
			myRegistry.put(job, reg);
		}
		myAnimationJobs.add(job);
		job.start(TimeUtils.now());
		return job;
	}

	@Override
	public AnimationJob playAnimation(Animation animation) {
		return playAnimation(animation, true);
	}

	public AnimationJob loopAnimation(Animation animation, boolean register) {
		if (myEventFactory == null || myAnimationSender == null) {
			return null;
		}
		String[] names = new String[]{
				AnimationJob.class.getName(),
		};
		AnimationEvent event =
				myEventFactory.createAnimationEvent(
						myAnimationPlayerId, "LOOP", animation);
		myAnimationSender.notifyListeners(event);
		for (ServiceAddOn<Playable> add : animation.getAddOns()) {
			add.getAddOn().start(TimeUtils.now());
		}
		AnimationJob job = new RemoteAnimationJob(this, animation,
				animation.getStartTime(), animation.getStopTime(), 800);
		if (register && myContext != null) {
			Dictionary props = new Properties();
			ServiceRegistration reg =
					myContext.registerService(names, job, props);
			myRegistry.put(job, reg);
		}
		job.start(TimeUtils.now());
		return job;
	}

	public AnimationJob loopAnimation(Animation animation) {
		return loopAnimation(animation, true);
	}

	public void stopAnimation(Animation animation) {
		if (myEventFactory == null || myAnimationSender == null) {
			return;
		}
		AnimationEvent event =
				myEventFactory.createAnimationEvent(
						myAnimationPlayerId, "STOP", animation);
		myAnimationSender.notifyListeners(event);
		for (ServiceAddOn<Playable> add : animation.getAddOns()) {
			add.getAddOn().stop(TimeUtils.now());
		}
	}

	public void clearAnimations() {
		if (myEventFactory == null || myAnimationSender == null) {
			return;
		}
		Animation empty = new Animation(new VersionProperty("empty", "1.0"));
		empty.addChannel(new Channel(0, "emptyChan"));
		MotionPath path = new MotionPath();
		path.addPoint(0, 0.5);
		path.addPoint(1, 0.5);
		empty.getChannel(0).addPath(path);
		AnimationEvent event =
				myEventFactory.createAnimationEvent(
						myAnimationPlayerId, "CLEAR", empty);
		myAnimationSender.notifyListeners(event);
		return;
	}

	@Override
	public AnimationJob playAnimation(Animation animation, Long start, Long stop) {
		animation.setStartTime(start);
		animation.setStopTime(stop);
		return playAnimation(animation);
	}

	@Override
	public List<AnimationJob> getCurrentAnimations() {
		return (List) myAnimationJobs;
	}

	@Override
	public void removeAnimationJob(AnimationJob job) {
		if (!(job instanceof RemoteAnimationJob)) {
			return;
		}
		RemoteAnimationJob rjob = (RemoteAnimationJob) job;
		if (!myAnimationJobs.contains(rjob)) {
			return;
		}
		rjob.stop(TimeUtils.now());
		if (myContext != null) {
			ServiceRegistration reg = myRegistry.remove(job);
			if (reg == null) {
				return;
			}
			try {
				reg.unregister();
			} catch (IllegalStateException ex) {
				theLogger.warn("Unable to unregister AnimationJob.  Already unregistered.",
						ex);
			}
		}
		myAnimationJobs.remove(rjob);
	}

	@Override
	public void addAnimationSignalListener(Listener<AnimationSignal> listener) {
		myListeners.add(listener);
	}

	@Override
	public void removeAnimationSignalListener(
			Listener<AnimationSignal> listener) {
		myListeners.remove(listener);
	}

	@Override
	public void handleEvent(AnimationSignal t) {
		boolean hashFound = false;
		RemoteAnimationJob goodJob = null;

		for (RemoteAnimationJob job : myAnimationJobs) {
			if (job.getAnimation().hashCode() == t.getAnimationHash()) {
				hashFound = true;
				goodJob = job;
				break;
			}
		}

		if (!hashFound) {
			for (RemoteAnimationJob job : myAnimationJobs) {
				if (job.getAnimation().getVersion().getName().equals(
						t.getAnimationName()) &&
						job.getAnimation().getVersion().getNumber().equals(
								t.getAnimationVersion())) {
					goodJob = job;
					break;
				}
			}
		}

		if (goodJob != null) {
			if (t.getEventType().equals(AnimationSignal.EVENT_START)) {
				goodJob.start(TimeUtils.now());
			} else if (t.getEventType().equals(AnimationSignal.EVENT_PAUSE)) {
				goodJob.pause(TimeUtils.now());
			} else if (t.getEventType().equals(AnimationSignal.EVENT_RESUME)) {
				goodJob.resume(TimeUtils.now());
			} else if (t.getEventType().equals(AnimationSignal.EVENT_CANCEL)) {
				goodJob.stop(TimeUtils.now());
			} else if (t.getEventType().equals(AnimationSignal.EVENT_COMPLETE)) {
				goodJob.complete(TimeUtils.now());
			}
		}

		notifyListeners(t);
	}

	public void stopAllAnimations() {
		for (AnimationJob job : getCurrentAnimations()) {
			stopAnimation(job.getAnimation());
		}
	}
}
