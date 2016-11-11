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

package org.mechio.client.basic;

import org.jflux.api.core.util.EmptyAdapter;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.api.messaging.rk.services.ServiceError;
import org.jflux.impl.messaging.rk.JMSAvroMessageSender;
import org.jflux.impl.messaging.rk.ServiceErrorRecord;
import org.jflux.impl.messaging.rk.services.PortableServiceCommand;
import org.mechio.api.speech.SpeechConfig;
import org.mechio.api.speech.SpeechEventList;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speech.messaging.RemoteSpeechServiceClient;
import org.mechio.client.basic.ConnectionContext.MioServiceConnector;
import org.mechio.impl.speech.PortableSpeechRequest;
import org.mechio.impl.speech.SpeechEventListRecord;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
final class MioSpeechConnector extends MioServiceConnector {
	final static String CMD_SENDER = "spCommandSender";
	final static String CONFIG_SENDER = "spConfigSender";
	final static String ERROR_RECEIVER = "spErrorReceiver";
	final static String SPEECH_SENDER = "spRequestSender";
	final static String EVENT_RECEIVER = "spEventReceiver";

	static Map<String, MioSpeechConnector> theMioSpeechConnectorMap = new TreeMap<>();

	private String myCommandDest = "speechCommand";
	private String myConfigDest = "speechCommand"; //Same as command
	private String myErrorDest = "speechError";
	private String myRequestDest = "speechRequest";
	private String myEventDest = "speechEvent";

	static synchronized MioSpeechConnector getConnector() {
		return getConnector(MechIO.getSpeechContextId());
	}

	static synchronized MioSpeechConnector getConnector(final String speechContext) {
		if (!theMioSpeechConnectorMap.containsKey(speechContext)) {
			theMioSpeechConnectorMap.put(speechContext, new MioSpeechConnector());
		}

		return theMioSpeechConnectorMap.get(speechContext);
	}


	@Override
	protected synchronized void addConnection(Session session)
			throws JMSException, URISyntaxException {
		if (myConnectionContext == null || myConnectionsFlag) {
			return;
		}
		Destination cmdDest = ConnectionContext.getQueue(myCommandDest);
		Destination confDest = ConnectionContext.getQueue(myConfigDest);
		Destination errDest = ConnectionContext.getTopic(myErrorDest);
		Destination reqDest = ConnectionContext.getQueue(myRequestDest);
		Destination evtDest = ConnectionContext.getTopic(myEventDest);

		myConnectionContext.addSender(CMD_SENDER, session, cmdDest,
				new EmptyAdapter());
		myConnectionContext.addSender(CONFIG_SENDER, session, confDest,
				new EmptyAdapter());
		myConnectionContext.addAsyncReceiver(ERROR_RECEIVER, session, errDest,
				ServiceErrorRecord.class, ServiceErrorRecord.SCHEMA$,
				new EmptyAdapter());

		myConnectionContext.addSender(SPEECH_SENDER, session, reqDest,
				new EmptyAdapter());
		myConnectionContext.addAsyncReceiver(EVENT_RECEIVER, session, evtDest,
				SpeechEventListRecord.class, SpeechEventListRecord.SCHEMA$,
				new EmptyAdapter());
		myConnectionsFlag = true;
	}

	synchronized RemoteSpeechServiceClient<SpeechConfig> buildRemoteClient() {
		if (myConnectionContext == null || !myConnectionsFlag) {
			return null;
		}
		MessageSender<ServiceCommand> cmdSender =
				myConnectionContext.getSender(CMD_SENDER);
		((JMSAvroMessageSender) cmdSender).setDefaultContentType("application/service-command");
		MessageSender<SpeechConfig> confSender =
				myConnectionContext.getSender(CONFIG_SENDER);
		MessageAsyncReceiver<ServiceError> errReceiver =
				myConnectionContext.getAsyncReceiver(ERROR_RECEIVER);

		MessageSender<SpeechRequest> reqSender =
				myConnectionContext.getSender(SPEECH_SENDER);
		MessageAsyncReceiver<SpeechEventList> evtReceiver =
				myConnectionContext.getAsyncReceiver(EVENT_RECEIVER);

		RemoteSpeechServiceClient<SpeechConfig> client =
				new RemoteSpeechServiceClient(SpeechConfig.class,
						"speechServiceId", "remoteSpeechServiceId",
						cmdSender, confSender, errReceiver,
						new PortableServiceCommand.Factory(),
						reqSender, evtReceiver,
						new PortableSpeechRequest.Factory());

		return client;
	}
}
