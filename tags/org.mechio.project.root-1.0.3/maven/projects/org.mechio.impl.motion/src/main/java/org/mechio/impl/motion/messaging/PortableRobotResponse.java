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

import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Adapter;
import org.jflux.impl.messaging.rk.common.PolymorphicAdapter.AdapterKeyMap;
import org.jflux.impl.messaging.rk.utils.JMSAvroPolymorphicBytesRecordAdapter;
import org.jflux.impl.messaging.rk.utils.JMSAvroPolymorphicRecordBytesAdapter;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.messaging.RobotResponseFactory;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;
import org.mechio.api.motion.protocol.RobotResponse;
import org.mechio.api.motion.protocol.RobotResponse.RobotPositionResponse;
import org.mechio.api.motion.protocol.RobotResponse.RobotResponseHeader;
import org.mechio.api.motion.protocol.RobotResponse.RobotStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableRobotResponse {
    private final static Logger theLogger = 
                LoggerFactory.getLogger(PortableRobotResponse.class);
    public final static String MIME_ROBOT_DEFINITION_RESPONSE = "application/avro-robot-def-resp";
    public final static String MIME_ROBOT_STATUS_RESPONSE = "application/avro-robot-stat-resp";
    public final static String MIME_ROBOT_POSITION_RESPONSE = "application/avro-robot-pos-resp";
        
    public static class Factory implements RobotResponseFactory{

        @Override
        public RobotResponseHeader createHeader(Robot.Id robotId, 
                String sourceId, String destinationId, 
                String commandType, long requesTimestamp) {
            return new PortableRobotResponseHeader(
                    robotId, sourceId, destinationId, 
                    commandType, requesTimestamp, TimeUtils.now());
        }

        @Override
        public RobotDefinitionResponse createDefinitionResponse(
                RobotResponseHeader header, Robot robot) {
            return new PortableRobotDefinitionResponse(header, robot);
        }

        @Override
        public RobotStatusResponse createStatusResponse(
                RobotResponseHeader header, boolean status) {
            return new PortableRobotStatusResponse(header, status);
        }

        @Override
        public RobotPositionResponse createPositionResponse(
                RobotResponseHeader header, RobotPositionMap positions) {
            return new PortableRobotPositionResponse(header, positions);
        }

    }
    
    public static class RecordMessageAdapter extends 
            JMSAvroPolymorphicBytesRecordAdapter<RobotResponse>{
        public RecordMessageAdapter(){
            addAdapter(RobotDefinitionResponseRecord.class, 
                    RobotDefinitionResponseRecord.SCHEMA$, (Adapter)
                    new PortableRobotDefinitionResponse.RecordMessageAdapter(), 
                    MIME_ROBOT_DEFINITION_RESPONSE);
            addAdapter(RobotPositionResponseRecord.class, 
                    RobotPositionResponseRecord.SCHEMA$, (Adapter)
                    new PortableRobotPositionResponse.RecordMessageAdapter(), 
                    MIME_ROBOT_POSITION_RESPONSE);
            addAdapter(RobotStatusResponseRecord.class, 
                    RobotStatusResponseRecord.SCHEMA$, (Adapter)
                    new PortableRobotStatusResponse.RecordMessageAdapter(), 
                    MIME_ROBOT_STATUS_RESPONSE);
        }
    }
    
    public static class MessageRecordAdapter 
            extends JMSAvroPolymorphicRecordBytesAdapter<RobotResponse> {

        public MessageRecordAdapter(){
            super(new RobotResponseKeyAdapter());
            addAdapter((Adapter)
                    new PortableRobotDefinitionResponse.MessageRecordAdapter(), 
                    MIME_ROBOT_DEFINITION_RESPONSE);
            addAdapter((Adapter)
                    new PortableRobotStatusResponse.MessageRecordAdapter(), 
                    MIME_ROBOT_STATUS_RESPONSE);
            addAdapter((Adapter)
                    new PortableRobotPositionResponse.MessageRecordAdapter(), 
                    MIME_ROBOT_POSITION_RESPONSE);
        }
        
    }
    
    public static class RobotResponseKeyAdapter implements AdapterKeyMap<RobotResponse>{
        @Override
        public String getKey(RobotResponse t) {
            if(t == null){
                throw new NullPointerException();
            }
            if(t instanceof RobotDefinitionResponse){
                return MIME_ROBOT_DEFINITION_RESPONSE;
            }else if(t instanceof RobotStatusResponse){
                return MIME_ROBOT_STATUS_RESPONSE;
            }else if(t instanceof RobotPositionResponse){
                return MIME_ROBOT_POSITION_RESPONSE;
            }
            return null;
        }
    }
}
