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
package org.mechio.integration.animation_motion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jflux.api.common.rk.position.DoubleRange;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.api.animation.utils.ChannelsParameter;
import org.mechio.api.animation.utils.ChannelsParameterSource;
import static org.mechio.api.animation.xml.AnimationXML.*;

/**
 *
 * @author Matthew Stevenson
 */
public class RobotParameterReader {
    public static ChannelsParameterSource readChannelsParamsSource(HierarchicalConfiguration config){
        List<HierarchicalConfiguration> nodes = (List<HierarchicalConfiguration>)config.configurationsAt(CHANNELS_PARAMETER);
        if(nodes == null || nodes.isEmpty()){
            return null;
        }
        ChannelsParameterSource channelParams = new ChannelRobotParameters();
        for(HierarchicalConfiguration node : nodes){
            ChannelsParameter channelParam = readChannelsParam(node);
            if(channelParam != null){
                channelParams.addChannelParameter(channelParam);
            }
        }
        return channelParams;
    }
    
    public static ChannelsParameter readChannelsParam(
            HierarchicalConfiguration config){
        int channelId = config.getInt(CHANNEL_ID_PARAM);
        String channelName = config.getString(CHANNEL_NAME_PARAM);
        NormalizedDouble defaultPosition =
                new NormalizedDouble(config.getDouble(DEFAULT_POSITION));
        NormalizableRange range = readNormalizableRange(config);
        Map<String, String> genericParams = readGenericProperties(config);
        
        ChannelsParameter param = new ChannelsParameter(
                channelId, channelName, defaultPosition, range);
        
        for(String key: genericParams.keySet()) {
            param.setParameter(key, genericParams.get(key));
        }
        
        return param;
    }
    
    public static Map<String,String> readGenericProperties(
            HierarchicalConfiguration config){
        List<HierarchicalConfiguration> nodes = (List<HierarchicalConfiguration>)config.configurationsAt(GENERIC_PARAMETER);
        
        if(nodes == null || nodes.isEmpty()){
            return null;
        }
        
        Map<String, String> genericProperties = new HashMap();
        
        for(HierarchicalConfiguration node : nodes){
            String key = node.getString(PARAM_NAME);
            String value = node.getString(PARAM_VALUE);
            
            genericProperties.put(key, value);
        }
        
        return genericProperties;
    }
    
    public static NormalizableRange<Double> readNormalizableRange(
            HierarchicalConfiguration config){
        HierarchicalConfiguration rangeConfig =
                config.configurationAt(NORMALIZABLE_RANGE);
        double min = rangeConfig.getDouble(RANGE_MIN);
        double max = rangeConfig.getDouble(RANGE_MAX);
        NormalizableRange<Double> range = new DoubleRange(min, max);
        
        return range;
    }
}
