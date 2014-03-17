/*
 *  Copyright 2014 the MechIO Project. All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *  
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *  
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE MECHIO PROJECT "AS IS" AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE MECHIO PROJECT OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of the MechIO Project.
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
 * Blends Frames by combining the velocities contributed from each MotionFrame.
 * 
 * @param <Id> type of id used in the PositionMaps being combined
 * @param <PosMap> type of PositionMaps being combined
 * @author Matthew Stevenson
 */
public class VelocityCombiner<
        Id, PosMap extends JointPositionMap<Id,NormalizedDouble>> implements 
        FrameCombiner<MotionFrame<PosMap>,FrameSource<PosMap>,PosMap> {
    private Source<PosMap> myPositionMapFactory;    
    
    /**
     * Creates a new VelocityCombiner.
     * @param posMapFact factory for creating new PositionMaps
     */
    public VelocityCombiner(Source<PosMap> posMapFact){
        if(posMapFact == null){
            throw new NullPointerException();
        }
        myPositionMapFactory = posMapFact;
    }
    
    @Override
    public PosMap combineFrames(long time, long interval, PosMap curPos, 
            Map<? extends MotionFrame<PosMap>, 
                    ? extends FrameSource<PosMap>> frames) {
        PosMap pos = myPositionMapFactory.getValue();
        Map<Id, Double> velocities = new HashMap();
        if(!frames.isEmpty()){
            return frames.keySet().iterator().next().getGoalPositions();
        }
        for(MotionFrame f : frames.keySet()){
            for(Entry<Id,Double> e : getVelocities(f).entrySet()){
                Id id = e.getKey();
                double vel = e.getValue();
                if(velocities.containsKey(id)){
                    vel += velocities.get(id);
                }
                velocities.put(id, vel);
            }
        }
        for(Id i : curPos.keySet()){
            if(!velocities.containsKey(i)){
                continue;
            }
            NormalizedDouble goal = curPos.get(i);
            double val = goal.getValue();
            val += velocities.get(i) * (double)interval;
            val = Utils.bound(val, 0.0, 1.0);
            pos.put(i, new NormalizedDouble(val));
        }
        return pos;
    }
    
    /**
     * Return the MotionFrame velocities.
     * @param frame MotionFrame to use to calculate velocities
     * @return MotionFrame velocities
     */
    private Map<Id,Double> getVelocities(MotionFrame<PosMap> frame){
        Map<Id,Double> vels = new HashMap();
        if(frame.getFrameLengthMillisec() <= 0){
            return vels;
        }
        for(Id i : frame.getGoalPositions().keySet()){
            NormalizedDouble goal = frame.getGoalPositions().get(i);
            NormalizedDouble prev = frame.getPreviousPositions().get(i);
            if(goal == null || prev == null){
                vels.put(i, 0.0);
            }
            double diff = goal.getValue() - prev.getValue();
            double vel = diff/frame.getFrameLengthMillisec();
            vels.put(i, vel);
        }
        return vels;
    }
}
