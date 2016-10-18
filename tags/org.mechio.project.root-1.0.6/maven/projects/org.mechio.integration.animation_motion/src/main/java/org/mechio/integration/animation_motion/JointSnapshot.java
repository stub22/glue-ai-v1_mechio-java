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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.impl.services.rk.osgi.ClassTracker;
import org.osgi.framework.BundleContext;
import org.mechio.api.animation.editor.AnimationEditor;
import org.mechio.api.animation.editor.ChannelEditor;
import org.mechio.api.animation.editor.ControlPointEditor;
import org.mechio.api.animation.editor.EditState;
import org.mechio.api.animation.editor.MotionPathEditor;
import org.mechio.api.animation.editor.features.SynchronizedPointGroup;
import org.mechio.api.animation.editor.history.HistoryActionGroup;
import org.mechio.api.animation.utils.PositionAdder;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.JointProperty;
import org.mechio.api.motion.joint_properties.ReadCurrentPosition;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class JointSnapshot implements PositionAdder{
    private ClassTracker<Robot> myRobotTracker;
    private Robot.Id myRobotId;
    private BundleContext myContext;
    
    public JointSnapshot(BundleContext context, Robot.Id robotId){
        if(context == null || robotId == null){
            throw new NullPointerException();
        }
        myContext = context;
        myRobotId = robotId;
    }
    
    private ClassTracker<Robot> getRobotTracker(){
        if(myRobotTracker != null){
            return myRobotTracker;
        }
        myRobotTracker = new ClassTracker<Robot>(myContext, Robot.class.getName(),null,null);
        return myRobotTracker;
    }
    
    public Robot getRobot(){
        ClassTracker<Robot> tracker = getRobotTracker();
        return tracker == null ? null : tracker.getTopService();
    }
    
    private Map<Integer,Double> getRobotSnapshot(){
        Robot<Joint> robot = getRobot();
        if(robot == null){
            return Collections.EMPTY_MAP;
        }
        return buildSnapshot(robot.getJointList());
    }
    
    private Map<Integer,Double> getRobotSnapshot(int...ids){
        Robot robot = getRobot();
        if(robot == null || ids.length == 0){
            return Collections.EMPTY_MAP;
        }
        List<Joint> joints = new ArrayList<Joint>(ids.length);
        for(int id : ids){
            joints.add(robot.getJoint(
                    new Robot.JointId(myRobotId, new Joint.Id(id))));
        }
        return buildSnapshot(joints);
    }
    
    private Map<Integer,Double> buildSnapshot(List<Joint> joints){
        if(joints == null || joints.isEmpty()){
            return Collections.EMPTY_MAP;
        }
        Map<Integer,Double> myPositions = new HashMap<Integer, Double>();
        for(Joint joint : joints){
            int id = joint.getId().getLogicalJointNumber();
            if(joint == null || myPositions.containsKey(id)){
                continue;
            }
            NormalizedDouble n = getJointPosition(joint);
            if(n == null){
                continue;
            }
            double pos = n.getValue();
            myPositions.put(id, pos);
        }
        return myPositions;
    }
    
    private NormalizedDouble getJointPosition(Joint joint){
        JointProperty prop = 
                joint.getProperty(ReadCurrentPosition.PROPERTY_NAME);
        if(prop == null){
            return null;
        }
        NormalizableRange range = prop.getNormalizableRange();
        Object val = prop.getValue();
        if(range == null || val == null){
            return null;
        }
        return range.normalizeValue(val);
    }
    
    @Override
    public void addPositions(AnimationEditor editor, double x){
        Map<Integer,Double> positions = getRobotSnapshot();
        addSnapshot(editor, x, positions);
    }
    
    public void addSnapshot(AnimationEditor editor, double x, int...ids){
        Map<Integer,Double> positions = getRobotSnapshot(ids);
        addSnapshot(editor, x, positions);
    }
    
    private void addSnapshot(AnimationEditor editor, double x, Map<Integer,Double> positions){
        if(editor == null){
            return;
        }
        List<ChannelEditor> chans = editor.getChildren();
        if(chans == null || chans.isEmpty()){
            return;
        }
        List<ControlPointEditor> points = new ArrayList<ControlPointEditor>();
        HistoryActionGroup addPos = new HistoryActionGroup("Add Positions", true);
        for(ChannelEditor chan : chans){
            int id = chan.getId();
            Double pos = positions.get(id);
            if(pos == null){
                continue;
            }
            MotionPathEditor path = getBestPath(chan, x);
            if(path == null){
                continue;
            }
            int i = path.addChild(this, new Point2D.Double(x, pos), addPos);
            ControlPointEditor point = path.getChild(i);
            if(point != null){
                points.add(point);
            }
        }
        if(!points.isEmpty()){
            SynchronizedPointGroup group =
                    new SynchronizedPointGroup(
                            points, editor.getSharedHistory(), addPos);
        }
    }
    
    private MotionPathEditor getBestPath(ChannelEditor chan, double x){
        if(chan.hasFlag(EditState.SELECTED)){
            MotionPathEditor path = chan.getSelected();
            if(path != null){
                return path;
            }
        }
        List<MotionPathEditor> paths = chan.getChildren();
        double bestDist = Double.MAX_VALUE;
        MotionPathEditor bestPath = null;
        for(MotionPathEditor path : paths){
            double start = path.getStart();
            double end = path.getEnd();
            if(x >= start && x <= end){
                return path;
            }
            double dist = (x < start) ? start-x : x-end;
            if(dist < bestDist){
                bestDist = dist;
                bestPath = path;
            }
        }
        return bestPath;
    }
}
