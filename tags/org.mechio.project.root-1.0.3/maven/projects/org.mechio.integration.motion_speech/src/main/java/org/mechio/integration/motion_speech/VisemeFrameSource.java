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
package org.mechio.integration.motion_speech;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.utils.Utils;
import org.jflux.api.core.Listener;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.JointId;
import org.mechio.api.motion.Robot.RobotPositionHashMap;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.protocol.DefaultMotionFrame;
import org.mechio.api.motion.blending.FrameSource;
import org.mechio.api.motion.protocol.MotionFrame;
import org.mechio.api.speech.viseme.Viseme;
import org.mechio.api.speech.viseme.VisemeBindingManager;
import org.mechio.api.speech.viseme.VisemeEvent;
import org.mechio.api.speech.viseme.VisemeEventQueue;

/**
 * Creates MotionFrames from Visemes for synchronizing Joint movement with s
 * speech.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeFrameSource implements FrameSource<RobotPositionMap> {
    private final static Logger theLogger = Logger.getLogger(VisemeFrameSource.class.getName());
    private Robot.Id myRobotId;
    private VisemeBindingManager myVisemeManager;
    private RobotPositionMap myStartPositions;
    private RobotPositionMap myGoalPositions;
    private Integer myCurrentMoveLength;
    private Long myCurrentMoveStart;
    private Map<Viseme,RobotPositionMap> myVisemePositions;
    private VisemeEventQueue myVisemeQueue;
    private VisemeEvent myPreviousViseme;
    /**
     * Creates a new VisemeFrameSource
     * @param robotId robot to move
     * @param visemeManager VisemeManager to provide positions from visemes
     */
    public VisemeFrameSource(
            Robot.Id robotId, VisemeBindingManager visemeManager){
        if(robotId == null){
            throw new NullPointerException();
        }
        myRobotId = robotId;
        myVisemeManager = visemeManager;
        cacheAllVisemePositions();
        myVisemeQueue = new VisemeEventQueue();
    }
    
    public void setVisemeBindingManager(VisemeBindingManager manager){
        myVisemeManager = manager;
        myVisemePositions.clear();
    }
    
    private void cacheAllVisemePositions(){
        myVisemePositions = new EnumMap<Viseme, RobotPositionMap>(Viseme.class);
        for(Viseme viseme : Viseme.values()){
            cacheVisemePositions(viseme);
        }
    }
    
    private RobotPositionMap cacheVisemePositions(Viseme viseme){
        if(myVisemePositions.containsKey(viseme)){
            return myVisemePositions.get(viseme);
        }
        if(myVisemeManager == null){
            theLogger.log(Level.WARNING, 
                    "Missing Viseme Manager for: {0}", myRobotId);
            return null;
        }
        Map<Integer,NormalizedDouble> vals = 
                myVisemeManager.getBindingValues(viseme);
        if(vals == null){
            theLogger.log(Level.WARNING, 
                    "Missing Viseme Binding for: {0}", viseme.name());
            return null;
        }
        RobotPositionMap map = convertVisemeValues(vals);
        myVisemePositions.put(viseme, map);
        return map;
    }
        
    private RobotPositionMap convertVisemeValues(Map<Integer,NormalizedDouble> values){
        RobotPositionMap map = new RobotPositionHashMap(values.size());
        for(Entry<Integer,NormalizedDouble> e : values.entrySet()){
            map.put(jointId(e.getKey()),e.getValue());
        }
        return map;
    }
        
    private JointId jointId(int id){
        Joint.Id jId = new Joint.Id(id);
        return new JointId(myRobotId, jId);
    }
    
    @Override
    public MotionFrame<RobotPositionMap> getMovements(
        long currentTimeUTC, long moveLengthMilliSec) {
        return getFrameUnsafe(currentTimeUTC, moveLengthMilliSec);
    }
    
    private MotionFrame getFrameUnsafe(
            long currentTimeUTC, long moveLengthMilliSec){
        VisemeEvent event = myVisemeQueue.getEvent(currentTimeUTC);
        if(event == null){
            return null;
        }else if(event != myPreviousViseme){
            myStartPositions = 
                    cacheVisemePositions(event.getCurrentViseme());
            myGoalPositions = 
                    cacheVisemePositions(event.getNextViseme());
            myCurrentMoveStart = event.getTimestampMillisecUTC();
            myCurrentMoveLength = event.getDuration();
            myPreviousViseme = event;
        }
        if(myStartPositions == null || myGoalPositions == null
                || myCurrentMoveStart == null || myCurrentMoveLength == null
                || myCurrentMoveLength <= 0){
            return null;
        }
        double startMillisec = currentTimeUTC - myCurrentMoveStart;
        startMillisec = Math.max(0, startMillisec);
        double stopMillisec = startMillisec + moveLengthMilliSec;
        stopMillisec = 
                Utils.bound(stopMillisec, startMillisec+1, myCurrentMoveLength);
        double startPercent = startMillisec/(double)myCurrentMoveLength;
        startPercent = Utils.bound(startPercent, 0.0, 1.0);
        double stopPercent = stopMillisec/(double)myCurrentMoveLength;
        stopPercent = Utils.bound(stopPercent, 0.0, 1.0);
        
        MotionFrame<RobotPositionMap> frame = 
                new DefaultMotionFrame<RobotPositionMap>();
        frame.setFrameLengthMillisec(moveLengthMilliSec);
        frame.setTimestampMillisecUTC(currentTimeUTC);
        frame.setPreviousPositions(new RobotPositionHashMap());
        frame.setGoalPositions(new RobotPositionHashMap());
        
        for(JointId jId : myStartPositions.keySet()){
            addJoint(jId, frame, startPercent, stopPercent);
        }
        //if(currentTimeUTC >= myCurrentMoveStart + myCurrentMoveLength){
            //cleanup();
        //}
        return frame;
    }
    
    private void addJoint(JointId jointId, 
            MotionFrame frame, double startPercent, double stopPercent){
        NormalizedDouble normAbsStart = myStartPositions.get(jointId);
        NormalizedDouble normAbsStop = myGoalPositions.get(jointId);
        if(normAbsStart == null || normAbsStop == null){
            return;
            
        }
        double absStart = normAbsStart.getValue();
        double absStop = normAbsStop.getValue();
        
        double range = absStop - absStart;
        double start = startPercent*range + absStart;  
        start = Utils.bound(start, 0.0, 1.0);      
        double stop = stopPercent*range + absStart;
        stop = Utils.bound(stop, 0.0, 1.0);
        
        frame.getPreviousPositions().put(jointId, new NormalizedDouble(start));
        frame.getGoalPositions().put(jointId, new NormalizedDouble(stop));
    }
    
    /**
     * Returns the VisemeListener which supplies this FrameSource with Visemes.
     * @return VisemeListener which supplies this FrameSource with Visemes
     */
    public Listener<VisemeEvent> getVisemeListener(){
        return myVisemeQueue.getListener();
    }
}
