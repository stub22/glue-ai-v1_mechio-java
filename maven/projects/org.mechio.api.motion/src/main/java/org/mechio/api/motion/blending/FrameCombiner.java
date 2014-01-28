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

import org.mechio.api.motion.protocol.MotionFrame;
import java.util.Map;
import org.mechio.api.motion.protocol.JointPositionMap;

/**
 * A FrameCombiner combines movement requests from multiple FrameSources into a 
 * single set of Joint movements.
 * @param <F> MotionFrame type
 * @param <B> FrameSource type
 * @param <PosMap> PositionMap type
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface FrameCombiner<
        F extends MotionFrame<PosMap>, 
        B extends FrameSource<PosMap>, 
        PosMap extends JointPositionMap>{
    /**
     * Combines Frames from FrameSources into a map of Joint positions.
     * @param time time of the move request
     * @param interval time since last move request
     * @param curPos set of current Joint positions
     * @param frames map of Frames and their originating FrameSources
     * @return a map of Joint positions
     */
    public  PosMap combineFrames(long time, long interval, 
            PosMap curPos, Map<? extends F, ? extends B> frames);
}
