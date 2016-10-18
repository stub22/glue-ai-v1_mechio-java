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
package org.mechio.impl.motion.messaging;

import org.jflux.api.core.Adapter;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.Id;
import org.mechio.api.motion.Robot.JointId;
import org.mechio.api.motion.messaging.RobotRequestFactory;
import org.mechio.api.motion.protocol.RobotRequest;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableRobotRequest implements RobotRequest{
    private RobotRequestRecord myRecord;
    private Robot.Id myCachedId;
    
    public PortableRobotRequest(RobotRequestRecord record){
        if(record == null){
            throw new NullPointerException();
        }
        myRecord = record;
        myCachedId = new Robot.Id(myRecord.getRobotId());
    }
    
    public PortableRobotRequest(RobotRequest request){
        if(request == null){
            throw new NullPointerException();
        }else if(request instanceof PortableRobotRequest){
            myRecord = ((PortableRobotRequest)request).getRecord();
            myCachedId = new Robot.Id(myRecord.getRobotId());
            return;
        }
        myCachedId = request.getRobotId();
        myRecord = new RobotRequestRecord();
        myRecord.setRobotId(myCachedId.getRobtIdString());
        myRecord.setRequestSourceId(request.getSourceId());
        myRecord.setRequestDestinationId(request.getDestinationId());
        myRecord.setRequestType(request.getRequestType());
        myRecord.setTimestampMillisecUTC(request.getTimestampMillisecUTC());
    }
    
    public PortableRobotRequest(
            Robot.Id robotId, String sourceId, String destId, 
            String requestType, long timestampMillisecUTC,
            Integer requestIndex){
        myCachedId = robotId;
        myRecord = new RobotRequestRecord();
        myRecord.setRobotId(myCachedId.getRobtIdString());
        myRecord.setRequestSourceId(sourceId);
        myRecord.setRequestDestinationId(destId);
        myRecord.setRequestType(requestType);
        myRecord.setTimestampMillisecUTC(timestampMillisecUTC);
        myRecord.setIntParam(requestIndex);
    }
    
    @Override
    public Id getRobotId() {
        return myCachedId;
    }

    @Override
    public String getSourceId() {
        return myRecord.getRequestSourceId();
    }

    @Override
    public String getDestinationId() {
        return myRecord.getRequestDestinationId();
    }

    @Override
    public String getRequestType() {
        return myRecord.getRequestType();
    }

    @Override
    public long getTimestampMillisecUTC() {
       return myRecord.getTimestampMillisecUTC();
    }

    @Override
    public Integer getRequestIndex() {
        return myRecord.getIntParam();
    }
    
    public RobotRequestRecord getRecord(){
        return myRecord;
    }
    
        public static class Factory implements 
                RobotRequestFactory<PortableRobotRequest> {

        @Override
        public PortableRobotRequest buildRobotRequest(Robot.Id robotId, 
                String sourceId, String destId, 
                String requestType, long timestampMillisecUTC) {
            return new PortableRobotRequest(robotId, sourceId, destId, 
                    requestType, timestampMillisecUTC, null);
        }

        @Override
        public PortableRobotRequest buildJointRequest(
                JointId jointId, String sourceId, String destId, 
                String requestType, long timestampMillisecUTC) {
            Robot.Id robotId = jointId.getRobotId();
            Integer jointIdInt = jointId.getJointId().getLogicalJointNumber();
            return new PortableRobotRequest(robotId, sourceId, destId, 
                    requestType, timestampMillisecUTC, jointIdInt);
        }

    }
    
    public static class MessageRecordAdapter implements 
            Adapter<RobotRequest,RobotRequestRecord>{

        @Override
        public RobotRequestRecord adapt(RobotRequest a) {
            if(a == null){
                throw new NullPointerException();
            }
            return new PortableRobotRequest(a).getRecord();
        }
    }
    
    public static class RecordMessageAdapter implements
            Adapter<RobotRequestRecord, RobotRequest>{
        @Override
        public RobotRequest adapt(RobotRequestRecord a) {
            return new PortableRobotRequest(a);
        }
    }
    
}
