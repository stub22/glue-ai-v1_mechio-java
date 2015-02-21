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

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.mechio.api.animation.protocol.PlayRequest;
import org.jflux.api.core.Adapter;

/**
 * PlayRequest which wraps a PlayRequestRecord.
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortablePlayRequest implements PlayRequest{
    private PlayRequestRecord myPlayRequestRecord;

    public PortablePlayRequest(PlayRequestRecord playRec){
        if(playRec == null){
            throw new NullPointerException();
        }
        myPlayRequestRecord = playRec;
    }
    
    public PortablePlayRequest(PlayRequest playRequest){
        PlayRequestRecord rec = new PlayRequestRecord();
        rec.setSourceId(playRequest.getSourceId());
        rec.setDestinationId(playRequest.getDestinationId());
        rec.setCurrentTimeMillisec(playRequest.getCurrentTimeMillisec());
        rec.setAnimationName(playRequest.getAnimationName());
        rec.setAnimationVersionNumber(playRequest.getAnimationVersionNumber());
        myPlayRequestRecord = rec;
    }
    
    public PortablePlayRequest(String sourceId, String destId, 
            long curTimeMillisecUTC, String animName, String animVersionNum){
        PlayRequestRecord rec = new PlayRequestRecord();
        rec.setSourceId(sourceId);
        rec.setDestinationId(destId);
        rec.setCurrentTimeMillisec(curTimeMillisecUTC);
        rec.setAnimationName(animName);
        rec.setAnimationVersionNumber(animVersionNum);
        myPlayRequestRecord = rec;
    }
    
    @Override
    public String getSourceId() {
        return myPlayRequestRecord.getSourceId();
    }

    @Override
    public String getDestinationId() {
        return myPlayRequestRecord.getDestinationId();
    }

    @Override
    public Long getCurrentTimeMillisec() {
        return myPlayRequestRecord.getCurrentTimeMillisec();
    }

    @Override
    public String getAnimationName() {
        return myPlayRequestRecord.getAnimationName();
    }

    @Override
    public String getAnimationVersionNumber() {
        return myPlayRequestRecord.getAnimationVersionNumber();
    }
    
    public PlayRequestRecord getRecord(){
        return myPlayRequestRecord;
    }
    
    public static class Factory implements PlayRequestFactory{
        @Override
        public PlayRequest createPlayRequest(
                String clientId, String hostId, VersionProperty animVersion) {
            if(clientId == null || hostId == null || animVersion == null){
                throw new NullPointerException();
            }
            return new PortablePlayRequest(
                    clientId, hostId, TimeUtils.now(), 
                    animVersion.getName(), animVersion.getNumber());
        }
    }
    
    public static class MessageRecordAdapter 
            implements Adapter<PlayRequest, PlayRequestRecord>{
        @Override
        public PlayRequestRecord adapt(PlayRequest a) {
            if(a == null){
                throw new NullPointerException();
            }
            return new PortablePlayRequest(a).getRecord();
        }
    }
    
    public static class RecordMessageAdapter implements
            Adapter<PlayRequestRecord, PlayRequest>{
        @Override
        public PlayRequest adapt(PlayRequestRecord a) {
            return new PortablePlayRequest(a);
        }
    }
}
