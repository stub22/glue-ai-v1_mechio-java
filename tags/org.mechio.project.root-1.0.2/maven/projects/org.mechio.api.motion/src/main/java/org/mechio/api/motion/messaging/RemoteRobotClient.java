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

import java.util.logging.Level;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;
import org.mechio.api.motion.protocol.RobotRequest;
import java.util.logging.Logger;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.messaging.rk.MessageBlockingReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.JointId;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.protocol.RobotResponse;
import org.mechio.api.motion.protocol.RobotResponse.RobotPositionResponse;
import org.mechio.api.motion.protocol.RobotResponse.RobotResponseHeader;
import org.mechio.api.motion.protocol.RobotResponse.RobotStatusResponse;
import org.mechio.api.motion.protocol.MotionFrame;
import org.mechio.api.motion.protocol.MotionFrameEvent;
import org.mechio.api.motion.protocol.MotionFrameEvent.MotionFrameEventFactory;

/**
 * Client for messaging with a RemoteRobotHost.
 * Used by RemoteRobot to control a Robot through a messaging channel.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteRobotClient {
    private final static Logger theLogger = 
            Logger.getLogger(RemoteRobotClient.class.getName());
    /**
     * Default number of milliseconds before a request times out.
     */
    public final static int DEFAULT_TIMEOUT_LENGTH = 20000;
    
    private Robot.Id myRobotId;
    private String mySourceId;
    private String myDestinationId;
    private RobotRequestFactory myRequestFactory;
    private MotionFrameEventFactory myMotionFrameAdapter;
    private MessageSender<RobotRequest> myRequestSender;
    private MessageBlockingReceiver<RobotResponse> myResponseReceiver;
    private MessageSender<MotionFrameEvent>  myMotionFrameSender;
    private boolean myActive;
    
    /**
     * Creates a new RemoteRobotClient.
     * @param robotId id of the remote robot
     * @param sourceId arbitrary String to identify this client
     * @param destId arbitrary String to identify the host
     * @param reqFact factory used for creating new RobotRequests
     */
    public RemoteRobotClient(Robot.Id robotId, 
            String sourceId, String destId, RobotRequestFactory reqFact,
            MotionFrameEventFactory motionFrameEventFactory){
        if(robotId == null || sourceId == null || destId == null || 
                reqFact == null || motionFrameEventFactory == null){
            throw new NullPointerException();
        }
        myRobotId = robotId;
        mySourceId = sourceId;
        myDestinationId = destId;
        myRequestFactory = reqFact;
        myMotionFrameAdapter = motionFrameEventFactory;
        myActive = true;
    }
    /**
     * Sets the MessageSender to send RobotRequests.
     * @param reqSender MessageSender to use
     */
    public void setRequestSender(MessageSender<RobotRequest> reqSender){
        if(!myActive){
            return;
        }
        if(reqSender == null){
            throw new NullPointerException();
        }
        myRequestSender = reqSender;
    }
    /**
     * Sets the MessageReceiver to receive RobotResponses.
     * @param respRec MessageReceiver to use
     */
    public void setResponseReceiver(
            MessageBlockingReceiver< RobotResponse> respRec){
        if(!myActive){
            return;
        }        
        if(respRec == null){
            throw new NullPointerException();
        }
        myResponseReceiver = respRec;
    }
    /**
     * Sets the MotionFrameSender to use for sending MotionFrames to the host.
     * @param frameSender MotionFrameSender to use
     */
    public void setMotionFrameSender(
            MessageSender<MotionFrameEvent> frameSender){
        if(!myActive){
            return;
        }
        if(frameSender == null){
            throw new NullPointerException();
        }
        myMotionFrameSender = frameSender;
    }
    /**
     * Returns the id of the remote Robot
     * @return 
     */
    public Robot.Id getRobotId(){
        return myRobotId;
    }
    /**
     * Returns the String identifying this client, currently unused.
     * @return String identifying this client
     */
    public String getSourceId(){
        return mySourceId;
    }
    /**
     * Returns the String identifying the host, currently unused.
     * @return String identifying the host
     */
    public String getDestinationId(){
        return myDestinationId;
    }
    /**
     * Requests a RobotDefinition from the host.
     * @return RobotDefinition defining the remote robot
     */
    public RobotDefinitionResponse requestRobotDefinition(){
        if(!myActive){
            return null;
        }
        return makeDefinitionRequest(
                RobotRequest.CMD_GET_ROBOT_DEFINITION, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Sends a connect command to the remote Robot.
     * @return true if successful
     */
    public boolean sendConnect(){
        if(!myActive){
            return false;
        }
        return makeStatusRequest(
                RobotRequest.CMD_CONNECT_ROBOT, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Sends a disconnect command to the remote Robot.
     * @return true if successful
     */
    public boolean sendDisconnect(){
        if(!myActive){
            return false;
        }
        return makeStatusRequest(
                RobotRequest.CMD_DISCONNECT_ROBOT, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Requests the remote Robot's connection status.
     * @return remote Robot's connection status
     */
    public boolean getConnected(){
        if(!myActive){
            return false;
        }
        return makeStatusRequest(
                RobotRequest.CMD_GET_CONNECTION_STATUS, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Sends an enable command to the remote Robot.
     * @return true if successful
     */
    public boolean sendEnable(){
        if(!myActive){
            return false;
        }
        return makeStatusRequest(
                RobotRequest.CMD_ENABLE_ROBOT, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Sends an disable command to the remote Robot.
     * @return true if successful
     */
    public boolean sendDisable(){
        if(!myActive){
            return false;
        }
        return makeStatusRequest(
                RobotRequest.CMD_DISABLE_ROBOT, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Requests the remote Robot's enabled status.
     * @return remote Robot's enabled status
     */
    public boolean getEnabled(){
        if(!myActive){
            return false;
        }
        return makeStatusRequest(
                RobotRequest.CMD_GET_ENABLED_STATUS, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Sends an disable command to a remote Joint.
     * @param jointId id of the remote Joint
     * @return true if successful
     */
    public boolean sendJointEnable(JointId jointId){
        if(!myActive){
            return false;
        }
        return makeStatusRequestForJoint(
                RobotRequest.CMD_ENABLE_JOINT, jointId, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Sends an disable command to a remote Joint.
     * @param jointId id of the remote Joint
     * @return true if successful
     */
    public boolean sendJointDisable(JointId jointId){
        if(!myActive){
            return false;
        }
        return makeStatusRequestForJoint(
                RobotRequest.CMD_DISABLE_JOINT, 
                jointId, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Requests the remote  Joint's enabled status.
     * @param jointId id of the remote Joint
     * @return remote Robot's enabled status
     */
    public boolean getJointEnabled(JointId jointId){
        return makeStatusRequestForJoint(
                RobotRequest.CMD_GET_JOINT_ENABLED_STATUS, 
                jointId, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Requests the remote Robot's default joint positions.
     * @return remote Robot's default joint positions
     */
    public RobotPositionMap requestDefaultPositions(){
        if(!myActive){
            return null;
        }
        return makePositionRequest(
                RobotRequest.CMD_GET_DEFAULT_POSITIONS, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Requests the remote Robot's goal positions.
     * @return remote Robot's goal positions
     */
    public RobotPositionMap requestGoalPositions(){
        if(!myActive){
            return null;
        }
        return makePositionRequest(
                RobotRequest.CMD_GET_GOAL_POSITIONS, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Requests the remote Robot's current positions.
     * @return remote Robot's current positions
     */
    public RobotPositionMap requestCurrentPositions(){
        if(!myActive){
            return null;
        }
        return makePositionRequest(
                RobotRequest.CMD_GET_CURRENT_POSITIONS, DEFAULT_TIMEOUT_LENGTH);
    }
    /**
     * Sends a MotionFrame to move a RemoteRobot.
     * @param frame MotionFrame to send
     */
    public void sendMovement(MotionFrame frame){
        if(!myActive){
            return;
        }
        MotionFrameEvent mfe = myMotionFrameAdapter.createMotionFrameEvent(
                mySourceId, myDestinationId, frame);
        myMotionFrameSender.notifyListeners(mfe);
    }
    
    public void shutDown() {
        myActive = false;
    }
    
    private Boolean makeStatusRequest(String requestType, long timeout){
        RobotStatusResponse resp = makeBlockingRequest(
            RobotStatusResponse.class, requestType, timeout);
        if(resp == null){
            theLogger.log(Level.WARNING, "Received null status for: {0}, from: {1}", 
                    new Object[]{requestType, myRobotId.getRobtIdString()});
            return null;
        }
        return resp.getStatusResponse();
    }
    
    private Boolean makeStatusRequestForJoint(String requestType, JointId jointId, long timeout){
        RobotStatusResponse resp = makeBlockingRequestForJoint(
            requestType, jointId, timeout);
        if(resp == null){
            theLogger.log(Level.WARNING, "Received null positions for: {0}, from: {1}", 
                    new Object[]{requestType, myRobotId.getRobtIdString()});
            return null;
        }
        return resp.getStatusResponse();
    }
    
    private RobotPositionMap makePositionRequest(
            String requestType, long timeout){
        RobotPositionResponse resp = makeBlockingRequest(
                RobotPositionResponse.class, requestType, timeout);
        if(resp == null){
            theLogger.log(Level.WARNING, "Received null positions for: {0}, from: {1}", 
                    new Object[]{requestType, myRobotId.getRobtIdString()});
            return null;
        }
        return resp.getPositionMap();
    }
    
    private RobotDefinitionResponse makeDefinitionRequest(
            String requestType, long timeout){
        theLogger.log(Level.INFO, "Making Robot Definition Request.  "
                + "Request Type: {0}, Timeout: {1}, Robot: {2}", 
                new Object[]{requestType, timeout, myRobotId.getRobtIdString()});
        RobotDefinitionResponse resp = makeBlockingRequest(
                RobotDefinitionResponse.class, requestType, timeout);
        if(resp == null){
            theLogger.log(Level.WARNING, "Received null definition for: {0}, from: {1}", 
                    new Object[]{requestType, myRobotId.getRobtIdString()});
            return null;
        }
        return resp;
    }
    
    private synchronized <T extends RobotResponse> T makeBlockingRequest(
            Class<T> responseClass, String requestType, long timeout){
        if(responseClass == null || requestType == null){
            throw new NullPointerException();
        }
        int clearedCount = myResponseReceiver.clearMessages();
        if(clearedCount > 0){
            theLogger.log(Level.INFO, 
                    "Cleared {3} messages before making blocking request.  "
                    + "Request Type: {0}, Timeout: {1}, Robot: {2}, Response Class: {4}", 
                    new Object[]{requestType, timeout, myRobotId.getRobtIdString(), 
                        clearedCount, responseClass});
        }
        RobotRequest req = myRequestFactory.buildRobotRequest(
                myRobotId, mySourceId, myDestinationId, 
                requestType, TimeUtils.now());
        myRequestSender.notifyListeners(req);
            theLogger.log(Level.INFO, 
                    "Robot Request Sent.  Fetching Response.  "
                    + "Request Type: {0}, Timeout: {1}, Robot: {2}, Response Class: {4}", 
                    new Object[]{requestType, timeout, myRobotId.getRobtIdString(), 
                        clearedCount, responseClass});
        return fetchTypedResponse(responseClass, req, timeout);
    }
    
    private synchronized RobotStatusResponse makeBlockingRequestForJoint(
            String requestType, JointId jointId, long timeout){
        if(requestType == null){
            throw new NullPointerException();
        }
        RobotRequest req = myRequestFactory.buildJointRequest(
                jointId, mySourceId, myDestinationId, 
                requestType, TimeUtils.now());
        myRequestSender.notifyListeners(req);
        return fetchTypedResponse(RobotStatusResponse.class, req, timeout);
    }
    
    private <T extends RobotResponse> T fetchTypedResponse(
            Class<T> clazz, RobotRequest request, long timeout){
        long start = TimeUtils.now();
        do{
            RobotResponse resp = 
                    (RobotResponse)myResponseReceiver.getValue();
            if(resp == null){
                theLogger.warning("Received null Message from Receiver");
            }else if(!isMatch(request, resp.getResponseHeader())){
                theLogger.warning("Response does not match Request.  Ignoring response.");
            }else if(clazz.isAssignableFrom(resp.getClass())){
                return (T)resp;
            }else{
                theLogger.log(Level.INFO, 
                        "Requested class ({0}) does not Response class ({1}).",
                        new Object[]{clazz, resp.getClass()});
            }
            long elapsed = TimeUtils.now() - start;
            timeout -= elapsed;
        }while(timeout > 0);
        return null;
    }
    
    private boolean isMatch(RobotRequest req, RobotResponseHeader resp){
        if(req == null || resp == null){
            theLogger.info("Received null request or response header, unable to determine match.");
            throw new NullPointerException();
        }
        if(!req.getRobotId().equals(resp.getRobotId())){
            theLogger.log(Level.INFO, 
                    "Requested Robot Id ({0}) does not match Response Robot Id ({1}).",
                    new Object[]{req.getRobotId(), resp.getRobotId()});
            return false;
        }else if(!req.getRequestType().equals(resp.getRequestType())){
            theLogger.log(Level.INFO, 
                    "Request Type ({0}) does not match Response Type ({1}).",
                    new Object[]{req.getRequestType(), resp.getRequestType()});
            return false;
        /*}else if(!req.getSourceId().equals(resp.getDestinationId())){
            return false;
        }else if(!req.getDestinationId().equals(resp.getSourceId())){
            return false;
        */}else if(req.getTimestampMillisecUTC() != resp.getRequestTimestampMillisecUTC()){
            theLogger.log(Level.INFO, 
                    "Request Timestamp ({0}) does not match Response Request Timestamp ({1}).",
                    new Object[]{req.getTimestampMillisecUTC(), 
                        resp.getRequestTimestampMillisecUTC()});
            return false;
        }
        return true;
    }
}
