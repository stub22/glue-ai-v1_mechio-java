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

package org.mechio.api.animation.editor.features;

import java.util.ArrayList;
import java.util.List;
import org.mechio.api.animation.editor.AbstractEditor;
import org.mechio.api.animation.editor.ChannelEditor;
import org.mechio.api.animation.editor.ControlPointEditor;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */

public class SyncPointGroupConfig {
    public static SyncGroupConfig createConfig(SynchronizedPointGroup group){
        List<ControlPointEditor> points = group.getPoints();
        List<SyncPointConfig> pointConfs = 
                new ArrayList<SyncPointConfig>(points.size());
        for(ControlPointEditor point : points){
            SyncPointConfig conf = createPointConfig(point);
            if(conf == null){
                continue;
            }
            pointConfs.add(conf);
        }
        if(pointConfs.isEmpty()){
            return null;
        }
        return new SyncGroupConfig(pointConfs);
    }
    
    static SyncPointConfig createPointConfig(ControlPointEditor point){
        if(point == null){
            return null;
        }
        AbstractEditor path = point.getParent();
        if(path == null){
            return null;
        }
        AbstractEditor channel = path.getParent();
        if(channel == null || !(channel instanceof ChannelEditor)){
            return null;
        }
        int chanId = ((ChannelEditor)channel).getId();
        int pathId = getChildIndex(channel, path);
        int pointId = getChildIndex(path, point);
        if(chanId < 0 || pathId < 0 || pointId < 0){
            return null;
        }
        return new SyncPointConfig(chanId, pathId, pointId);
    }
    
    static int getChildIndex(AbstractEditor parent, AbstractEditor child){
        List children = parent.getChildren();
        if(children == null){
            return -1;
        }
        return children.indexOf(child);
    }
    
    public static class SyncPointConfig {
        public int channelId;
        public int motionPathId;
        public int controlPointId;

        public SyncPointConfig(
                int channelId, int motionPathId, int controlPointId) {
            this.channelId = channelId;
            this.motionPathId = motionPathId;
            this.controlPointId = controlPointId;
        }
    }
    
    public static class SyncGroupConfig {
        public List<SyncPointConfig> points;

        public SyncGroupConfig(List<SyncPointConfig> points) {
            this.points = points;
        }
    }
}
