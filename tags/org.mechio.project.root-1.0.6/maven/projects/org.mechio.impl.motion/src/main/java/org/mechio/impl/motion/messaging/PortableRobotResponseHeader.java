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

import org.mechio.api.motion.Robot;
import org.mechio.api.motion.protocol.RobotResponse.RobotResponseHeader;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableRobotResponseHeader implements RobotResponseHeader{
    private RobotResponseHeaderRecord myRecord;
    private Robot.Id myCachedId;
    
    public PortableRobotResponseHeader(RobotResponseHeaderRecord record){
        if(record == null){
            throw new NullPointerException();
        }
        myRecord = record;
        myCachedId = new Robot.Id(myRecord.getRobotId());
    }
    
    public PortableRobotResponseHeader(RobotResponseHeader header){
        if(header == null){
            throw new NullPointerException();
        }else if(header instanceof PortableRobotResponseHeader){
            myRecord = ((PortableRobotResponseHeader)header).getRecord();
            myCachedId = new Robot.Id(myRecord.getRobotId());
            return;
        }
        myCachedId = header.getRobotId();
        myRecord = new RobotResponseHeaderRecord();
        myRecord.setRobotId(myCachedId.getRobtIdString());
        myRecord.setRequestSourceId(header.getSourceId());
        myRecord.setRequestDestinationId(header.getDestinationId());
        myRecord.setRequestType(header.getRequestType());
        myRecord.setRequestTimestampMillisecUTC(
                header.getRequestTimestampMillisecUTC());
        myRecord.setResponseTimestampMillisecUTC(
                header.getResponseTimestampMillisecUTC());
    }
    
    public PortableRobotResponseHeader(
            Robot.Id robotId, String sourceId, String destId, 
            String commandType, long requestTimestampMillisecUTC, 
            long responseTimestampMillisecUTC){
        myCachedId = robotId;
        myRecord = new RobotResponseHeaderRecord();
        myRecord.setRobotId(myCachedId.getRobtIdString());
        myRecord.setRequestSourceId(sourceId);
        myRecord.setRequestDestinationId(destId);
        myRecord.setRequestType(commandType);
        myRecord.setRequestTimestampMillisecUTC(requestTimestampMillisecUTC);
        myRecord.setResponseTimestampMillisecUTC(responseTimestampMillisecUTC);
    }
    
    @Override
    public Robot.Id getRobotId() {
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
    public long getRequestTimestampMillisecUTC() {
       return myRecord.getRequestTimestampMillisecUTC();
    }

    @Override
    public long getResponseTimestampMillisecUTC() {
       return myRecord.getResponseTimestampMillisecUTC();
    }
    
    public RobotResponseHeaderRecord getRecord(){
        return myRecord;
    }
    
}
