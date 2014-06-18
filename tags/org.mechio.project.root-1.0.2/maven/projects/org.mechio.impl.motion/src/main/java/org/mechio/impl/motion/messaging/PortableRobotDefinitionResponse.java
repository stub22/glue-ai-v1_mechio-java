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

import java.util.ArrayList;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Array;
import org.jflux.api.core.Adapter;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableRobotDefinitionResponse implements RobotDefinitionResponse{
    private RobotDefinitionResponseRecord myRecord;
    private PortableRobotResponseHeader myCachedHeader;
    private List<PortableJointDefinition> myCachedJointDefinitions;
    
    public PortableRobotDefinitionResponse(RobotDefinitionResponseRecord record){
        if(record == null){
            throw new NullPointerException();
        }
        myRecord = record;
        myCachedHeader = 
                new PortableRobotResponseHeader(myRecord.getResponseHeader());
        myCachedJointDefinitions = new ArrayList<PortableJointDefinition>();
        for(JointDefinitionRecord rec : myRecord.getJoints()){
            myCachedJointDefinitions.add(new PortableJointDefinition(rec));
        }
    }
    
    public PortableRobotDefinitionResponse(RobotDefinitionResponse response){
        if(response == null){
            throw new NullPointerException();
        }else if(response instanceof PortableRobotDefinitionResponse){
            PortableRobotDefinitionResponse pr = 
                    ((PortableRobotDefinitionResponse)response);
            myRecord = pr.getRecord();
            myCachedHeader = pr.myCachedHeader;
            myCachedJointDefinitions = pr.myCachedJointDefinitions;
            return;
        }
        setHeader(response.getResponseHeader());
        myRecord = new RobotDefinitionResponseRecord();
        myRecord.setResponseHeader(myCachedHeader.getRecord());
        myRecord.setConnected(response.getConnected());
        myRecord.setEnabled(response.getEnabled());
        int count = response.getJointDefinitions().size();
        myCachedJointDefinitions = new ArrayList(count);
        myRecord.setJoints(new Array(
                count, Schema.createArray(JointDefinitionRecord.SCHEMA$)));
        for(JointDefinition def : response.getJointDefinitions()){
            PortableJointDefinition pjd = new PortableJointDefinition(def);
            myCachedJointDefinitions.add(pjd);
            myRecord.getJoints().add(pjd.getRecord());
        }
    }
    
    public PortableRobotDefinitionResponse(
            RobotResponseHeader header, boolean connected, boolean enabled, List<JointDefinition> jointDefs){
        setHeader(header);
        myRecord = new RobotDefinitionResponseRecord();
        myRecord.setResponseHeader(myCachedHeader.getRecord());
        myRecord.setConnected(connected);
        myRecord.setEnabled(enabled);
        int count = jointDefs.size();
        myCachedJointDefinitions = new ArrayList(count);
        myRecord.setJoints(new Array(
                count, Schema.createArray(JointDefinitionRecord.SCHEMA$)));
        for(JointDefinition def : jointDefs){
            PortableJointDefinition pjd = new PortableJointDefinition(def);
            myCachedJointDefinitions.add(pjd);
            myRecord.getJoints().add(pjd.getRecord());
        }
    }
    
    public PortableRobotDefinitionResponse(
            RobotResponseHeader header, Robot<? extends Joint> robot){
        if(header == null || robot == null){
            throw new NullPointerException();
        }
        setHeader(header);
        myRecord = new RobotDefinitionResponseRecord();
        myRecord.setResponseHeader(myCachedHeader.getRecord());
        myRecord.setConnected(robot.isConnected());
        myRecord.setEnabled(robot.isEnabled());
        int count = robot.getJointList().size();
        myCachedJointDefinitions = new ArrayList(count);
        myRecord.setJoints(new Array(
                count, Schema.createArray(JointDefinitionRecord.SCHEMA$)));
        for(Joint joint : robot.getJointList()){
            PortableJointDefinition pjd = new PortableJointDefinition(joint);
            myCachedJointDefinitions.add(pjd);
            myRecord.getJoints().add(pjd.getRecord());
        }
    }
    
    private void setHeader(RobotResponseHeader header){
        if(header instanceof PortableRobotResponseHeader){
            myCachedHeader = (PortableRobotResponseHeader)header;
        }else{
            myCachedHeader = new PortableRobotResponseHeader(header);
        }
    }
    
    @Override
    public boolean getConnected() {
        return myRecord.getConnected();
    }

    @Override
    public boolean getEnabled() {
        return myRecord.getEnabled();
    }

    @Override
    public List<JointDefinition> getJointDefinitions() {
        return (List)myCachedJointDefinitions;
    }

    @Override
    public RobotResponseHeader getResponseHeader() {
        return myCachedHeader;
    }
    
    public RobotDefinitionResponseRecord getRecord(){
        return myRecord;
    }
    
    public static class MessageRecordAdapter implements 
            Adapter<RobotDefinitionResponse,RobotDefinitionResponseRecord>{

        @Override
        public RobotDefinitionResponseRecord adapt(RobotDefinitionResponse a) {
            if(a == null){
                throw new NullPointerException();
            }
            return new PortableRobotDefinitionResponse(a).getRecord();
        }
    }
    
    public static class RecordMessageAdapter implements
            Adapter<RobotDefinitionResponseRecord, RobotDefinitionResponse>{
        @Override
        public RobotDefinitionResponse adapt(RobotDefinitionResponseRecord a) {
            return new PortableRobotDefinitionResponse(a);
        }
    }
}
