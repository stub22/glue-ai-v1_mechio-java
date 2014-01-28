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
package org.mechio.impl.animation.messaging;

import org.jflux.api.common.rk.utils.TimeUtils;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.protocol.AnimationEvent;
import org.jflux.api.core.Adapter;

/**
 * AnimationEvent which wraps an AnimationEventRecord.
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableAnimationEvent implements AnimationEvent{
    private AnimationEventRecord myAnimationEventRecord;
    private Animation myCachedAnimation;
    
    public PortableAnimationEvent(AnimationEventRecord eventRec){
        if(eventRec == null){
            throw new NullPointerException();
        }
        myAnimationEventRecord = eventRec;
    }
    public PortableAnimationEvent(AnimationEvent event){
        if(event == null){
            throw new NullPointerException();
        }
        if(event instanceof PortableAnimationEvent){
            PortableAnimationEvent pEvent = (PortableAnimationEvent)event;
            myAnimationEventRecord = pEvent.getRecord();
            myCachedAnimation = pEvent.getAnimation();
            return;
        }
        buildRecord(event.getSourceId(), 
                    event.getDestinationId(), 
                    event.getCurrentTimeMillisec(), 
                    event.getAnimation());
    }
    
    public PortableAnimationEvent(String sourceId, String destinationId, 
            long currentTime, Animation anim){
        buildRecord(sourceId, destinationId, currentTime, anim);
    }
    
    private void buildRecord(String sourceId, String destinationId, 
            long currentTime, Animation anim){
        if(sourceId == null || destinationId == null || anim == null){
            throw new NullPointerException();
        }
        AnimationEventRecord eventRec = new AnimationEventRecord();
        eventRec.setSourceId(sourceId);
        eventRec.setDestinationId(destinationId);
        eventRec.setCurrentTimeMillisec(currentTime);
        AnimationRecord animRec = AnimationMessagingUtils.packAnimation(anim);
        eventRec.setAnimation(animRec);
        myAnimationEventRecord = eventRec;
        myCachedAnimation = anim;
    }
    
    @Override
    public String getSourceId() {
        return myAnimationEventRecord.getSourceId();
    }

    @Override
    public String getDestinationId() {
        return myAnimationEventRecord.getDestinationId();
    }

    @Override
    public Long getCurrentTimeMillisec() {
        return myAnimationEventRecord.getCurrentTimeMillisec();
    }

    @Override
    public Animation getAnimation() {
        if(myCachedAnimation == null){
            AnimationRecord animRec = myAnimationEventRecord.getAnimation();
            myCachedAnimation = 
                    AnimationMessagingUtils.unpackAnimation(animRec);
        }
        return myCachedAnimation;
    }
    
    public AnimationEventRecord getRecord(){
        return myAnimationEventRecord;
    }
    
    public static class Factory implements AnimationEventFactory{
        @Override
        public AnimationEvent createAnimationEvent(
                String clientId, String hostId, Animation animation) {
            if(clientId == null || hostId == null || animation == null){
                throw new NullPointerException();
            }
            return new PortableAnimationEvent(
                    clientId, hostId, TimeUtils.now(), animation);
        }
    }
    
    public static class MessageRecordAdapter 
            implements Adapter<AnimationEvent, AnimationEventRecord>{
        @Override
        public AnimationEventRecord adapt(AnimationEvent a) {
            if(a == null){
                throw new NullPointerException();
            }
            return new PortableAnimationEvent(a).getRecord();
        }
    }
    
    public static class RecordMessageAdapter implements
            Adapter<AnimationEventRecord, AnimationEvent>{
        @Override
        public AnimationEvent adapt(AnimationEventRecord a) {
            return new PortableAnimationEvent(a);
        }
    }
}
