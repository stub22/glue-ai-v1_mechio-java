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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DefaultChannelNode implements ChannelNode{
    private String myName;
    private List<ChannelDefinition> myChannels;
    private List<ChannelNode> myChildGroups;
     
    @Override
    public String getName(){
        return myName;
    }
    
    public void setName(String name){
        myName = name;
    }
    
    @Override
    public List<ChannelDefinition> getChannels(){
        return myChannels;
    }
    
    @Override
    public List<ChannelNode> getChildGroups(){
        return myChildGroups;
    }
    
    public void addChannel(ChannelDefinition definition){
        if(definition == null){
            throw new NullPointerException();
        }
        if(myChannels == null){
            myChannels = new ArrayList<ChannelDefinition>();
        }
        myChannels.add(definition);
    }
    
    public void addGroup(ChannelNode group){
        if(group == null){
            throw new NullPointerException();
        }
        if(myChildGroups == null){
            myChildGroups = new ArrayList<ChannelNode>();
        }
        myChildGroups.add(group);
    }
}
