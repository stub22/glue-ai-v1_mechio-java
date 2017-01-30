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
package org.mechio.api.vision.messaging;

import org.jflux.api.common.rk.playable.PlayState;
import org.jflux.api.common.rk.utils.EventRepeater;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Listener;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.jflux.api.messaging.rk.services.DefaultServiceClient;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.api.messaging.rk.services.ServiceCommandFactory;
import org.jflux.api.messaging.rk.services.ServiceError;
import org.mechio.api.vision.ImageRegionList;
import org.mechio.api.vision.ImageRegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects to a remote ImageService through Messaging components.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteImageRegionServiceClient<Conf> extends
		DefaultServiceClient<Conf> implements ImageRegionService {
	private static final Logger theLogger = LoggerFactory.getLogger(RemoteImageRegionServiceClient.class);
	private String myImageRegionsServiceId;
	private MessageAsyncReceiver<ImageRegionList> myRegionsReceiver;
	private EventRepeater<ImageRegionList> myRegionsEventRepeater;

	/**
	 * Creates a new RemoteImageServiceClients.
	 * Call <code>start()</code> to start the service.
	 */
	public RemoteImageRegionServiceClient(
			Class<Conf> configClass,
			String imageRegionsServiceId,
			String remoteId,
			MessageSender<ServiceCommand> commandSender,
			MessageSender<Conf> configSender,
			MessageAsyncReceiver<ServiceError> errorReceiver,
			ServiceCommandFactory commandFactory,
			MessageAsyncReceiver<ImageRegionList> regionsReceiver) {
		super(imageRegionsServiceId, remoteId,
				commandSender, configSender, errorReceiver, commandFactory);
		if (imageRegionsServiceId == null) {
			throw new NullPointerException();
		}
		myImageRegionsServiceId = imageRegionsServiceId;
		myRegionsReceiver = regionsReceiver;
		myRegionsEventRepeater = new EventRepeater<>();
	}

	@Override
	public String getImageServiceId() {
		return myImageRegionsServiceId;
	}

	@Override
	public void start() {
		super.start(TimeUtils.now());
	}

	@Override
	public void stop() {
		super.stop(TimeUtils.now());
	}

	@Override
	public boolean onComplete(long time) {
		return playStateChange(super.onComplete(time), PlayState.COMPLETED);
	}

	@Override
	public boolean onPause(long time) {
		return playStateChange(super.onPause(time), PlayState.PAUSED);
	}

	@Override
	public boolean onResume(long time) {
		return playStateChange(super.onResume(time), PlayState.RUNNING);
	}

	@Override
	public boolean onStart(long time) {
		return playStateChange(super.onStart(time), PlayState.RUNNING);
	}

	@Override
	public boolean onStop(long time) {
		return playStateChange(super.onStop(time), PlayState.STOPPED);
	}

	private boolean playStateChange(boolean attempt, PlayState state) {
		if (!attempt) {
			return false;
		} else if (myRegionsReceiver == null) {
			theLogger.info("PlayState changed to {}, "
					+ "but ImageReceiver is null.", state);
			return true;
		} else if (state == PlayState.RUNNING) {
			theLogger.info("PlayState changed to {}, "
					+ "adding repeater to ImageReceiver.", state);
			myRegionsReceiver.addListener(myRegionsEventRepeater);
		} else {
			theLogger.info("PlayState changed to {}, "
					+ "removing repeater from ImageReceiver.", state);
			myRegionsReceiver.removeListener(myRegionsEventRepeater);
		}
		return true;
	}

	public void setImageRegionsReceiver(
			MessageAsyncReceiver<ImageRegionList> receiver) {
		if (myRegionsReceiver != null) {
			myRegionsReceiver.removeListener(myRegionsEventRepeater);
		}
		myRegionsReceiver = receiver;
		if (myRegionsReceiver != null && PlayState.RUNNING == getPlayState()) {
			myRegionsReceiver.addListener(myRegionsEventRepeater);
		}
	}

	@Override
	public void addImageRegionsListener(Listener<ImageRegionList> listener) {
		myRegionsEventRepeater.addListener(listener);
	}

	@Override
	public void removeImageRegionsListener(Listener<ImageRegionList> listener) {
		myRegionsEventRepeater.removeListener(listener);
	}
}
