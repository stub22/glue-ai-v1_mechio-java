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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.position.NormalizedDouble;

/**
 * A VisemeBindingManager holds a set of VisemeBindings with different binding 
 * keys.  This manages synchronizing multiple output channels to speech.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class VisemeBindingManager {
    /**
     * Service type version name.
     */
    public final static String VERSION_NAME = "VisemeBindingManager";
    /**
     * Service type version number.
     */
    public final static String VERSION_NUMBER = "1.0";
    /**
     * Controller type VersionProperty.
     */
    public final static VersionProperty VERSION = new VersionProperty(VERSION_NAME, VERSION_NUMBER);
    
    private List<VisemeBinding> myBindings;
    
    /**
     * Creates an empty VisemeBindingManager.
     */
    public VisemeBindingManager(){
        myBindings = new ArrayList<VisemeBinding>();
    }
    
    /**
     * Adds a VisemeBinding to this Manager.  If a VisemeBinding with the same
     * binding key exists, it is replaced with the new VisemeBinding.
     * @param binding VisemeBinding to add
     */
    public void addBinding(VisemeBinding binding){
        if(binding == null){
            throw new NullPointerException();
        }
        int i = getBindingIndex(binding.getBindingKey());
        if(i != -1){
            myBindings.remove(i);
        }
        myBindings.add(binding);
    }
    
    private int getBindingIndex(int key){
        int i=0;
        for(VisemeBinding binding : myBindings){
            if(binding.getBindingKey() == key){
                return i;
            }
            i++;
        }
        return -1;
    }
    
    /**
     * Returns a Map of binding keys to NormalizedDoubles for the given Viseme.
     * @param viseme Viseme value to retrieve from each VisemeBinding
     * @return Map of binding keys to NormalizedDoubles for the given Viseme
     */
    public Map<Integer,NormalizedDouble> getBindingValues(Viseme viseme){
        if(viseme == null){
            throw new NullPointerException();
        }
        Map<Integer,NormalizedDouble> map = new HashMap();
        for(VisemeBinding binding : myBindings){
            map.put(binding.getBindingKey(), binding.getValue(viseme));
        }
        return map;
    }
}
