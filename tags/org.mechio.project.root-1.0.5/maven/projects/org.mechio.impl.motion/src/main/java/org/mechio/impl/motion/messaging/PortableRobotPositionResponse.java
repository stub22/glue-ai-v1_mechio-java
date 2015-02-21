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
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.protocol.RobotResponse.RobotPositionResponse;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableRobotPositionResponse implements RobotPositionResponse{
    private RobotPositionResponseRecord myRecord;
    private PortableRobotResponseHeader myCachedHeader;
    private RobotPositionMap myCachedPositions;
    
    public PortableRobotPositionResponse(RobotPositionResponseRecord record){
        if(record == null){
            throw new NullPointerException();
        }
        myRecord = record;
        myCachedHeader = 
                new PortableRobotResponseHeader(myRecord.getResponseHeader());
        myCachedPositions = MotionMessagingUtils.unpackPositionMap(
                myRecord.getPositionResponse());
    }
    
    public PortableRobotPositionResponse(RobotPositionResponse response){
        if(response == null){
            throw new NullPointerException();
        }else if(response instanceof PortableRobotPositionResponse){
            PortableRobotPositionResponse pr = 
                    ((PortableRobotPositionResponse)response);
            myRecord = pr.getRecord();
            myCachedHeader = pr.myCachedHeader;
            return;
        }
        setHeader(response.getResponseHeader());
        myCachedPositions = response.getPositionMap();
        myRecord = new RobotPositionResponseRecord();
        myRecord.setResponseHeader(myCachedHeader.getRecord());
        myRecord.setPositionResponse(
                MotionMessagingUtils.packRobotPositionMap(myCachedPositions));
    }
    
    public PortableRobotPositionResponse(
            RobotResponseHeader header, RobotPositionMap positions){
        setHeader(header);
        myCachedPositions = positions;
        myRecord = new RobotPositionResponseRecord();
        myRecord.setPositionResponse(
                MotionMessagingUtils.packRobotPositionMap(positions));
        myRecord.setResponseHeader(myCachedHeader.getRecord());
    }
    
    private void setHeader(RobotResponseHeader header){
        if(header instanceof PortableRobotResponseHeader){
            myCachedHeader = (PortableRobotResponseHeader)header;
        }else{
            myCachedHeader = new PortableRobotResponseHeader(header);
        }
    }

    @Override
    public RobotResponseHeader getResponseHeader() {
        return myCachedHeader;
    }
    
    @Override
    public RobotPositionMap getPositionMap() {
        return myCachedPositions;
    }
    
    public RobotPositionResponseRecord getRecord(){
        return myRecord;
    }
    
    public static class MessageRecordAdapter implements 
            Adapter<RobotPositionResponse,RobotPositionResponseRecord>{

        @Override
        public RobotPositionResponseRecord adapt(RobotPositionResponse a) {
            if(a == null){
                throw new NullPointerException();
            }
            return new PortableRobotPositionResponse(a).getRecord();
        }
    }
    
    public static class RecordMessageAdapter implements
            Adapter<RobotPositionResponseRecord, RobotPositionResponse>{
        @Override
        public RobotPositionResponse adapt(RobotPositionResponseRecord a) {
            return new PortableRobotPositionResponse(a);
        }
    }
}
