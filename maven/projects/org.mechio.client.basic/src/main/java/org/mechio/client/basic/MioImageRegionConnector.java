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
import org.jflux.impl.messaging.rk.ServiceErrorRecord;
import org.jflux.impl.messaging.rk.services.PortableServiceCommand;
import org.mechio.api.vision.ImageEvent;
import org.mechio.api.vision.config.CameraServiceConfig;
import org.mechio.api.vision.config.FaceDetectServiceConfig;
import org.mechio.api.vision.messaging.RemoteImageRegionServiceClient;
import org.mechio.client.basic.ConnectionContext.MioServiceConnector;
import org.mechio.impl.vision.ImageRegionListRecord;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
final class MioImageRegionConnector extends MioServiceConnector {
	final static String CMD_SENDER = "irCommandSender";
	final static String CONFIG_SENDER = "irConfigSender";
	final static String ERROR_RECEIVER = "irErrorReceiver";
	final static String EVENT_RECEIVER = "irEventReceiver";

	private String myCommandDest = "visionproc0Command";
	private String myConfigDest = "visionproc0Command"; //Same as command
	private String myErrorDest = "visionproc0Error";
	private String myEventDest = "visionproc0Event";

	static Map<String, MioImageRegionConnector> theMioImageRegionConnectorMap = new TreeMap<>();

	static synchronized MioImageRegionConnector getConnector() {
		return getConnector(MechIO.getImageRegionContextId());
	}

	static synchronized MioImageRegionConnector getConnector(final String context) {
		if (!theMioImageRegionConnectorMap.containsKey(context)) {
			final MioImageRegionConnector mioImageRegionConnector = new MioImageRegionConnector();
			theMioImageRegionConnectorMap.put(context, mioImageRegionConnector);
		}

		return theMioImageRegionConnectorMap.get(context);
	}

	@Override
	protected synchronized void addConnection(Session session)
			throws JMSException, URISyntaxException {
		if (myConnectionContext == null || myConnectionsFlag) {
			return;
		}

		readCameraId();

		Destination cmdDest = ConnectionContext.getQueue(myCommandDest);
		Destination confDest = ConnectionContext.getQueue(myConfigDest);
		Destination errDest = ConnectionContext.getTopic(myErrorDest);
		Destination evtDest = ConnectionContext.getTopic(myEventDest);

		myConnectionContext.addSender(CMD_SENDER, session, cmdDest,
				new EmptyAdapter());
		myConnectionContext.addSender(CONFIG_SENDER, session, confDest,
				new EmptyAdapter());
		myConnectionContext.addAsyncReceiver(ERROR_RECEIVER, session, errDest,
				ServiceErrorRecord.class, ServiceErrorRecord.SCHEMA$,
				new EmptyAdapter());
		myConnectionContext.addAsyncReceiver(EVENT_RECEIVER, session, evtDest,
				ImageRegionListRecord.class, ImageRegionListRecord.SCHEMA$,
				new EmptyAdapter());

		myConnectionsFlag = true;
	}

	synchronized RemoteImageRegionServiceClient<FaceDetectServiceConfig> buildRemoteClient() {
		if (myConnectionContext == null || !myConnectionsFlag) {
			return null;
		}
		MessageSender<ServiceCommand> cmdSender =
				myConnectionContext.getSender(CMD_SENDER);
		MessageSender<CameraServiceConfig> confSender =
				myConnectionContext.getSender(CONFIG_SENDER);
		MessageAsyncReceiver<ServiceError> errReceiver =
				myConnectionContext.getAsyncReceiver(ERROR_RECEIVER);
		MessageAsyncReceiver<ImageEvent> evtReceiver =
				myConnectionContext.getAsyncReceiver(EVENT_RECEIVER);

		RemoteImageRegionServiceClient<FaceDetectServiceConfig> client =
				new RemoteImageRegionServiceClient(
						FaceDetectServiceConfig.class, "imageRegionServiceId",
						"remoteImageRegionServiceId", cmdSender, confSender,
						errReceiver, new PortableServiceCommand.Factory(), evtReceiver);

		return client;
	}

	private synchronized void readCameraId() {
		String cameraId = UserSettings.getImageRegionId();

		if (cameraId.equals("0")) {
			myCommandDest = myCommandDest.replace("1", cameraId);
			myConfigDest = myConfigDest.replace("1", cameraId);
			myErrorDest = myErrorDest.replace("1", cameraId);
			myEventDest = myEventDest.replace("1", cameraId);
		} else if (cameraId.equals("1")) {
			myCommandDest = myCommandDest.replace("0", cameraId);
			myConfigDest = myConfigDest.replace("0", cameraId);
			myErrorDest = myErrorDest.replace("0", cameraId);
			myEventDest = myEventDest.replace("0", cameraId);
		}
	}
}
