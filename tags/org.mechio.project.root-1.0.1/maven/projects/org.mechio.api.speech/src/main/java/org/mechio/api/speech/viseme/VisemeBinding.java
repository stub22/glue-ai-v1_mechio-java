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
package org.mechio.api.speech.viseme;

import java.util.EnumMap;
import java.util.Map;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.utils.Utils;
import org.mechio.api.speech.viseme.config.VisemeBindingConfig;

/**
 * Defines NormalizedDoubles, for each Viseme, to be bound to a key.
 * Used to synchronize movement or visual output with speech.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeBinding{
    private int myBindingKey;
    private Map<Viseme,NormalizedDouble> myVisemeValueMap;
    
    /**
     * Creates an empty VisemeBinding for the given key.
     * @param key binding key for this VisemeBinding
     */
    public VisemeBinding(int key){
        myBindingKey = key;
        myVisemeValueMap = new EnumMap<Viseme, NormalizedDouble>(Viseme.class);
    }
    
    /**
     * Creates a new VisemeBinding with the given configuration.
     * @param config configuration for the VisemeBinding
     */
    public VisemeBinding(VisemeBindingConfig<VisemePosition> config){
        this(config.getBindingId());
        
        for(VisemePosition pos : config.getVisemeBindings()){
            int id = pos.getVisemeId();
            Viseme vis = Viseme.getById(id);
            double boundPos = Utils.bound(pos.getPosition(), 0.0, 1.0);
            NormalizedDouble val = new NormalizedDouble(boundPos);
            myVisemeValueMap.put(vis, val);
        }
    }
    
    /**
     * Returns the binding key for this VisemeBinding.
     * @return binding key for this VisemeBinding
     */
    public int getBindingKey(){
        return myBindingKey;
    }
    
    /**
     * Returns the NormalizedDouble mapped to the given Viseme.
     * @param viseme Viseme value to retrieve
     * @return NormalizedDouble mapped to the given Viseme
     */
    public NormalizedDouble getValue(Viseme viseme){
        if(viseme == null){
            throw new NullPointerException();
        }
        return myVisemeValueMap.get(viseme);
    }
    
    /**
     * Sets the NormalizedDouble to be mapped to the given Viseme.
     * @param viseme Viseme to map to the given NormalizedDouble
     * @param value NormalizedDouble the Viseme should map to
     */
    public void setVisemeValue(Viseme viseme, NormalizedDouble value){
        if(viseme == null){
            throw new NullPointerException();
        }
        myVisemeValueMap.put(viseme, value);
    }
    
    public Map<Viseme,NormalizedDouble> getVisemeValueMap(){
        return myVisemeValueMap;
    }
}
