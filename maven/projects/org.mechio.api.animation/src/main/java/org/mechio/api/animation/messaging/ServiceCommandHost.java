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

import org.jflux.api.core.Listener;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an easy way to listen for ServiceCommands. Implementors just need to override the {@link
 * ServiceCommandHost#handleEvent(ServiceCommand)} method and then call the {@link
 * ServiceCommandHost#start()} method to use.
 *
 * @author ben
 * @since 3/22/2017.
 */
public abstract class ServiceCommandHost implements Listener<ServiceCommand> {

	private static final Logger theLogger = LoggerFactory.getLogger(ServiceCommandHost.class);
	private final MessageAsyncReceiver<ServiceCommand> myMessageReceiver;
	private Listener<ServiceCommand> listener;
	private boolean initialized = false;

	/**
	 * Creates a new {@link ServiceCommandHost}.
	 *
	 * @param messageReceiver A message receiver to listen for ServiceCommands on. If the
	 *                        implementor needs a {@link MessageAsyncReceiver} they can easily get
	 *                        one from the {@link JMSAvroMessageAsyncReceiverFactory} or could get
	 *                        one from a lifecycle.
	 */
	public ServiceCommandHost(final MessageAsyncReceiver<ServiceCommand> messageReceiver) {
		this.myMessageReceiver = messageReceiver;
	}

	private void init() {

		final ServiceCommandHost serviceCommandHost = this;
		listener = new Listener<ServiceCommand>() {
			@Override
			public void handleEvent(final ServiceCommand input) {
				serviceCommandHost.handleEvent(input);
			}
		};
		myMessageReceiver.addListener(listener);

		initialized = true;
	}

    /*
	 * TODO: add method to set a different MessageReceiver.
     * Waiting until we have a use case. Since that will add extra state to reason about without
     * providing any immediate gains.
     *
     * When in doubt, leave it out.
     */


	@Override
	public abstract void handleEvent(ServiceCommand input);

	public final void start() {
		if (!initialized) {
			init();
		}


		try {
			myMessageReceiver.start();
		} catch (final Exception ex) {
			theLogger.error("Error starting listener", ex);
		}
	}

	public final void stop() {
		if (!initialized) {
			theLogger.warn("I can't stop! Was never started.");
			return;
		}

		try {
			myMessageReceiver.removeListener(listener);
			myMessageReceiver.stop();
		} catch (final Exception ex) {
			theLogger.error("Error stopping listener", ex);
		}
	}

}
