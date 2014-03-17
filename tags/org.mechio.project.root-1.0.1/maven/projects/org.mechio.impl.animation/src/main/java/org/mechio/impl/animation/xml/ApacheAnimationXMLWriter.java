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

package org.mechio.impl.animation.xml;

import org.apache.commons.configuration.ConfigurationException;
import org.mechio.api.animation.xml.AnimationFileWriter;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.jflux.api.common.rk.playable.Playable;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.services.Constants;
import org.jflux.api.common.rk.services.addon.AddOnUtils;
import org.jflux.api.common.rk.services.addon.ServiceAddOn;
import org.jflux.api.common.rk.services.addon.ServiceAddOnDriver;
import org.jflux.extern.utils.apache_commons_configuration.rk.XMLConfigUtils;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.Channel;
import org.mechio.api.animation.MotionPath;
import org.mechio.api.animation.editor.features.SynchronizedPointGroup;
import org.mechio.api.animation.utils.ChannelsParameter;
import org.mechio.api.animation.utils.ChannelsParameterSource;
import static org.mechio.api.animation.xml.AnimationXML.*;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ApacheAnimationXMLWriter implements AnimationFileWriter{
    private final static Logger theLogger = Logger.getLogger(ApacheAnimationXMLWriter.class.getName());

    @Override
    public void writeAnimation(
            String path, Animation anim, ChannelsParameterSource source,
            Set<SynchronizedPointGroup> syncPointGroups)
            throws Exception{
        ApacheAnimationXMLWriter.saveAnimation(
                path, anim, source, syncPointGroups);
    }
    
    /**
     * Saves an Animation to disk as an XML file.
     * @param path the full path to the destination file
     * @param anim the Animation to save
     */
    static void saveAnimation(
            String path, Animation anim, ChannelsParameterSource source,
            Set<SynchronizedPointGroup> syncPointGroups)
            throws ConfigurationException {
        if(path == null || path.isEmpty() || anim == null){
            return;
        }
        XMLConfiguration config = 
                writeAnimation(anim, path, source, syncPointGroups);
        config.save(path);
    }

    public static XMLConfiguration writeAnimation(
            Animation anim, String path, ChannelsParameterSource source,
            Set<SynchronizedPointGroup> syncPointGroups){
        XMLConfiguration config = new XMLConfiguration();
        config.setRootElementName(ANIMATION);
        ConfigurationNode node = config.getRootNode();
        node.addChild(XMLConfigUtils.writeVersion(anim.getVersion(), ANIMATION_VERSION_TYPE));
        node.addChild(writeChannels(anim.getChannels()));
        node.addChild(writeAddOnList(anim.getAddOns(), path));
        node.addChild(writeChannelsParameters(source));
        node.addChild(SyncPointGroupXML.ApacheWriter.writeSyncGroups(syncPointGroups));
        return config;
    }

    public static ConfigurationNode writeChannels(List<Channel> channels){
        ConfigurationNode node = XMLConfigUtils.node(CHANNELS);
        if(channels == null){
            return node;
        }
        for(Channel channel : channels){
            node.addChild(writeChannel(channel));
        }
        return node;
    }

    public static ConfigurationNode writeChannel(Channel channel){
        if(channel == null){
            return null;
        }
        ConfigurationNode node = XMLConfigUtils.node(CHANNEL);
        node.addAttribute(XMLConfigUtils.node(CHANNEL_ID, channel.getId()));
        String name = channel.getName();
        if(name != null && !name.isEmpty()){
            node.addAttribute(XMLConfigUtils.node(MOTION_PATH_NAME, name));
        }
        node.addChild(writeMotionPaths(channel.getMotionPaths()));
        return node;
    }

    public static ConfigurationNode writeMotionPaths(List<MotionPath> paths){
        ConfigurationNode node = XMLConfigUtils.node(MOTION_PATHS);
        if(paths == null || paths.isEmpty()){
            return node;
        }
        for(MotionPath path : paths){
            ConfigurationNode pNode = writeMotionPath(path);
            if(pNode != null){
                node.addChild(pNode);
            }
        }
        return node;
    }

    public static ConfigurationNode writeMotionPath(MotionPath mp){
        if(mp == null){
            return null;
        }
        ConfigurationNode node = XMLConfigUtils.node(MOTION_PATH);
        String name = mp.getName();
        if(name != null && !name.isEmpty()){
            node.addAttribute(XMLConfigUtils.node(MOTION_PATH_NAME, name));
        }
        node.addChild(XMLConfigUtils.writeVersion(mp.getInterpolatorVersion(), INTERPOLATION_VERSION_TYPE));
        node.addChild(writeControlPoints(mp.getControlPoints()));
        return node;
    }

    public static ConfigurationNode writeControlPoints(List<Point2D> points) {
        ConfigurationNode node = XMLConfigUtils.node(CONTROL_POINTS);
        for(Point2D p : points){
            ConfigurationNode child = writeControlPoint(p);
            if(child != null){
                node.addChild(child);
            }
        }
        return node;
    }

    public static ConfigurationNode writeControlPoint(Point2D p){
        if(p == null){
            return null;
        }
        ConfigurationNode node = XMLConfigUtils.node(CONTROL_POINT);
        node.addChild(XMLConfigUtils.node(TIME, p.getX()));
        node.addChild(XMLConfigUtils.node(POSITION, p.getY()));
        return node;
    }
    
    public static ConfigurationNode writeChannelsParameters(
            ChannelsParameterSource paramsSource) {
        ConfigurationNode node = XMLConfigUtils.node(CHANNELS_PARAMETERS);
        if(paramsSource == null){
            return node;
        }
        List<ChannelsParameter> params = paramsSource.getChannelParameters();
        if(params == null){
            return node;
        }
        for(ChannelsParameter param : params){
            node.addChild(writeChannelsParameter(param));
        }
        
        return node;
    }
    
    public static ConfigurationNode writeChannelsParameter(
            ChannelsParameter param) {
        if(param == null) {
            return null;
        }
        
        ConfigurationNode node = XMLConfigUtils.node(CHANNELS_PARAMETER);
        node.addChild(XMLConfigUtils.node(
                CHANNEL_ID_PARAM, param.getChannelID()));
        node.addChild(XMLConfigUtils.node(
                CHANNEL_NAME_PARAM, param.getChannelName()));
        node.addChild(XMLConfigUtils.node(
                DEFAULT_POSITION, param.getDefaultPosition().getValue()));
        node.addChild(writeNormalizableRange(param.getNormalizableRange()));
        node.addChild(writeGenericParameters(param.getKeyValuePairs()));
        
        return node;
    }
    
    public static ConfigurationNode writeNormalizableRange(
            NormalizableRange range) {
        if(range == null) {
            return null;
        }
        
        ConfigurationNode node = XMLConfigUtils.node(NORMALIZABLE_RANGE);
        
        node.addChild(XMLConfigUtils.node(RANGE_MIN, range.getMin()));
        node.addChild(XMLConfigUtils.node(RANGE_MAX, range.getMax()));
        
        return node;
    }
    
    public static ConfigurationNode writeGenericParameters(
            Map<String, String> pairs) {
        ConfigurationNode node = XMLConfigUtils.node(GENERIC_PARAMETERS);
        
        if(pairs == null) {
            return node;
        }
        
        for(String key: pairs.keySet()) {
            node.addChild(writeGenericParameter(key, pairs.get(key)));
        }
        
        return node;
    }
    
    public static ConfigurationNode writeGenericParameter(
            String key, String value) {
        if(value == null) {
            return null;
        }
        
        ConfigurationNode node = XMLConfigUtils.node(GENERIC_PARAMETER);
        
        node.addChild(XMLConfigUtils.node(PARAM_NAME, key));
        node.addChild(XMLConfigUtils.node(PARAM_VALUE, value));
        
        return node;
    }
    
    public static ConfigurationNode writeAddOnList(
            List<ServiceAddOn<Playable>> addons, String animPath){
        if(addons == null || animPath == null){
            throw new NullPointerException();
        }
        ConfigurationNode node = XMLConfigUtils.node(ADDONS);
        int addonCount = 0;
        for(ServiceAddOn addon : addons){
            String addonPath = animPath + ".addon." + addonCount + ".conf";
            ConfigurationNode addonNode;
            try{
                addonNode = writeAddOn(addon, addonPath);
            }catch(Exception ex){
                theLogger.log(Level.WARNING, "Error writing AddOn.", ex);
                continue;
            }
            if(addonNode == null){
                continue;
            }
            node.addChild(addonNode);
            addonCount++;
        }
        return node;
    }
    
    public static ConfigurationNode writeAddOn(
            ServiceAddOn<Playable> addon, String addonPath) throws Exception{
        if(addon == null || addonPath == null){
            return null;
        }
        ServiceAddOnDriver driver = addon.getAddOnDriver();
        if(driver == null){
            throw new NullPointerException();
        }
        if(!AddOnUtils.saveAddOnConfig(addon, addonPath)){
            return null;
        }
        ConfigurationNode node = XMLConfigUtils.node(ADDON);
        node.addChild(XMLConfigUtils.writeVersion(
                driver.getServiceVersion(), 
                Constants.SERVICE_VERSION));
        node.addChild(XMLConfigUtils.writeVersion(
                driver.getConfigurationFormat(), 
                Constants.CONFIG_FORMAT_VERSION));
        node.addChild(XMLConfigUtils.node(ADDON_FILE, addonPath));
        return node;
    }
}
