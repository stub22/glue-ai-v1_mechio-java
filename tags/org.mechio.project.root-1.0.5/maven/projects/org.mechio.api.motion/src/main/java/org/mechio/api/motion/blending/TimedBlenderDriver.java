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
import java.util.List;
import java.util.Map;
import org.jflux.api.common.rk.utils.TimerLoop;
import org.mechio.api.motion.protocol.MotionFrame;

/**
 * A TimedBlenderDriver runs in a TimerLoop and regularly pulls Frames from the
 * FrameSources in its FrameSourceTracker, and sends them to its Blender.
 * 
 * @author Matthew Stevenson
 */
public class TimedBlenderDriver extends TimerLoop {
    private Blender myBlender;
    private FrameSourceTracker mySourceTracker;

    /**
     * Creates a new TimedBlenderDriver to run at the given interval.
     * @param interval milliseconds between blending
     */
    public TimedBlenderDriver(long interval){
        super(interval);
    }

    /**
     * Sets the Blender to drive.
     * @param blender Blender to drive
     */
    public void setBlender(Blender blender){
        myBlender = blender;
    }

    /**
     * Set the FrameSourceTracker to use.
     * @param tracker FrameSourceTracker to use
     */
    public void setFrameSourceTracker(FrameSourceTracker tracker){
        mySourceTracker = tracker;
    }

    /**
     * Returns the FrameSourceTracker uses.
     * @return rameSourceTracker used
     */
    public FrameSourceTracker getFrameSourceTracker(){
        return mySourceTracker;
    }

    @Override
    protected void timerTick(long time, long interval) {
        if(mySourceTracker == null || myBlender == null){
            return;
        }
        List<FrameSource> sources = mySourceTracker.getSources();
        if(sources == null || sources.isEmpty()){
            return;
        }
        Map<MotionFrame,FrameSource> frames = collectFrames(time, interval, sources);
        myBlender.blend(time, interval, frames);
    }

    private Map<MotionFrame,FrameSource> collectFrames(long time, long interval, List<FrameSource> sources){
        Map<MotionFrame,FrameSource> frames = new HashMap();
        for(FrameSource source : sources){
            MotionFrame frame = source.getMovements(time, interval);
            if(frame == null){
                continue;
            }
            frames.put(frame, source);
        }
        return frames;
    }

}
