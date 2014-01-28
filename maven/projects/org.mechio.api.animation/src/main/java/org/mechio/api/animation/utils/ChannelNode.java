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

import java.util.List;
import org.jflux.api.common.rk.position.NormalizedDouble;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface ChannelNode {
    public String getName();
    public List<ChannelDefinition> getChannels();
    public List<ChannelNode> getChildGroups();
    
    public static interface ChannelDefinition{
        public int getId();
        public String getName();
        public double getDefaultPosition();
    }
    
    public static class DefaultChannelDefinition implements ChannelDefinition{
        public int myId;
        public String myName;
        public NormalizedDouble myDefaultPosition;
        
        public DefaultChannelDefinition(int id, String name, NormalizedDouble def){
            if(name == null || def == null){
                throw new NullPointerException();
            }
            myId = id;
            myName = name;
            myDefaultPosition = def;
        }

        @Override
        public int getId() {
            return myId;
        }

        @Override
        public String getName() {
            return myName;
        }

        @Override
        public double getDefaultPosition() {
            return myDefaultPosition.getValue();
        }
    }
}
