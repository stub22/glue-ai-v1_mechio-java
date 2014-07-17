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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData.Array;
import org.jflux.api.common.rk.config.VersionProperty;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.Channel;
import org.mechio.api.animation.MotionPath;
import org.mechio.api.interpolation.InterpolatorDirectory;
import org.mechio.api.interpolation.InterpolatorFactory;

/**
 * Utilities for converting between Animations and AnimationRecords.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationMessagingUtils {    
    public static AnimationRecord packAnimation(Animation animation){
        if(animation == null){
            throw new NullPointerException();
        }
        AnimationRecord animRec = new AnimationRecord();
        animRec.setName(animation.getVersion().getName());
        animRec.setVersionNumber(animation.getVersion().getNumber());
        animRec.setStartTime(animation.getStartTime());
        animRec.setStopTime(animation.getStopTime());
        Schema arraySchema = Schema.createArray(ChannelRecord.SCHEMA$);
        List<Channel> chanList = animation.getChannels();
        GenericArray<ChannelRecord> channels = 
                new Array<ChannelRecord>(chanList.size(), arraySchema);
        for(Channel chan : chanList){
            channels.add(packChannel(chan));
        }
        animRec.setChannels(channels);
        return animRec;
    }
    
    private static ChannelRecord packChannel(Channel channel){
        ChannelRecord chanRec = new ChannelRecord();
        chanRec.setName(channel.getName());
        chanRec.setChannelId(channel.getId());
        chanRec.setStartTime(channel.getStartTime());
        chanRec.setStopTime(channel.getStopTime());
        Schema arraySchema = Schema.createArray(MotionPathRecord.SCHEMA$);
        List<MotionPath> pathList = channel.getMotionPaths();
        GenericArray<MotionPathRecord> paths = 
                new Array<MotionPathRecord>(pathList.size(), arraySchema);
        int i=0;
        for(MotionPath mp : pathList){
            paths.add(packMotionPath(mp, i++));
        }
        chanRec.setMotionPaths(paths);
        return chanRec;
    }
    
    private static MotionPathRecord packMotionPath(MotionPath path, int id){
        MotionPathRecord pathRec = new MotionPathRecord();
        pathRec.setName(path.getName());
        pathRec.setMotionPathId(id);
        pathRec.setStartTime(path.getStartTime());
        pathRec.setStopTime(path.getStopTime());
        VersionProperty interpVersion = path.getInterpolatorVersion();
        pathRec.setInterpolator(packInterpolatorType(interpVersion));
        Schema arraySchema = Schema.createArray(ControlPointRecord.SCHEMA$);
        List<Point2D> pointList = path.getControlPoints();
        GenericArray<ControlPointRecord> points = 
                new Array<ControlPointRecord>(pointList.size(), arraySchema);
        for(Point2D p : pointList){
            points.add(packControlPoint(p));
        }
        pathRec.setControlPoints(points);
        return pathRec;
    }
    
    private static InterpolatorTypeRecord packInterpolatorType(
            VersionProperty prop){
        InterpolatorTypeRecord interp = new InterpolatorTypeRecord();
        interp.setName(prop.getName());
        interp.setVersionNumber(prop.getNumber());
        return interp;
    }
    
    private static ControlPointRecord packControlPoint(Point2D point){
        ControlPointRecord pointRec = new ControlPointRecord();
        pointRec.setTime((long)point.getX());
        pointRec.setPosition(point.getY());
        return pointRec;
    }
    
    public static Animation unpackAnimation(AnimationRecord animRec){
        String name = animRec.getName();
        String number = animRec.getVersionNumber();
        VersionProperty animVersion = new VersionProperty(name, number);
        Animation anim = new Animation(animVersion);
        for(ChannelRecord chanRec : animRec.getChannels()){
            anim.addChannel(unpackChannel(chanRec));
        }
        if(animRec.getStartTime() != null){
            anim.setStartTime(animRec.getStartTime());
        }
        
        if(animRec.getStopTime() != null){
            anim.setStopTime(animRec.getStopTime());
        }
        return anim;
    }
    
    private static Channel unpackChannel(ChannelRecord chanRec){
        Channel chan = new Channel(chanRec.getChannelId(), chanRec.getName());
        for(MotionPathRecord pathRec : chanRec.getMotionPaths()){
            chan.addPath(unpackMotionPath(pathRec));
        }
        if(chanRec.getStartTime() != null){
            chan.setStartTime(chanRec.getStartTime());
        }
        if(chanRec.getStopTime() != null){
            chan.setStopTime(chanRec.getStopTime());
        }
        return chan;
    }
    
    private static MotionPath unpackMotionPath(MotionPathRecord pathRec){
        String interpName = pathRec.getInterpolator().getName();
        String interpNumber = pathRec.getInterpolator().getVersionNumber();
        VersionProperty interp = new VersionProperty(interpName, interpNumber);
        InterpolatorFactory fact = 
                InterpolatorDirectory.instance().getFactory(interp);
        MotionPath path = new MotionPath(fact);
        path.setName(pathRec.getName());
        int pointCount = pathRec.getControlPoints().size();
        List<Point2D> points = new ArrayList<Point2D>(pointCount);
        for(ControlPointRecord pr : pathRec.getControlPoints()){
            points.add(unpackControlPoint(pr));
        }
        path.addPoints(points);
        if(pathRec.getStartTime() != null){
            path.setStartTime(pathRec.getStartTime());
        }
        if(pathRec.getStopTime() != null){
            path.setStopTime(pathRec.getStopTime());
        }
        return path;
    }
    
    private static Point2D unpackControlPoint(ControlPointRecord pointRec){
        return new Point2D.Double(pointRec.getTime(), pointRec.getPosition());
    }
}
