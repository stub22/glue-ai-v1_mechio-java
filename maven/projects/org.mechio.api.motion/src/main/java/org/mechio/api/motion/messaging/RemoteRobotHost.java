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
package org.mechio.api.motion.messaging;

import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Listener;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.jflux.spec.discovery.Beacon;
import org.jflux.spec.discovery.SerialNumberSpec;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.JointId;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.protocol.MotionFrameEvent;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;
import org.mechio.api.motion.protocol.RobotRequest;
import org.mechio.api.motion.protocol.RobotResponse;
import org.mechio.api.motion.protocol.RobotResponse.RobotResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Hosts a Robot to be controlled by a RemoteRobotClient through some Messaging
 * channel.  Receives RobotRequest Messages and replies with RobotResponse
 * Messages.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteRobotHost {
	private static final Logger theLogger = LoggerFactory.getLogger(RemoteRobotHost.class);
	public final static int DEFAULT_SEND_DATA_INTERVAL = 100;
	private final static String theSerialKey =
			"org.mechio.api.motion.serialnumber";

	private Robot myRobot;
	private String mySourceId;
	private String myDestinationId;
	private MessageSender<RobotResponse> myResponseSender;
	private MessageAsyncReceiver<RobotRequest> myRequestReceiver;
	private RequestListener myRequestListener;
	private RobotResponseFactory myResponseFactory;
	private MessageAsyncReceiver<MotionFrameEvent> myMotionFrameReceiver;
	private Listener<MotionFrameEvent> myMoveHandler;
	private Thread myDefinitionThread;
	private boolean myPusherActive;
	private MessageSender<RobotDefinitionResponse> myDefSender;
	private SerialNumberSpec mySerialNumber;
	private Beacon myBeacon;
	private int mySendDataIntervalMillisec = DEFAULT_SEND_DATA_INTERVAL;

	/**
	 * Creates a new RemoteRobotHost to host the given Robot.
	 *
	 * @param robot               Robot to host
	 * @param sourceId            arbitrary String identifying this host
	 * @param destinationId       arbitrary String identifying a client
	 * @param sender              MessageSender to send RobotResponses
	 * @param receiver            MessageReceiver to receive RobotRequests from a client
	 * @param factory             factory for creating new RobotResponse Messages
	 * @param motionFrameReceiver MessageReceiver to receive MotionFrameEvents
	 * @param moveHandler         Listener to handle MotionFrameEvents from clients
	 * @param defSender           MessageSender for pushing RobotDefinitionResponses
	 * @param serialNumber        SerialNumberSpec for uniquely identifying the robot
	 */
	public RemoteRobotHost(Robot robot,
						   String sourceId, String destinationId,
						   MessageSender<RobotResponse> sender,
						   MessageAsyncReceiver<RobotRequest> receiver,
						   RobotResponseFactory factory,
						   MessageAsyncReceiver<MotionFrameEvent> motionFrameReceiver,
						   Listener<MotionFrameEvent> moveHandler,
						   MessageSender<RobotDefinitionResponse> defSender,
						   SerialNumberSpec serialNumber,
						   int sendDataIntervalMillisec) {
		this(sourceId, destinationId);
		mySendDataIntervalMillisec = sendDataIntervalMillisec;
		initialize(
				robot, sender, receiver, factory, motionFrameReceiver,
				moveHandler, defSender, serialNumber);
	}

	/**
	 * Creates a new RemoteRobotHost to host the given Robot.
	 *
	 * @param robot               Robot to host
	 * @param sourceId            arbitrary String identifying this host
	 * @param destinationId       arbitrary String identifying a client
	 * @param sender              MessageSender to send RobotResponses
	 * @param receiver            MessageReceiver to receive RobotRequests from a client
	 * @param factory             factory for creating new RobotResponse Messages
	 * @param motionFrameReceiver MessageReceiver to receive MotionFrameEvents
	 * @param moveHandler         Listener to handle MotionFrameEvents from clients
	 * @param defSender           MessageSender for pushing RobotDefinitionResponses
	 * @param serialNumber        SerialNumberSpec for uniquely identifying the robot
	 */

	public RemoteRobotHost(Robot robot,
						   String sourceId, String destinationId,
						   MessageSender<RobotResponse> sender,
						   MessageAsyncReceiver<RobotRequest> receiver,
						   RobotResponseFactory factory,
						   MessageAsyncReceiver<MotionFrameEvent> motionFrameReceiver,
						   Listener<MotionFrameEvent> moveHandler,
						   MessageSender<RobotDefinitionResponse> defSender,
						   SerialNumberSpec serialNumber) {
		this(sourceId, destinationId);
		initialize(
				robot, sender, receiver, factory, motionFrameReceiver,
				moveHandler, defSender, serialNumber);
	}

	/**
	 * Creates a new RemoteRobotHost to host the given Robot.
	 *
	 * @param robot               Robot to host
	 * @param sourceId            arbitrary String identifying this host
	 * @param destinationId       arbitrary String identifying a client
	 * @param sender              MessageSender to send RobotResponses
	 * @param receiver            MessageReceiver to receive RobotRequests from a client
	 * @param factory             factory for creating new RobotResponse Messages
	 * @param motionFrameReceiver MessageReceiver to receive MotionFrameEvents
	 * @param moveHandler         Listener to handle MotionFrameEvents from clients
	 * @param defSender           MessageSender for pushing RobotDefinitionResponses
	 */
	public RemoteRobotHost(Robot robot,
						   String sourceId, String destinationId,
						   MessageSender<RobotResponse> sender,
						   MessageAsyncReceiver<RobotRequest> receiver,
						   RobotResponseFactory factory,
						   MessageAsyncReceiver<MotionFrameEvent> motionFrameReceiver,
						   Listener<MotionFrameEvent> moveHandler,
						   MessageSender<RobotDefinitionResponse> defSender) {
		this(sourceId, destinationId);
		initialize(robot, sender, receiver,
				factory, motionFrameReceiver, moveHandler, defSender, null);
	}

	/**
	 * Creates an empty RemoteRobotHost.
	 *
	 * @param sourceId      arbitrary String identifying this host
	 * @param destinationId arbitrary String identifying a client
	 */
	protected RemoteRobotHost(String sourceId, String destinationId) {
		if (sourceId == null || destinationId == null) {
			throw new NullPointerException();
		}
		mySourceId = sourceId;
		myDestinationId = destinationId;
		myRequestListener = new RequestListener();
		theLogger.info("Creating Remote Robot.  sourceId={}, destId={}",
				mySourceId, myDestinationId);
	}

	private void initialize(
			Robot robot,
			MessageSender<RobotResponse> sender,
			MessageAsyncReceiver<RobotRequest> receiver,
			RobotResponseFactory factory,
			MessageAsyncReceiver<MotionFrameEvent> motionFrameReceiver,
			Listener<MotionFrameEvent> moveHandler,
			MessageSender<RobotDefinitionResponse> defSender,
			SerialNumberSpec serialNumber) {
		if (myRequestReceiver != null && myRequestListener != null) {
			myRequestReceiver.removeListener(myRequestListener);
		}
		if (myMotionFrameReceiver != null && myMoveHandler != null) {
			myMotionFrameReceiver.removeListener(myMoveHandler);
		}
		myRobot = robot;
		myResponseSender = sender;
		myRequestReceiver = receiver;
		myResponseFactory = factory;
		myMotionFrameReceiver = motionFrameReceiver;
		myMoveHandler = moveHandler;
		myPusherActive = false;
		myDefSender = defSender;
		mySerialNumber = serialNumber;
		if (myRequestReceiver != null) {
			myRequestReceiver.addListener(myRequestListener);
		}
		if (myMotionFrameReceiver != null) {
			myMotionFrameReceiver.addListener(myMoveHandler);
		}
		if (mySerialNumber == null) {
			String robotSerial =
					System.getProperty(
							theSerialKey, System.getenv(theSerialKey));
			mySerialNumber = new SerialNumberSpec();
			mySerialNumber.setSerialNumber(
					robotSerial == null ?
							UUID.randomUUID().toString() : robotSerial);
			mySerialNumber.addProperty(
					"generated", robotSerial == null ? "random" : "property");
		} else if (mySerialNumber.getSerialNumber() == null) {
			String robotSerial =
					System.getProperty(
							theSerialKey, System.getenv(theSerialKey));
			mySerialNumber.setSerialNumber(
					robotSerial == null ?
							UUID.randomUUID().toString() : robotSerial);
			mySerialNumber.addProperty(
					"generated", robotSerial == null ? "random" : "property");
		}

		mySerialNumber.addProperty(
				"robotId", myRobot.getRobotId().getRobtIdString());

		myBeacon = new Beacon(mySerialNumber);
		new Thread(myBeacon).start();

		theLogger.info("Initializing Remote Robot.  sourceId={}, destId={}, robotId={}",
				mySourceId, myDestinationId, myRobot.getRobotId());
	}

	/**
	 * Sets the Robot to host.
	 *
	 * @param robot Robot to host
	 */
	public void setRobot(Robot robot) {
		myRobot = robot;
	}

	/**
	 * Sets the MessageSender to send RobotResponses.
	 *
	 * @param sender MessageSender to use
	 */
	public void setResponseSender(MessageSender<RobotResponse> sender) {
		myResponseSender = sender;
	}

	public MessageSender<RobotResponse> getResponseSender() {
		return myResponseSender;
	}

	/**
	 * Sets the MessageSender to send RobotDefinitionResponses.
	 *
	 * @param sender MessageSender to use
	 */
	public void setDefSender(MessageSender<RobotDefinitionResponse> sender) {
		myDefSender = sender;
	}

	public MessageSender<RobotDefinitionResponse> getDefSender() {
		return myDefSender;
	}

	/**
	 * Sets the MessageReceiver to receive RobotRequests.
	 *
	 * @param receiver MessageReceiver to use
	 */
	public void setRequestReceiver(
			MessageAsyncReceiver<RobotRequest> receiver) {
		if (myRequestReceiver != null && myRequestListener != null) {
			myRequestReceiver.removeListener(myRequestListener);
		}
		myRequestReceiver = receiver;
		if (myRequestReceiver != null && myRequestListener != null) {
			myRequestReceiver.addListener(myRequestListener);
		}
	}

	/**
	 * Sets the factory to use for creating new RobotResponse Messages.
	 *
	 * @param factory factory to use for creating new RobotResponse Messages
	 */
	public void setResponseFactory(RobotResponseFactory factory) {
		myResponseFactory = factory;
	}

	/**
	 * Sets the MessageReceiver to receive MotionFrames
	 *
	 * @param receiver MessageReceiver to use
	 */
	public void setMotionFrameReceiver(
			MessageAsyncReceiver<MotionFrameEvent> receiver) {
		if (myMotionFrameReceiver != null && myMoveHandler != null) {
			myMotionFrameReceiver.removeListener(myMoveHandler);
		}
		myMotionFrameReceiver = receiver;
		if (myMotionFrameReceiver != null && myMoveHandler != null) {
			myMotionFrameReceiver.addListener(myMoveHandler);
		}
	}

	/**
	 * Sets the Listener to handle MotionFrames from clients.
	 *
	 * @param moveHandler Listener to handle MotionFrames from clients
	 */
	public void setMoveHandler(Listener<MotionFrameEvent> moveHandler) {
		if (myMotionFrameReceiver != null && myMoveHandler != null) {
			myMotionFrameReceiver.removeListener(myMoveHandler);
		}
		myMoveHandler = moveHandler;
		if (myMotionFrameReceiver != null && myMoveHandler != null) {
			myMotionFrameReceiver.addListener(myMoveHandler);
		}
	}

	public void setSendDataInterval(int sendDataIntervalMillisec) {
		mySendDataIntervalMillisec = sendDataIntervalMillisec;
	}

	/**
	 * Returns the hosted Robot's id.
	 *
	 * @return hosted Robot's id
	 */
	public Robot.Id getRobotId() {
		if (myRobot == null) {
			return null;
		}
		return myRobot.getRobotId();
	}

	/**
	 * Returns a String identifying this host.  Currently unused.
	 *
	 * @return String identifying this host
	 */
	public String getSourceId() {
		return mySourceId;
	}

	/**
	 * Returns a String identifying a client.  Currently unused.
	 *
	 * @return String identifying a client
	 */
	public String getDestinationId() {
		return myDestinationId;
	}

	public Robot getRobot() {
		return myRobot;
	}

	public void shutDown() {
		myPusherActive = false;
	}

	/**
	 * Creates and sends a RobotDefinitionResponse.
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleDefinitionRequest(RobotRequest req) {
		sendDefinitionResponse(req);

		if (myDefinitionThread == null || !myPusherActive) {
			myPusherActive = true;
			myDefinitionThread = new Thread(new DefinitionPusher());
			myDefinitionThread.start();
		}
	}

	/**
	 * Calls <code>connect()</code> on the hosted Robot.  The return value from
	 * that call is returned in a RobotStatusResponse.
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleConnectRequest(RobotRequest req) {
		if (myDefinitionThread == null || !myPusherActive) {
			myPusherActive = true;
			myDefinitionThread = new Thread(new DefinitionPusher());
			myDefinitionThread.start();
		}

		sendStatusResponse(req, getRobot().connect());
	}

	/**
	 * Calls <code>disconnect()</code> on the hosted Robot.  The return value
	 * from that call is returned in a RobotStatusResponse.
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleDisconnectRequest(RobotRequest req) {
		getRobot().disconnect();

		if (myDefinitionThread != null && myPusherActive) {
			shutDown();
		}

		sendStatusResponse(req, true);
	}

	/**
	 * Creates and sends a RobotStatusResponse with the Robot's connection
	 * status
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleConnectionStatusRequest(RobotRequest req) {
		sendStatusResponse(req, getRobot().isConnected());
	}

	/**
	 * Calls <code>setEnabled(true)</code> on the hosted Robot.  Sends a
	 * successful RobotStatusResponse.
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleEnableRequest(RobotRequest req) {
		getRobot().setEnabled(true);
		sendStatusResponse(req, true);
	}

	/**
	 * Calls <code>setEnabled(false)</code> on the hosted Robot.  Sends a
	 * successful RobotStatusResponse.
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleDisableRequest(RobotRequest req) {
		getRobot().setEnabled(false);
		sendStatusResponse(req, true);
	}

	/**
	 * Creates and sends a RobotStatusResponse with the Robot's enabled status
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleEnabledStatusRequest(RobotRequest req) {
		sendStatusResponse(req, getRobot().isEnabled());
	}

	/**
	 * Calls <code>setEnabled(true)</code> on the hosted Robot's Joint.  Sends a
	 * successful RobotStatusResponse.
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleEnableRequestForJoint(RobotRequest req) {
		getRequestedJoint(req).setEnabled(true);
		sendStatusResponse(req, true);
	}

	/**
	 * Calls <code>setEnabled(true)</code> on the hosted Robot's Joint.  Sends a
	 * successful RobotStatusResponse.
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleDisableRequestForJoint(RobotRequest req) {
		getRequestedJoint(req).setEnabled(false);
		sendStatusResponse(req, true);
	}

	/**
	 * Creates and sends a RobotStatusResponse with the Joint's connection
	 * status
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleEnabledStatusRequestForJoint(RobotRequest req) {
		sendStatusResponse(req, getRequestedJoint(req).getEnabled());
	}

	/**
	 * Retrieves the Joint specified in the RobotRequest.
	 *
	 * @param req RobotRequest specifying a Joint
	 * @return Joint specified in the RobotRequest
	 */
	protected Joint getRequestedJoint(RobotRequest req) {
		Integer jIdInt = req.getRequestIndex();
		if (jIdInt == null) {
			throw new NullPointerException();
		}
		Joint.Id jId = new Joint.Id(jIdInt);
		Robot.Id rId = req.getRobotId();
		JointId jointId = new Robot.JointId(rId, jId);
		Joint j = getRobot().getJoint(jointId);
		if (j == null) {
			throw new NullPointerException();
		}
		return j;
	}

	/**
	 * Sends a RobotPositionResponse with the Robot's default positions.
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleDefaultPositionRequest(RobotRequest req) {
		sendPositionResponse(req, getRobot().getDefaultPositions());
	}

	/**
	 * Sends a RobotPositionResponse with the Robot's goal positions.
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleGoalPositionRequest(RobotRequest req) {
		sendPositionResponse(req, getRobot().getGoalPositions());
	}

	/**
	 * Sends a RobotPositionResponse with the Robot's current positions.
	 *
	 * @param req RobotRequest the host is responding to
	 */
	protected void handleCurrentPositionRequest(RobotRequest req) {
		sendPositionResponse(req, getRobot().getCurrentPositions());
	}

	private void sendDefinitionResponse(RobotRequest req) {
		theLogger.trace("Sending Definition Response."
						+ "  Request Timestamp: {}, Type: {}, Source: {}, Dest: {}, Robot: {}.",
				req.getTimestampMillisecUTC(), req.getRequestType(),
				req.getSourceId(), req.getDestinationId(), req.getRobotId());
		if (myResponseSender == null) {
			theLogger.trace("Unable to send Definition Response, missing ResponseSender."
							+ "  Request Timestamp: {}, Type: {}, Source: {}, Dest: {}, Robot: {}.",
					req.getTimestampMillisecUTC(), req.getRequestType(),
					req.getSourceId(), req.getDestinationId(), req.getRobotId());
			return;
		}
		RobotResponseHeader header = getHeader(req);
		Robot robot = getRobot();

		theLogger.trace("Creating Definition Response."
						+ "  Request Timestamp: {}, Type: {}, Source: {}, Dest: {}, Robot: {}."
						+ "  Using Header: {}, Robot: {}",
				req.getTimestampMillisecUTC(), req.getRequestType(),
				req.getSourceId(), req.getDestinationId(), req.getRobotId(),
				header, robot);
		RobotDefinitionResponse def =
				myResponseFactory.createDefinitionResponse(header, robot);
		myResponseSender.notifyListeners(def);
		theLogger.trace("Definition Response Sent."
						+ "  Request Timestamp: {}, Type: {}, Source: {}, Dest: {}, Robot: {}.",
				req.getTimestampMillisecUTC(), req.getRequestType(),
				req.getSourceId(), req.getDestinationId(), req.getRobotId());
	}

	private void sendStatusResponse(RobotRequest req, boolean value) {
		if (myResponseSender == null) {
			return;
		}
		myResponseSender.notifyListeners(
				myResponseFactory.createStatusResponse(
						getHeader(req),
						value));
	}

	private void sendPositionResponse(
			RobotRequest req, RobotPositionMap positions) {
		if (myResponseSender == null) {
			return;
		}
		myResponseSender.notifyListeners(
				myResponseFactory.createPositionResponse(
						getHeader(req),
						positions));
	}

	private RobotResponseHeader getHeader(RobotRequest req) {
		return myResponseFactory.createHeader(
				getRobotId(), mySourceId, myDestinationId,
				req.getRequestType(), req.getTimestampMillisecUTC());
	}

	private RobotResponseHeader getHeader() {
		return myResponseFactory.createHeader(
				getRobotId(), mySourceId, myDestinationId,
				RobotRequest.CMD_GET_ROBOT_DEFINITION, TimeUtils.now());
	}

	class RequestListener implements Listener<RobotRequest> {

		@Override
		public void handleEvent(RobotRequest event) {
			String reqType = event.getRequestType();
			if (reqType == null) {
				theLogger.trace("Received RobotRequest with null RequestType.");
			} else {
				theLogger.info("Received RobotRequest with RequestType: {}.", reqType);
			}
			if (reqType.equals(RobotRequest.CMD_GET_ROBOT_DEFINITION)) {
				handleDefinitionRequest(event);
			} else if (reqType.equals(RobotRequest.CMD_CONNECT_ROBOT)) {
				handleConnectRequest(event);
			} else if (reqType.equals(RobotRequest.CMD_DISCONNECT_ROBOT)) {
				handleDisconnectRequest(event);
			} else if (reqType.equals(RobotRequest.CMD_GET_CONNECTION_STATUS)) {
				handleConnectionStatusRequest(event);
			} else if (reqType.equals(RobotRequest.CMD_ENABLE_ROBOT)) {
				handleEnableRequest(event);
			} else if (reqType.equals(RobotRequest.CMD_DISABLE_ROBOT)) {
				handleDisableRequest(event);
			} else if (reqType.equals(RobotRequest.CMD_GET_ENABLED_STATUS)) {
				handleEnabledStatusRequest(event);
			} else if (reqType.equals(RobotRequest.CMD_GET_DEFAULT_POSITIONS)) {
				handleDefaultPositionRequest(event);
			} else if (reqType.equals(RobotRequest.CMD_GET_GOAL_POSITIONS)) {
				handleGoalPositionRequest(event);
			} else if (reqType.equals(RobotRequest.CMD_GET_CURRENT_POSITIONS)) {
				handleGoalPositionRequest(event);
			} else if (reqType.equals(RobotRequest.CMD_ENABLE_JOINT)) {
				handleEnableRequestForJoint(event);
			} else if (reqType.equals(RobotRequest.CMD_DISABLE_JOINT)) {
				handleDisableRequestForJoint(event);
			} else if (reqType.equals(RobotRequest.CMD_GET_JOINT_ENABLED_STATUS)) {
				handleEnabledStatusRequestForJoint(event);
			} else {
				theLogger.warn("Received unknown request type: {}", reqType);
			}
		}
	}

	class DefinitionPusher implements Runnable {
		@Override
		public void run() {
			long start;
			while (myPusherActive) {
				start = TimeUtils.now();
				theLogger.trace("Sending Definition Response.");
				if (myDefSender == null) {
					theLogger.trace("Unable to send Definition Response, missing DefSender.");
					return;
				}
				RobotResponseHeader header = getHeader();
				Robot robot = getRobot();

				theLogger.trace("Creating Definition Response.");
				RobotDefinitionResponse def =
						myResponseFactory.createDefinitionResponse(
								header, robot);
				myDefSender.notifyListeners(def);
				theLogger.trace("Definition Response Sent.");
				long now = TimeUtils.now();
				long elapsed = now - start;
				long sleep = mySendDataIntervalMillisec - elapsed;
				if (sleep > 0) {
					TimeUtils.sleep(sleep);
				}
			}
		}
	}
}
