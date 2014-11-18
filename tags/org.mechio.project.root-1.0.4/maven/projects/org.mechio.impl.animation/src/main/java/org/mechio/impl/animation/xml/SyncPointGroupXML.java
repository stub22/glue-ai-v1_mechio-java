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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.tree.ConfigurationNode;
import java.util.Set;
import org.mechio.api.animation.editor.features.SyncPointGroupConfig.SyncPointConfig;
import java.io.IOException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jflux.extern.utils.apache_commons_configuration.rk.XMLConfigUtils;
import org.mechio.api.animation.editor.features.SyncPointGroupConfig;
import org.mechio.api.animation.editor.features.SyncPointGroupConfig.SyncGroupConfig;
import org.mechio.api.animation.editor.features.SynchronizedPointGroup;
import org.xmlpull.v1.XmlSerializer;
import static org.mechio.api.animation.xml.AnimationXML.*;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */

public class SyncPointGroupXML {
    public static class XPP3Writer {        
        static void writeSyncGroups(XmlSerializer xs, 
                Set<SynchronizedPointGroup> pointGroups) throws IOException{
            xs.startTag(NAMESPACE, SYNC_POINT_GROUPS);
            if(pointGroups == null){
                xs.endTag(NAMESPACE, SYNC_POINT_GROUPS);
                return;
            }
            for(SynchronizedPointGroup group : pointGroups){
                SyncGroupConfig conf = SyncPointGroupConfig.createConfig(group);
                if(conf == null){
                    continue;
                }
                writeSyncGroupConfig(xs, conf);
            }
            xs.endTag(NAMESPACE, SYNC_POINT_GROUPS);
        }
        
        static void writeSyncGroupConfig(
                XmlSerializer xs, SyncGroupConfig conf) throws IOException{
            xs.startTag(NAMESPACE, SYNC_POINT_GROUP);
            for(SyncPointConfig pointConf : conf.points){
                writeSyncPointConfig(xs, pointConf);
            }
            xs.endTag(NAMESPACE, SYNC_POINT_GROUP);
        }
        
        static void writeSyncPointConfig(
                XmlSerializer xs, SyncPointConfig conf) throws IOException{
            xs.startTag(NAMESPACE, SYNC_POINT);
                tag(xs, NAMESPACE, SYNC_POINT_CHANNEL_ID, conf.channelId);
                tag(xs, NAMESPACE, SYNC_POINT_MOTION_PATH_ID, conf.motionPathId);
                tag(xs, NAMESPACE, SYNC_POINT_CONTROL_POINT_ID, conf.controlPointId);
            xs.endTag(NAMESPACE, SYNC_POINT);
        }
        
        static void tag(XmlSerializer xs, 
                String namespace, String tag, Object val) throws IOException{
            xs.startTag(namespace, tag);
                xs.text(val.toString());
            xs.endTag(namespace, tag);
        }
    }
    
    public static class ApacheWriter {
        static ConfigurationNode writeSyncGroups(
                Set<SynchronizedPointGroup> pointGroups) {
            ConfigurationNode node = XMLConfigUtils.node(SYNC_POINT_GROUPS);
            if(pointGroups == null){
                return node;
            }
            for(SynchronizedPointGroup group : pointGroups){
                SyncGroupConfig conf = SyncPointGroupConfig.createConfig(group);
                if(conf == null){
                    continue;
                }
                node.addChild(writeSyncGroupConfig(conf));
            }
            return node;
        }
        
        static ConfigurationNode writeSyncGroupConfig(SyncGroupConfig conf) {
            ConfigurationNode node = XMLConfigUtils.node(SYNC_POINT_GROUP);
            for(SyncPointConfig c : conf.points){
                node.addChild(writeSyncPointConfig(c));
            }
            return node;
        }
        
        static ConfigurationNode writeSyncPointConfig(SyncPointConfig conf) {
            ConfigurationNode node = XMLConfigUtils.node(SYNC_POINT);
            node.addChild(XMLConfigUtils.node(SYNC_POINT_CHANNEL_ID, conf.channelId));
            node.addChild(XMLConfigUtils.node(SYNC_POINT_MOTION_PATH_ID, conf.motionPathId));
            node.addChild(XMLConfigUtils.node(SYNC_POINT_CONTROL_POINT_ID, conf.controlPointId));
            return node;
        }
    }
    
    public static class ApacheReader {
        static List<SyncGroupConfig> readSyncPointGroupConfigs(
                HierarchicalConfiguration conf) {
            List<HierarchicalConfiguration> nodes = 
                    (List)conf.configurationsAt(SYNC_POINT_GROUP);
            if(nodes == null || nodes.isEmpty()){
                return null;
            }
            List<SyncGroupConfig> groups = new ArrayList(nodes.size());
            for(HierarchicalConfiguration node : nodes){
                SyncGroupConfig group = readSyncPointGroup(node);
                if(group == null){
                    continue;
                }
                groups.add(group);
            }
            return groups;
        }
        
        static SyncGroupConfig readSyncPointGroup(HierarchicalConfiguration conf) {
            List<HierarchicalConfiguration> nodes = 
                    (List)conf.configurationsAt(SYNC_POINT);
            if(nodes == null || nodes.isEmpty()){
                return null;
            }
            List<SyncPointConfig> points = new ArrayList(nodes.size());
            for(HierarchicalConfiguration node : nodes){
                SyncPointConfig point = readSyncPointConfig(node);
                if(point == null){
                    continue;
                }
                points.add(point);
            }
            if(points.isEmpty()){
                return null;
            }
            return new SyncGroupConfig(points);
        }
        
        static SyncPointConfig readSyncPointConfig(HierarchicalConfiguration conf) {
            int chanId = conf.getInt(SYNC_POINT_CHANNEL_ID, -1);
            int pathId = conf.getInt(SYNC_POINT_MOTION_PATH_ID, -1);
            int pointId = conf.getInt(SYNC_POINT_CONTROL_POINT_ID, -1);
            if(chanId < 0 || pathId < 0 || pointId < 0){
                return null;
            }
            return new SyncPointConfig(chanId, pathId, pointId);
        }
    }
}
