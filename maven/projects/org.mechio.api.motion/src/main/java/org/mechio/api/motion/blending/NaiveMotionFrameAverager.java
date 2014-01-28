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

package org.mechio.api.motion.blending;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.utils.Utils;
import org.jflux.api.core.Source;
import org.mechio.api.motion.protocol.MotionFrame;
import org.mechio.api.motion.protocol.JointPositionMap;

/**
 * Naively combines MotionFrames by averaging the goal positions.
 * Ignores MotionFrame start positions and timing.
 * 
 * @param <Id> type of id used in the PositionMaps being combined
 * @param <PosMap> type of PositionMaps being combined
 * @author Matthew Stevenson <www.mechio.org>
 */
public class NaiveMotionFrameAverager<
        Id, PosMap extends JointPositionMap<Id,NormalizedDouble>> implements 
        FrameCombiner<MotionFrame<PosMap>,FrameSource<PosMap>,PosMap> {
    private Source<PosMap> myPositionMapFactory;    
    
    /**
     * Creates a new NaiveMotionFrameAverager/
     * @param posMapFact factory for creating new PositionMaps
     */
    public NaiveMotionFrameAverager(Source<PosMap> posMapFact){
        if(posMapFact == null){
            throw new NullPointerException();
        }
        myPositionMapFactory = posMapFact;
    }
    /**
     * Averages the goal positions from the MotionFrames into a single 
     * PositionMap.  The MotionFrames' start positions, timing, and 
     * FrameSources are ignored.
     * @return PositionMap with averaged goal positions from the MotionFrames
     */
    @Override
    public PosMap combineFrames(long time, long interval, PosMap curPos, 
    Map<? extends MotionFrame<PosMap>, ? extends FrameSource<PosMap>> frames) {
        //Use a HashMap instead of a PositionMap since the sum may be greater
        //than 1.0 before averaging.
        Map<Id, Double> posSums = new HashMap();
        Map<Id, Integer> count = new HashMap();
        
        for(MotionFrame f : frames.keySet()){
            sumGoalPositions(f, posSums, count);
        }
        PosMap pos = myPositionMapFactory.getValue();
        for(Entry<Id,Double> e : posSums.entrySet()){
            Id id = e.getKey();
            double goal = e.getValue();
            double c = count.get(id);
            double avg = goal/c;
            avg = Utils.bound(avg, 0.0, 1.0);
            NormalizedDouble val = new NormalizedDouble(avg);
            pos.put(id, val);
        }
        return pos;
    }

    private void sumGoalPositions(MotionFrame<PosMap> f, 
            Map<Id, Double> posSums, Map<Id, Integer> count) {
        PosMap goals = f.getGoalPositions();
        if(goals == null){
            return;
        }
        for(Entry<Id,NormalizedDouble> e : goals.entrySet()){
            Id id = (Id)e.getKey();
            NormalizedDouble goal = e.getValue();
            double val = goal.getValue();
            int c = 1;
            if(posSums.containsKey(id)){
                val += posSums.get(id);
                c += count.get(id);
            }
            posSums.put(id, val);
            count.put(id, c);
        }
    }
}
