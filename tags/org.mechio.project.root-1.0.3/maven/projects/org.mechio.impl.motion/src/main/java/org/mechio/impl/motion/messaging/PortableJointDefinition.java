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
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.JointProperty;
import org.mechio.api.motion.protocol.RobotDefinitionResponse.JointDefinition;
import org.mechio.api.motion.protocol.RobotDefinitionResponse.JointPropDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableJointDefinition implements JointDefinition{
    private static Logger theLogger = LoggerFactory.getLogger(PortableJointDefinition.class);
    private JointDefinitionRecord myRecord;
    private Joint.Id myCachedId;
    private NormalizedDouble myCachedDefaultPosition;
    private NormalizedDouble myCachedGoalPosition;
    private List<JointPropDefinition> myPropDefs;
    
    public PortableJointDefinition(JointDefinitionRecord record){
        if(record == null){
            throw new NullPointerException();
        }
        setRecord(record);
        myPropDefs = new ArrayList<JointPropDefinition>(record.getProperties().size());
        myPropDefs.addAll(record.getProperties());
    }
    
    public PortableJointDefinition(JointDefinition jointDef){
        if(jointDef == null){
            throw new NullPointerException();
        }
        myPropDefs = jointDef.getJointProperties();
        if(jointDef instanceof PortableJointDefinition){
            PortableJointDefinition pr = ((PortableJointDefinition)jointDef);
            setRecord(pr.getRecord());
            return;
        }
        setRecord(jointDef.getJointId(), 
                jointDef.getName(), 
                jointDef.getDefaultPosition(), 
                jointDef.getGoalPosition(), 
                jointDef.getEnabled(),
                jointDef.getJointProperties());
    }
    
    public PortableJointDefinition(Joint.Id jId, String name, 
            NormalizedDouble defPos, NormalizedDouble goalPos, boolean enabled,
            List<JointProperty> properties){
        myPropDefs = new ArrayList<JointPropDefinition>(properties.size());
        for(JointProperty prop : properties){
            try{
                myPropDefs.add(defineJointProperty(prop));
            }catch(Exception ex){
                theLogger.warn("Unable to add joint property - joint: " + name + ", prop: " + prop.getPropertyName(), ex);
            }
        }
        setRecord(jId, name, defPos, goalPos, enabled, myPropDefs);
    }
    
    public PortableJointDefinition(Joint joint){
        myPropDefs = new ArrayList<JointPropDefinition>(joint.getProperties().size());
        for(JointProperty prop : joint.getProperties()){
            try{
                myPropDefs.add(defineJointProperty(prop));
            }catch(Exception ex){
                theLogger.warn("Unable to add joint property - joint: " + joint.getName() + ", prop: " + prop.getPropertyName(), ex);
            }
        }
        setRecord(joint.getId(), joint.getName(), 
                joint.getDefaultPosition(), 
                joint.getGoalPosition(), 
                joint.getEnabled(),
                myPropDefs);
    }
    
    private void setRecord(Joint.Id jId, String name, 
            NormalizedDouble defPos, NormalizedDouble goalPos, boolean enabled,
            List<JointPropDefinition> properties){
        JointDefinitionRecord rec = new JointDefinitionRecord();
        rec.setJointId(jId.getLogicalJointNumber());
        rec.setName(name);
        rec.setDefaultPosition(defPos.getValue());
        rec.setGoalPosition(goalPos.getValue());
        rec.setEnabled(enabled);
        rec.setProperties(new Array<JointPropDefinitionRecord>(
                properties.size(),
                Schema.createArray(JointPropDefinitionRecord.SCHEMA$)));
        myPropDefs = properties;
        for(JointPropDefinition prop : properties){
            rec.getProperties().add((JointPropDefinitionRecord)prop);
        }
        myCachedId = jId;
        myCachedDefaultPosition = defPos;
        myCachedGoalPosition = goalPos;
        myRecord = rec;
    }
    
    private void setRecord(JointDefinitionRecord record){
        if(record == null){
            throw new NullPointerException();
        }
        myRecord = record;
        myCachedId = new Joint.Id(myRecord.getJointId());
        myCachedDefaultPosition = 
                new NormalizedDouble(myRecord.getDefaultPosition());
        myCachedGoalPosition = new NormalizedDouble(myRecord.getGoalPosition());
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
        return myCachedDefaultPosition;
    }

    @Override
    public NormalizedDouble getGoalPosition() {
        return myCachedGoalPosition;
    }

    @Override
    public boolean getEnabled() {
        return myRecord.getEnabled();
    }

    @Override
    public List<JointPropDefinition> getJointProperties() {
        return myPropDefs;
    }
    
    public JointDefinitionRecord getRecord(){
        return myRecord;
    }

    private JointPropDefinition defineJointProperty(JointProperty prop) {
        JointPropDefinitionRecord def = new JointPropDefinitionRecord();
        def.setPropertyName(prop.getPropertyName());
        def.setDisplayName(prop.getDisplayName());
        
        Class c = prop.getPropertyClass();
        NormalizableRange r = prop.getNormalizableRange();
        
        if(!Number.class.isAssignableFrom(c)) {
            def.setMinValue(0.0);
            def.setMaxValue(1.0);
            def.setInitialValue(
                    r.normalizeValue(prop.getValue()).getValue());
        } else {
            Number n = (Number)prop.getValue();
            Number min = (Number)r.getMin();
            Number max = (Number)r.getMax();
            
            def.setMinValue(min.doubleValue());
            def.setMaxValue(max.doubleValue());
            def.setInitialValue(n.doubleValue());
        }
        
        return def;
    }
}
