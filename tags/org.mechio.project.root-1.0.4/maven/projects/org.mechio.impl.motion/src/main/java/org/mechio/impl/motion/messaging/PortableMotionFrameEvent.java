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
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.protocol.MotionFrame;
import org.mechio.api.motion.protocol.MotionFrameEvent;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableMotionFrameEvent implements MotionFrameEvent {
    private MotionFrameEventRecord myFrameEventRecord;
    private MotionFrame<RobotPositionMap> myCachedMotionFrame;
    
    public PortableMotionFrameEvent(MotionFrameEventRecord frameEventRec){
        if(frameEventRec == null){
            throw new NullPointerException();
        }
        myFrameEventRecord = frameEventRec;
    }
    
    public PortableMotionFrameEvent(
            String sourceId, String destinationId, 
            long currentTimeMillisecUTC, 
            MotionFrame<RobotPositionMap> motionFrame){
        if(sourceId == null || destinationId == null || motionFrame == null){
            throw new NullPointerException();
        }
        myFrameEventRecord = new MotionFrameEventRecord();
        myFrameEventRecord.setSourceId(sourceId);
        myFrameEventRecord.setDestinationId(destinationId);
        myFrameEventRecord.setCurrentTimeMillisecUTC(currentTimeMillisecUTC);
        myFrameEventRecord.setMotionFrame(
                MotionMessagingUtils.packMotionFrame(motionFrame));
        myCachedMotionFrame = motionFrame;
    }
    
    public PortableMotionFrameEvent(MotionFrameEvent frameEvent){
        if(frameEvent == null){
            throw new NullPointerException();
        }
        if(frameEvent instanceof PortableMotionFrameEvent){
            myFrameEventRecord = 
                    ((PortableMotionFrameEvent)frameEvent).myFrameEventRecord;
            myCachedMotionFrame = 
                    ((PortableMotionFrameEvent)frameEvent).myCachedMotionFrame;
            return;
        }
        myFrameEventRecord = new MotionFrameEventRecord();
        myFrameEventRecord.setSourceId(frameEvent.getSourceId());
        myFrameEventRecord.setDestinationId(frameEvent.getDestinationId());
        myFrameEventRecord.setCurrentTimeMillisecUTC(
                frameEvent.getTimestampMillisecUTC());
        myFrameEventRecord.setMotionFrame(MotionMessagingUtils.packMotionFrame(
                frameEvent.getMotionFrame()));
        myCachedMotionFrame = frameEvent.getMotionFrame();
    }
    
    @Override
    public String getSourceId() {
        return myFrameEventRecord.getSourceId();
    }

    @Override
    public String getDestinationId() {
        return myFrameEventRecord.getDestinationId();
    }

    @Override
    public long getTimestampMillisecUTC() {
        return myFrameEventRecord.getCurrentTimeMillisecUTC();
    }

    @Override
    public MotionFrame<RobotPositionMap> getMotionFrame() {
        if(myCachedMotionFrame == null){
            MotionFrameRecord frameRec = myFrameEventRecord.getMotionFrame();
            myCachedMotionFrame = 
                    MotionMessagingUtils.unpackMotionFrame(frameRec);
        }
        return myCachedMotionFrame;
    }
    
    public MotionFrameEventRecord getRecord(){
        return myFrameEventRecord;
    }
    
    public static class Factory implements MotionFrameEventFactory{

        @Override
        public MotionFrameEvent createMotionFrameEvent(
                String sourceId, String destId, 
                MotionFrame<RobotPositionMap> motionFrame) {
            return new PortableMotionFrameEvent(sourceId, destId, 
                    TimeUtils.now(), motionFrame);
        }
        
    }
    
    public static class MessageRecordAdapter 
            implements Adapter<MotionFrameEvent, MotionFrameEventRecord>{
        @Override
        public MotionFrameEventRecord adapt(MotionFrameEvent a) {
            if(a == null){
                throw new NullPointerException();
            }
            return new PortableMotionFrameEvent(a).getRecord();
        }
    }
    
    public static class RecordMessageAdapter implements
            Adapter<MotionFrameEventRecord, MotionFrameEvent>{
        @Override
        public MotionFrameEvent adapt(MotionFrameEventRecord a) {
            return new PortableMotionFrameEvent(a);
        }
    }
}
