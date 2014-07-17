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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData.Array;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.JointId;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.protocol.DefaultMotionFrame;
import org.mechio.api.motion.protocol.MotionFrame;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class MotionMessagingUtils {
    public static MotionFrameRecord packMotionFrame(
            MotionFrame<RobotPositionMap> frame){
        MotionFrameRecord frameRec = new MotionFrameRecord();
        frameRec.setMoveDurationMillisec(frame.getFrameLengthMillisec());
        frameRec.setTimestampMillisecUTC(frame.getTimestampMillisecUTC());
        RobotPositionMap start = frame.getPreviousPositions();
        RobotPositionMap goal = frame.getGoalPositions();
        if(goal == null){
            throw new NullPointerException();
        }
        frameRec.setGoalPositions(packRobotPositionMap(goal));
        if(start != null){
            frameRec.setStartPositions(packRobotPositionMap(start));
        }
        return frameRec;        
    }
    
    public static RobotPositionMapRecord packRobotPositionMap(
            RobotPositionMap map){
        RobotPositionMapRecord mapRec = new RobotPositionMapRecord();
        GenericArray<JointPositionRecord> jointPositions = new Array(map.size(), 
                Schema.createArray(JointPositionRecord.SCHEMA$));
        for(Entry<JointId,NormalizedDouble> e : map.entrySet()){
            JointId jId = e.getKey();
            NormalizedDouble pos = e.getValue();
            JointPositionRecord posRec = packJointPosition(jId, pos);
            jointPositions.add(posRec);
        }
        mapRec.setJointPositions(jointPositions);
        return mapRec;
    }
    
    private static JointPositionRecord packJointPosition(
            Robot.JointId jointId, NormalizedDouble position){
        JointIdRecord jointIdRec = packJointId(jointId);
        JointPositionRecord posRec = new JointPositionRecord();
        posRec.setJointId(jointIdRec);
        posRec.setNormalizedPosition(position.getValue());
        return posRec;
    }
    
    public static JointIdRecord packJointId(Robot.JointId jointId){
        JointIdRecord idRec = new JointIdRecord();
        idRec.setJointId(jointId.getJointId().getLogicalJointNumber());
        idRec.setRobotId(jointId.getRobotId().getRobtIdString());
        return idRec;
    }
    
    public static MotionFrame unpackMotionFrame(MotionFrameRecord frameRec){
        MotionFrame<RobotPositionMap> frame = 
                new DefaultMotionFrame<RobotPositionMap>();
        frame.setTimestampMillisecUTC(frameRec.getTimestampMillisecUTC());
        frame.setFrameLengthMillisec(frameRec.getMoveDurationMillisec());
        frame.setGoalPositions(unpackPositionMap(frameRec.getGoalPositions()));
        RobotPositionMapRecord startRec = frameRec.getStartPositions();
        if(startRec != null){
            frame.setPreviousPositions(unpackPositionMap(startRec));
        }
        return frame;
    }
    
    public static RobotPositionMap unpackPositionMap(
            RobotPositionMapRecord mapRec){
        List<JointPositionRecord> posRecs = mapRec.getJointPositions();
        RobotPositionMap map = new Robot.RobotPositionHashMap(posRecs.size());
        for(JointPositionRecord posRec : posRecs){
            Entry<JointId,NormalizedDouble> e = unpackJointPosition(posRec);
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }
    
    private static Entry<JointId,NormalizedDouble> unpackJointPosition(
            JointPositionRecord posRec){
        JointId jId = unpackJointId(posRec.getJointId());
        NormalizedDouble pos = new NormalizedDouble(
                posRec.getNormalizedPosition());
        Entry<JointId,NormalizedDouble> e = 
                new SimpleEntry<JointId, NormalizedDouble>(jId,pos);
        return e;
    }
    
    public static JointId unpackJointId(JointIdRecord jointIdRec){
        Robot.Id rId = new Robot.Id(jointIdRec.getRobotId().toString());
        Joint.Id jId = new Joint.Id(jointIdRec.getJointId());
        JointId jointId = new JointId(rId, jId);
        return jointId;
    }
}
