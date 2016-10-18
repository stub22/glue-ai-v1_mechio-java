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
package org.mechio.impl.motion.sync;

import java.util.ArrayList;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Array;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.Id;
import org.mechio.api.motion.sync.SynchronizedJointConfig;
import org.mechio.api.motion.sync.SynchronizedRobotConfig;
import org.mechio.impl.motion.messaging.SynchronizedJointConfigRecord;
import org.mechio.impl.motion.messaging.SynchronizedRobotConfigRecord;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableSynchronizedRobotConfig implements SynchronizedRobotConfig{
    private SynchronizedRobotConfigRecord myRecord;
    private Robot.Id myRobotId;
    private List<PortableSynchronizedJointConfig> myCachedJointConfigs;
    
    public PortableSynchronizedRobotConfig(SynchronizedRobotConfigRecord record){
        setRecord(record);
    }
    
    public PortableSynchronizedRobotConfig(SynchronizedRobotConfig conf){
        if(conf == null){
            throw new NullPointerException();
        }
        if(conf instanceof PortableSynchronizedRobotConfig){
            setRecord(((PortableSynchronizedRobotConfig)conf).getRecord());
            return;
        }
        myRecord = new SynchronizedRobotConfigRecord();
        myRecord.setRobotId(conf.getRobotId().getRobtIdString());
        List<SynchronizedJointConfig> joints = conf.getJointConfigs();
        myRecord.setJoints(new Array<SynchronizedJointConfigRecord>(
                joints.size(), 
                Schema.createArray(SynchronizedJointConfigRecord.SCHEMA$)));
        for(SynchronizedJointConfig jc : joints){
            myRecord.getJoints().add(
                    new PortableSynchronizedJointConfig(jc).getRecord());
        }
    }
    
    public PortableSynchronizedRobotConfig(
            Robot.Id robotId, List<PortableSynchronizedJointConfig> jointConfigs){
        if(robotId == null || jointConfigs == null){
            throw new NullPointerException();
        }
        myRobotId = robotId;
        myCachedJointConfigs = jointConfigs;
        myRecord = new SynchronizedRobotConfigRecord();
        myRecord.setRobotId(myRobotId.getRobtIdString());
        myRecord.setJoints(new Array<SynchronizedJointConfigRecord>(
                myCachedJointConfigs.size(), 
                Schema.createArray(SynchronizedJointConfigRecord.SCHEMA$)));
        for(PortableSynchronizedJointConfig jc : myCachedJointConfigs){
            myRecord.getJoints().add(jc.getRecord());
        }
    }
    
    private void setRecord(SynchronizedRobotConfigRecord record){
        if(record == null){
            throw new NullPointerException();
        }
        myRecord = record;
        myRobotId = new Robot.Id(myRecord.getRobotId());
        int count = myRecord.getJoints().size();
        myCachedJointConfigs = new ArrayList(count);
        for(SynchronizedJointConfigRecord jc : myRecord.getJoints()){
            myCachedJointConfigs.add(new PortableSynchronizedJointConfig(jc));
        }
    }
    
    @Override
    public Id getRobotId() {
        return myRobotId;
    }

    @Override
    public List<SynchronizedJointConfig> getJointConfigs() {
        return (List)myCachedJointConfigs;
    }
    
    public SynchronizedRobotConfigRecord getRecord(){
        return myRecord;
    }
    
}
