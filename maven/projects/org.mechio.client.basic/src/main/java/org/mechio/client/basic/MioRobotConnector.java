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

import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageBlockingReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.messaging.RemoteRobotClient;
import org.mechio.api.motion.protocol.MotionFrameEvent;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;
import org.mechio.api.motion.protocol.RobotRequest;
import org.mechio.api.motion.protocol.RobotResponse;
import org.mechio.client.basic.ConnectionContext.MioServiceConnector;
import org.mechio.impl.motion.messaging.PortableMotionFrameEvent;
import org.mechio.impl.motion.messaging.PortableRobotDefinitionResponse;
import org.mechio.impl.motion.messaging.PortableRobotRequest;
import org.mechio.impl.motion.messaging.PortableRobotResponse;
import org.mechio.impl.motion.messaging.RobotDefinitionResponseRecord;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
final class MioRobotConnector extends MioServiceConnector {
	private final static Logger theLogger = Logger.getLogger(MioRobotConnector.class.getName());
	final static String theDefaultId = "Avatar_ZenoR50";

	private final static String RESPONSE_RECEIVER = "robotResponseReceiver";
	private final static String REQUEST_SENDER = "robotRequestSender";
	private final static String MOVE_SENDER = "robotMoveSender";
	private final static String DEF_RECEIVER = "robotDefinitionReceiver";

	private Robot.Id myRobotId;
	private String myResponseDest;
	private String myRequestDest;
	private String myMoveDest;
	private String myDefDest;
	private RemoteRobotClient myRobotClient;

	static Map<String, MioRobotConnector> theMioRobotConnectorMap = new TreeMap<>();

	static synchronized MioRobotConnector getConnector() {
		return getConnector(MechIO.getRobotContextId(), theDefaultId);
	}

	static synchronized MioRobotConnector getConnector(final String context, final String defalutRobotId) {
		if (!theMioRobotConnectorMap.containsKey(context)) {
			final MioRobotConnector mioRobotConnector = new MioRobotConnector();
			mioRobotConnector.setRobotId(defalutRobotId);
			theMioRobotConnectorMap.put(context, mioRobotConnector);
		}

		return theMioRobotConnectorMap.get(context);
	}

	static synchronized void clearConnector() {
		theMioRobotConnectorMap.clear();
	}

	public MioRobotConnector() {
		setRobotId(theDefaultId);
	}

	void setRobotId(final String robotId) {
		if (robotId == null) {
			throw new NullPointerException();
		}
		myRobotId = new Robot.Id(robotId);
		String cleanId = robotId.replaceAll("[^a-zA-Z0-9]+", "");
		myResponseDest = "robot" + cleanId + "hostrobotResponse";
		myRequestDest = "robot" + cleanId + "hostrobotRequest";
		myMoveDest = "robot" + cleanId + "hostmotionFrame";
		myDefDest = "robot" + cleanId + "hostrobotDefinition";
	}

	@Override
	protected synchronized void addConnection(Session session) throws
			JMSException, URISyntaxException {
		if (myConnectionContext == null || myConnectionsFlag) {
			return;
		}
		Destination resp = ConnectionContext.getTopic(myResponseDest);
		Destination req = ConnectionContext.getTopic(myRequestDest);
		Destination move = ConnectionContext.getTopic(myMoveDest);
		Destination def = ConnectionContext.getTopic(myDefDest);

		myConnectionContext.addBlockingPolyReceiver(RESPONSE_RECEIVER, session, resp,
				new PortableRobotResponse.RecordMessageAdapter());
		myConnectionContext.addSender(REQUEST_SENDER, session, req,
				new PortableRobotRequest.MessageRecordAdapter());
		myConnectionContext.addSender(MOVE_SENDER, session, move,
				new PortableMotionFrameEvent.MessageRecordAdapter());
		myConnectionContext.addAsyncReceiver(DEF_RECEIVER, session, def,
				RobotDefinitionResponseRecord.class,
				RobotDefinitionResponseRecord.SCHEMA$,
				new PortableRobotDefinitionResponse.RecordMessageAdapter());
		myConnectionsFlag = true;
	}

	synchronized RemoteRobotClient buildRemoteClient() {
		if (myConnectionContext == null || !myConnectionsFlag) {
			return null;
		}
		if (myRobotClient != null) {
			return myRobotClient;
		}
		MessageBlockingReceiver<RobotResponse> respReceiver =
				myConnectionContext.getBlockingReceiver(RESPONSE_RECEIVER);
		MessageSender<RobotRequest> reqSender =
				myConnectionContext.getSender(REQUEST_SENDER);
		MessageSender<MotionFrameEvent> moveSender =
				myConnectionContext.getSender(MOVE_SENDER);

		myRobotClient = new RemoteRobotClient(
				myRobotId, "source", "dest",
				new PortableRobotRequest.Factory(),
				new PortableMotionFrameEvent.Factory());
		myRobotClient.setResponseReceiver(respReceiver);
		myRobotClient.setRequestSender(reqSender);
		myRobotClient.setMotionFrameSender(moveSender);
		return myRobotClient;
	}

	synchronized MessageAsyncReceiver<RobotDefinitionResponse> getDefReceiver() {
		return myConnectionContext.getAsyncReceiver(DEF_RECEIVER);
	}
}
