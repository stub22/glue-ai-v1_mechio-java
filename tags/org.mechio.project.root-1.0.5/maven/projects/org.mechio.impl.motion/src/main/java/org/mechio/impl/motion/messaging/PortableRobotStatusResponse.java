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
import org.mechio.api.motion.protocol.RobotResponse.RobotStatusResponse;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableRobotStatusResponse implements RobotStatusResponse{
    private RobotStatusResponseRecord myRecord;
    private PortableRobotResponseHeader myCachedHeader;
    
    public PortableRobotStatusResponse(RobotStatusResponseRecord record){
        if(record == null){
            throw new NullPointerException();
        }
        myRecord = record;
        myCachedHeader = 
                new PortableRobotResponseHeader(myRecord.getResponseHeader());
    }
    
    public PortableRobotStatusResponse(RobotStatusResponse response){
        if(response == null){
            throw new NullPointerException();
        }else if(response instanceof PortableRobotStatusResponse){
            PortableRobotStatusResponse pr = 
                    ((PortableRobotStatusResponse)response);
            myRecord = pr.getRecord();
            myCachedHeader = pr.myCachedHeader;
            return;
        }
        setHeader(response.getResponseHeader());
        myRecord = new RobotStatusResponseRecord();
        myRecord.setResponseHeader(myCachedHeader.getRecord());
    }
    
    public PortableRobotStatusResponse(
            RobotResponseHeader header, boolean status){
        if(header == null){
            throw new NullPointerException();
        }
        setHeader(header);
        myRecord = new RobotStatusResponseRecord();
        myRecord.setStatusResponse(status);
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
    public boolean getStatusResponse() {
        return myRecord.getStatusResponse();
    }
    
    public RobotStatusResponseRecord getRecord(){
        return myRecord;
    }
    
    public static class MessageRecordAdapter implements 
            Adapter<RobotStatusResponse,RobotStatusResponseRecord>{

        @Override
        public RobotStatusResponseRecord adapt(RobotStatusResponse a) {
            if(a == null){
                throw new NullPointerException();
            }
            return new PortableRobotStatusResponse(a).getRecord();
        }
    }
    
    public static class RecordMessageAdapter implements
            Adapter<RobotStatusResponseRecord, RobotStatusResponse>{
        @Override
        public RobotStatusResponse adapt(RobotStatusResponseRecord a) {
            return new PortableRobotStatusResponse(a);
        }
    }
}
