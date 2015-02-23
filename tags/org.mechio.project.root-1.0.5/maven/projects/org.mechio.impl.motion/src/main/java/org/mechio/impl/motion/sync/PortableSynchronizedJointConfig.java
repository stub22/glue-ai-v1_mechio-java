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

import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.sync.SynchronizedJointConfig;
import org.mechio.impl.motion.messaging.SynchronizedJointConfigRecord;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableSynchronizedJointConfig implements SynchronizedJointConfig{
    private SynchronizedJointConfigRecord myRecord;
    private Joint.Id myCachedId;
    private NormalizedDouble myCachedDefPos;
    
    public PortableSynchronizedJointConfig(SynchronizedJointConfigRecord record){
        if(record == null){
            throw new NullPointerException();
        }
        setRecord(record);
    }
    
    public PortableSynchronizedJointConfig(SynchronizedJointConfig conf){
        if(conf == null){
            throw new NullPointerException();
        }else if(conf instanceof PortableSynchronizedJointConfig){
            setRecord(((PortableSynchronizedJointConfig)conf).getRecord());
            return;
        }
        myRecord = new SynchronizedJointConfigRecord();
        myRecord.setJointId(conf.getJointId().getLogicalJointNumber());
        myRecord.setName(conf.getName());
        NormalizedDouble def = conf.getDefaultPosition();
        if(def != null){
            myRecord.setDefaultPosition(def.getValue());
        }
        setRecord(myRecord);
    }
    
    public PortableSynchronizedJointConfig(Joint.Id jointId, String name, NormalizedDouble defPos){
        if(jointId == null || name == null){
            throw new NullPointerException();
        }
        myCachedId = jointId;
        myCachedDefPos = defPos;
        myRecord = new SynchronizedJointConfigRecord();
        myRecord.setJointId(myCachedId.getLogicalJointNumber());
        myRecord.setName(name);
        if(myCachedDefPos != null){
            myRecord.setDefaultPosition(myCachedDefPos.getValue());
        }
    }
    
    private void setRecord(SynchronizedJointConfigRecord record){
        myRecord = record;
        myCachedId = new Joint.Id(myRecord.getJointId());
        Double def = myRecord.getDefaultPosition();
        if(def != null){
            myCachedDefPos = new NormalizedDouble(def);
        }
    }
    
    @Override
    public Joint.Id getJointId() {
        return myCachedId;
    }

    @Override
    public String getName() {
        return myRecord.getName();
    }

    @Override
    public NormalizedDouble getDefaultPosition() {
        return myCachedDefPos;
    }
    
    public SynchronizedJointConfigRecord getRecord(){
        return myRecord;
    }
}
