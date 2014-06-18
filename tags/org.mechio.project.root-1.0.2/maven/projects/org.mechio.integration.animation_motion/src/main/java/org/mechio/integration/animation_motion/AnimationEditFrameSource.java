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

import java.util.Map;
import java.util.Map.Entry;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.api.animation.utils.AnimationEditListener;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.utils.PositionTargetFrameSource;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationEditFrameSource extends PositionTargetFrameSource implements AnimationEditListener{

    public AnimationEditFrameSource(Robot robot){
        super(0.001, null);
        setRobot(robot);
        setStopOnGoal(true);
    }
    
    @Override
    public void handlePositions(long x, Map<Integer, Double> positions) {
        Robot r = getRobot();
        if(r == null){
            return;
        }
        Robot.RobotPositionMap posMap = 
                new Robot.RobotPositionHashMap(positions.size());
        for(Entry<Integer,Double> e : positions.entrySet()){
            Integer id = e.getKey();
            Double rawPos = e.getValue();
            if(id == null || rawPos == null){
                continue;
            }else if(!NormalizedDouble.isValid(rawPos)){
                continue;
            }
            NormalizedDouble val = new NormalizedDouble(rawPos);
            Joint.Id jId = new Joint.Id(id);
            Robot.JointId djId = 
                    new Robot.JointId(r.getRobotId(), jId);
            posMap.put(djId, val);
        }
        this.putPositions(posMap);
        
    }

    @Override
    public void setEditEnabled(boolean val) {
        setEnabled(val);
    }
}
