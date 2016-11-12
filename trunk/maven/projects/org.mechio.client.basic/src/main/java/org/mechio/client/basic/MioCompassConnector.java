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
import org.mechio.api.sensor.CompassConfigEvent;
import org.mechio.api.sensor.DeviceReadPeriodEvent;
import org.mechio.api.sensor.FilteredVector3Event;
import org.mechio.api.sensor.imu.RemoteCompassServiceClient;
import org.mechio.client.basic.ConnectionContext.MioServiceConnector;
import org.mechio.impl.sensor.CompassConfigRecord;
import org.mechio.impl.sensor.DeviceReadPeriodRecord;
import org.mechio.impl.sensor.FilteredVector3Record;
import org.mechio.impl.sensor.HeaderRecord;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
final class MioCompassConnector extends MioServiceConnector {
	final static String COMPASS_VALUE_RECEIVER = "compassValueReceiver";
	final static String COMPASS_CONFIG_SENDER = "compassConfigSender";
	final static String COMPASS_READ_PERIOD_SENDER = "compassReadPeriodSender";

	private String theCompassInputDest = "compassEvent";
	private String theCompassConfigDest = "compassConfig";
	private String theCompassReadDest = "compassRead";

	static Map<String, MioCompassConnector> theMioCompassConnectorMap = new TreeMap<>();

	static synchronized MioCompassConnector getConnector() {
		return getConnector(MechIO.getSensorContextId());
	}

	static synchronized MioCompassConnector getConnector(final String context) {
		if (!theMioCompassConnectorMap.containsKey(context)) {
			final MioCompassConnector mioCompassConnector = new MioCompassConnector();
			theMioCompassConnectorMap.put(context, mioCompassConnector);
		}

		return theMioCompassConnectorMap.get(context);
	}

	@Override
	protected synchronized void addConnection(Session session)
			throws JMSException, URISyntaxException {
		if (myConnectionContext == null || myConnectionsFlag) {
			return;
		}
		Destination compassValReceiver = ConnectionContext.getTopic(theCompassInputDest);
		myConnectionContext.addAsyncReceiver(COMPASS_VALUE_RECEIVER, session, compassValReceiver,
				FilteredVector3Record.class, FilteredVector3Record.SCHEMA$,
				new EmptyAdapter<FilteredVector3Record, FilteredVector3Record>());
		Destination compassCfgSender = ConnectionContext.getTopic(theCompassConfigDest);
		myConnectionContext.addSender(COMPASS_CONFIG_SENDER, session, compassCfgSender,
				new EmptyAdapter<CompassConfigRecord, CompassConfigRecord>());
		Destination compassPerSender = ConnectionContext.getTopic(theCompassReadDest);
		myConnectionContext.addSender(COMPASS_READ_PERIOD_SENDER, session, compassPerSender,
				new EmptyAdapter<DeviceReadPeriodRecord, DeviceReadPeriodRecord>());
		myConnectionsFlag = true;
	}

	synchronized RemoteCompassServiceClient buildRemoteClient() {
		if (myConnectionContext == null || !myConnectionsFlag) {
			return null;
		}
		MessageAsyncReceiver<FilteredVector3Event> compassValReceiver =
				myConnectionContext.getAsyncReceiver(COMPASS_VALUE_RECEIVER);
		MessageSender<CompassConfigEvent<HeaderRecord>> compassCfgSender =
				myConnectionContext.getSender(COMPASS_CONFIG_SENDER);
		MessageSender<DeviceReadPeriodEvent<HeaderRecord>> compassPerSender =
				myConnectionContext.getSender(COMPASS_READ_PERIOD_SENDER);

		RemoteCompassServiceClient<HeaderRecord> client =
				new RemoteCompassServiceClient<>(
						compassCfgSender, compassPerSender, compassValReceiver);
		return client;
	}
}
