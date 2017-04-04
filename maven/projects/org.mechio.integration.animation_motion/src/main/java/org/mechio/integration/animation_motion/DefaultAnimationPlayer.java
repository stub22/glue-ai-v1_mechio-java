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

package org.mechio.integration.animation_motion;

import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Listener;
import org.jflux.api.core.util.DefaultNotifier;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.player.AnimationJob;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.protocol.AnimationSignal;
import org.mechio.api.animation.protocol.AnimationSignal.AnimationSignalFactory;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.blending.FrameSource;
import org.mechio.impl.animation.messaging.PortableAnimationSignal;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * AnimationPlayer which creates AnimationJobFrameSource and registers them with the
 * OSGi service registry to be used as FrameSources.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DefaultAnimationPlayer extends DefaultNotifier<AnimationSignal>
		implements AnimationPlayer {
	private static final Logger theLogger = LoggerFactory.getLogger(DefaultAnimationPlayer.class);
	private final static long theStepLength = 40L;
	private BundleContext myContext;
	private Map<AnimationJob, ServiceRegistration> myRegistry;
	private List<AnimationJob> myAnimationJobs;
	private long myStepLength;
	private Robot.Id myRobotId;
	private static AnimationSignalFactory theSignalFactory =
			new PortableAnimationSignal.Factory();

	/**
	 * Creates a new OSGiAnimationPlayer using the given BundleContext.
	 *
	 * @param context BundleContext to be used by the OSGiAnimationPlayer
	 */
	public DefaultAnimationPlayer(BundleContext context, Robot.Id robotId) {
		if (context == null || robotId == null) {
			throw new NullPointerException();
		}
		myContext = context;
		myRobotId = robotId;
		myAnimationJobs = new ArrayList();
		myStepLength = theStepLength;
		myRegistry = new HashMap();
	}

	public void setRobotId(Robot.Id robotId) {
		if (robotId == null) {
			throw new NullPointerException();
		}
		myRobotId = robotId;
	}

	/**
	 * Returns the robotId this Animation Player uses.
	 *
	 * @return robotId this Animation Player uses
	 */
	public Robot.Id getRobotId() {
		return myRobotId;
	}

	@Override
	public String getAnimationPlayerId() {
		return myRobotId.getRobtIdString();
	}

	/**
	 * Sets the step length for AnimationJobs created by this player.
	 *
	 * @param val new step length in milliseconds
	 */
	public void setStepLength(long val) {
		myStepLength = val;
	}

	/**
	 * Returns the step length for new AnimationJobs.
	 *
	 * @return step length for new AnimationJobs
	 */
	public long getStepLength() {
		return myStepLength;
	}

	@Override
	public AnimationJob playAnimation(Animation animation) {
		return playAnimation(animation, null, null);
	}

	@Override
	public List<AnimationJob> getCurrentAnimations() {
		return Collections.unmodifiableList(myAnimationJobs);
	}

	@Override
	public void removeAnimationJob(AnimationJob job) {
		if (job == null) {
			return;
		}
		ServiceRegistration reg = myRegistry.remove(job);
		if (reg == null) {
			return;
		}
		try {
			reg.unregister();
		} catch (IllegalStateException ex) {
			theLogger.warn("Unable to unregister AnimationJob.  Already unregistered.", ex);
		}
		myAnimationJobs.remove(job);
	}

	@Override
	public AnimationJob playAnimation(Animation animation, Long start, Long stop) {
		String[] names = new String[]{
				AnimationJob.class.getName(),
				FrameSource.class.getName()
		};
		AnimationJob job = new AnimationJobFrameSource(
				this, myRobotId, animation, myStepLength, start, stop);
		Dictionary props = new Properties();
		props.put(Robot.PROP_ID, myRobotId.toString());
		ServiceRegistration reg = myContext.registerService(names, job, props);
		((AnimationJobFrameSource) job).setAnimationSignalFactory(
				theSignalFactory);
		myAnimationJobs.add(job);
		myRegistry.put(job, reg);
		job.start(TimeUtils.now());
		return job;
	}

	@Override
	public void addAnimationSignalListener(Listener<AnimationSignal> listener) {
		myListeners.add(listener);
	}

	@Override
	public void removeAnimationSignalListener(Listener<AnimationSignal> listener) {
		myListeners.remove(listener);
	}

    @Override
    public String toString() {
        return "DefaultAnimationPlayer{" + "myAnimationJobs=" + myAnimationJobs + ", myStepLength=" +
            myStepLength + ", myRobotId=" + myRobotId + '}';
    }


}
