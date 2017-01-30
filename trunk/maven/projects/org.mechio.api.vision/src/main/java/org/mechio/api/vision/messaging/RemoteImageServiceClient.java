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
import org.mechio.api.vision.ImageEvent;
import org.mechio.api.vision.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects to a remote ImageService through Messaging components.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteImageServiceClient<Conf> extends
		DefaultServiceClient<Conf> implements ImageService {
	private static final Logger theLogger = LoggerFactory.getLogger(RemoteImageServiceClient.class);
	private String myImageServiceId;
	private MessageAsyncReceiver<ImageEvent> myImageReceiver;
	private EventRepeater<ImageEvent> myImageEventRepeater;

	/**
	 * Creates a new RemoteImageServiceClients.
	 * Call <code>start()</code> to start the service.
	 */
	public RemoteImageServiceClient(
			Class<Conf> configClass,
			String imageServiceId,
			String remoteId,
			MessageSender<ServiceCommand> commandSender,
			MessageSender<Conf> configSender,
			MessageAsyncReceiver<ServiceError> errorReceiver,
			ServiceCommandFactory commandFactory,
			MessageAsyncReceiver<ImageEvent> imageReceiver) {
		super(imageServiceId, remoteId,
				commandSender, configSender, errorReceiver, commandFactory);
		if (imageServiceId == null) {
			throw new NullPointerException();
		}
		myImageServiceId = imageServiceId;
		myImageReceiver = imageReceiver;
		myImageEventRepeater = new EventRepeater<>();
	}

	@Override
	public String getImageServiceId() {
		return myImageServiceId;
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
		} else if (myImageReceiver == null) {
			theLogger.info("PlayState changed to {}, "
					+ "but ImageReceiver is null.", state);
			return true;
		} else if (state == PlayState.RUNNING) {
			theLogger.info("PlayState changed to {}, "
					+ "adding repeater to ImageReceiver.", state);
			myImageReceiver.addListener(myImageEventRepeater);
		} else {
			theLogger.info("PlayState changed to {}, "
					+ "removing repeater from ImageReceiver.", state);
			myImageReceiver.removeListener(myImageEventRepeater);
		}
		return true;
	}

	public void setImageReceiver(MessageAsyncReceiver<ImageEvent> receiver) {
		if (myImageReceiver != null) {
			myImageReceiver.removeListener(myImageEventRepeater);
		}
		myImageReceiver = receiver;
		if (myImageReceiver != null && PlayState.RUNNING == getPlayState()) {
			myImageReceiver.addListener(myImageEventRepeater);
		}
	}

	@Override
	public void addImageListener(Listener<ImageEvent> listener) {
		myImageEventRepeater.addListener(listener);
	}

	@Override
	public void removeImageListener(Listener<ImageEvent> listener) {
		myImageEventRepeater.removeListener(listener);
	}
}
