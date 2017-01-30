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
package org.mechio.api.speechrec.messaging;

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
import org.mechio.api.speechrec.SpeechRecEventList;
import org.mechio.api.speechrec.SpeechRecService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects to a remote SpeechRecService through Messaging components.
 *
 * @author Amy Jessica Book <www.mechio.org>
 */
public class RemoteSpeechRecServiceClient<Conf> extends
		DefaultServiceClient<Conf> implements SpeechRecService {
	private static final Logger theLogger = LoggerFactory.getLogger(RemoteSpeechRecServiceClient.class);
	private String mySpeechRecServiceId;
	private MessageAsyncReceiver<SpeechRecEventList> mySpeechRecReceiver;
	private EventRepeater<SpeechRecEventList> mySpeechRecEventRepeater;

	/**
	 * Creates a new RemoteSpeechRecServiceClients.
	 * Call <code>start()</code> to start the service.
	 */
	public RemoteSpeechRecServiceClient(
			Class<Conf> configClass,
			String speechRecServiceId,
			String remoteId,
			MessageSender<ServiceCommand> commandSender,
			MessageSender<Conf> configSender,
			MessageAsyncReceiver<ServiceError> errorReceiver,
			ServiceCommandFactory commandFactory,
			MessageAsyncReceiver<SpeechRecEventList> speechRecReceiver) {
		super(speechRecServiceId, remoteId,
				commandSender, configSender, errorReceiver, commandFactory);
		if (speechRecServiceId == null) {
			throw new NullPointerException();
		}
		mySpeechRecServiceId = speechRecServiceId;
		mySpeechRecReceiver = speechRecReceiver;
		mySpeechRecEventRepeater = new EventRepeater<>();
	}

	@Override
	public String getSpeechRecServiceId() {
		return mySpeechRecServiceId;
	}

	@Override
	public void start() {
		start(TimeUtils.now());
		if (mySpeechRecReceiver != null) {
			mySpeechRecReceiver.addListener(mySpeechRecEventRepeater);
		}
	}

	@Override
	public void stop() {
		super.stop(TimeUtils.now());
	}

	@Override
	public boolean onComplete(long time) {
		return playStateChange(super.onStop(time), PlayState.COMPLETED);
	}

	@Override
	public boolean onPause(long time) {
		return playStateChange(super.onStop(time), PlayState.PAUSED);
	}

	@Override
	public boolean onResume(long time) {
		return playStateChange(super.onStop(time), PlayState.RUNNING);
	}

	@Override
	public boolean onStart(long time) {
		return playStateChange(super.onStop(time), PlayState.RUNNING);
	}

	@Override
	public boolean onStop(long time) {
		return playStateChange(super.onStop(time), PlayState.STOPPED);
	}

	private boolean playStateChange(boolean attempt, PlayState state) {
		if (!attempt) {
			return false;
		} else if (mySpeechRecReceiver == null) {
			theLogger.info("PlayState changed to {}, "
					+ "but SpeechRecReceiver is null.", state);
			return true;
		} else if (state == PlayState.RUNNING) {
			theLogger.info("PlayState changed to {}, "
					+ "adding repeater to SpeechRecReceiver.", state);
			mySpeechRecReceiver.addListener(mySpeechRecEventRepeater);
		} else {
			theLogger.info("PlayState changed to {}, "
					+ "removing repeater from SpeechRecReceiver.", state);
			mySpeechRecReceiver.removeListener(mySpeechRecEventRepeater);
		}
		return true;
	}

	public void setSpeechRecReceiver(MessageAsyncReceiver<SpeechRecEventList> receiver) {
		if (mySpeechRecReceiver != null) {
			mySpeechRecReceiver.removeListener(mySpeechRecEventRepeater);
		}
		mySpeechRecReceiver = receiver;
		if (mySpeechRecReceiver != null && PlayState.RUNNING == getPlayState()) {
			mySpeechRecReceiver.addListener(mySpeechRecEventRepeater);
		}
	}

	@Override
	public void addSpeechRecListener(Listener<SpeechRecEventList> listener) {
		mySpeechRecEventRepeater.addListener(listener);
	}

	@Override
	public void removeSpeechRecListener(Listener<SpeechRecEventList> listener) {
		mySpeechRecEventRepeater.removeListener(listener);
	}
}
