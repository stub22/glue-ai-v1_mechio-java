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

import java.util.List;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Adapter;
import org.mechio.api.animation.protocol.AnimationSignal;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
public class PortableAnimationSignal implements AnimationSignal {
    private AnimationSignallingRecord myAnimationSignallingRecord;
    
    public PortableAnimationSignal(AnimationSignallingRecord signalRec) {
        if(signalRec == null) {
            throw new NullPointerException();
        }
        
        myAnimationSignallingRecord = signalRec;
    }
    
    public PortableAnimationSignal(AnimationSignal signal) {
        if(signal == null) {
            throw new NullPointerException();
        }
        
        if(signal instanceof PortableAnimationSignal) {
            PortableAnimationSignal pSignal = (PortableAnimationSignal)signal;
            myAnimationSignallingRecord = pSignal.getRecord();
            return;
        }
        
        buildRecord(
                signal.getSourceId(), signal.getTimestampMillisecUTC(),
                signal.getEventType(), signal.getAnimationName(),
                signal.getAnimationVersion(), signal.getAnimationHash(),
                signal.getAnimationLength(), signal.getAnimationProperties());
    }
    
    public PortableAnimationSignal(
            String sourceId, long timestampMillisecUTC, String eventType,
            String animationName, String animationVersion, int animationHash,
            long animationLength, List<String> animationProperties) {
        buildRecord(
                sourceId, timestampMillisecUTC, eventType, animationName,
                animationVersion, animationHash, animationLength,
                animationProperties);
    }
    
    private void buildRecord(
            String sourceId, long timestampMillisecUTC, String eventType,
            String animationName, String animationVersion, int animationHash,
            long animationLength, List<String> animationProperties) {
        if(sourceId == null || eventType == null || animationName == null ||
                animationVersion == null || animationProperties == null) {
            throw new NullPointerException();
        }
        
        AnimationSignallingRecord signalRec = new AnimationSignallingRecord();
        signalRec.setSourceId(sourceId);
        signalRec.setTimestampMillisecUTC(timestampMillisecUTC);
        signalRec.setEventType(eventType);
        signalRec.setAnimationName(animationName);
        signalRec.setAnimationVersion(animationVersion);
        signalRec.setAnimationHash(animationHash);
        signalRec.setAnimationLength(animationLength);
        signalRec.setAnimationProperties(animationProperties);
        
        myAnimationSignallingRecord = signalRec;
    }

    @Override
    public String getSourceId() {
        return myAnimationSignallingRecord.getSourceId();
    }

    @Override
    public Long getTimestampMillisecUTC() {
        return myAnimationSignallingRecord.getTimestampMillisecUTC();
    }

    @Override
    public String getEventType() {
        return myAnimationSignallingRecord.getEventType();
    }

    @Override
    public String getAnimationName() {
        return myAnimationSignallingRecord.getAnimationName();
    }

    @Override
    public String getAnimationVersion() {
        return myAnimationSignallingRecord.getAnimationVersion();
    }

    @Override
    public Integer getAnimationHash() {
        return myAnimationSignallingRecord.getAnimationHash();
    }

    @Override
    public Long getAnimationLength() {
        return myAnimationSignallingRecord.getAnimationLength();
    }

    @Override
    public List<String> getAnimationProperties() {
        return myAnimationSignallingRecord.getAnimationProperties();
    }
    
    public AnimationSignallingRecord getRecord() {
        return myAnimationSignallingRecord;
    }
    
    public static class Factory implements AnimationSignalFactory {
        @Override
        public AnimationSignal createAnimationSignal(
                String sourceId, String eventType, String animationName,
                String animationVersion, int animationHash,
                long animationLength, List<String> animationProperties) {
            if(sourceId == null || eventType == null || animationName == null ||
                    animationVersion == null || animationProperties == null) {
                throw new NullPointerException();
            }
            
            return new PortableAnimationSignal(
                    sourceId, TimeUtils.now(), eventType, animationName,
                    animationVersion, animationHash, animationLength,
                    animationProperties);
        }
    }
    
    public static class MessageRecordAdapter
        implements Adapter<AnimationSignal, AnimationSignallingRecord> {
        @Override
        public AnimationSignallingRecord adapt(AnimationSignal a) {
            if(a == null) {
                throw new NullPointerException();
            }
            
            return new PortableAnimationSignal(a).getRecord();
        }
    }
    
    public static class RecordMessageAdapter
        implements Adapter<AnimationSignallingRecord, AnimationSignal> {
        @Override
        public AnimationSignal adapt(AnimationSignallingRecord a) {
            return new PortableAnimationSignal(a);
        }
    }
}
