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
package org.mechio.api.animation.utils;

import java.util.HashMap;
import java.util.Map;
import org.jflux.api.common.rk.position.DoubleRange;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
public class ChannelsParameter {
    private int myChannelID;
    private String myChannelName;
    private NormalizedDouble myDefaultPosition;
    private NormalizableRange<Double> myNormalizableRange;
    private Map<String, String> myKeyValuePairs;
    
    public ChannelsParameter(int channelID, String channelName,
            NormalizedDouble defaultPosition,
            NormalizableRange<Double> normalizableRange) {
        if(channelName == null || defaultPosition == null) {
            throw new IllegalArgumentException();
        }
        
        this.myChannelID = channelID;
        this.myChannelName = channelName;
        this.myDefaultPosition = defaultPosition;
        this.myNormalizableRange = normalizableRange;
        if(myNormalizableRange == null){
            myNormalizableRange = new DoubleRange(0.0,1.0);
        }
        
        this.myKeyValuePairs = new HashMap();
    }
    
    public ChannelsParameter(int channelID, String channelName,
            NormalizedDouble defaultPosition) {
        if(channelName == null || defaultPosition == null) {
            throw new IllegalArgumentException();
        }
        
        this.myChannelID = channelID;
        this.myChannelName = channelName;
        this.myDefaultPosition = defaultPosition;
        this.myNormalizableRange = null;
        
        this.myKeyValuePairs = new HashMap();
    }
    
    public Object getParameter(String key) {
        return myKeyValuePairs.get(key);
    }
    
    public void setParameter(String key, String value) {
        if(key == null || value == null) {
            throw new IllegalArgumentException();
        }
        
        if(myKeyValuePairs.containsKey(key)) {
            myKeyValuePairs.remove(key);
        }
        
        myKeyValuePairs.put(key, value);
    }
    
    public void dropParameter(String key) {
        if(key == null || !myKeyValuePairs.containsKey(key)) {
            throw new IllegalArgumentException();
        }
        
        myKeyValuePairs.remove(key);
    }
    
    public int getChannelID() {
        return myChannelID;
    }
    
    public NormalizedDouble getDefaultPosition() {
        return myDefaultPosition;
    }
    
    public NormalizableRange<Double> getNormalizableRange() {
        if(myNormalizableRange == null) {
            throw new IllegalArgumentException();
        }
        
        return myNormalizableRange;
    }
    
    public String getChannelName() {
        return myChannelName;
    }
    
    public Map<String, String> getKeyValuePairs() {
        return myKeyValuePairs;
    }
}
