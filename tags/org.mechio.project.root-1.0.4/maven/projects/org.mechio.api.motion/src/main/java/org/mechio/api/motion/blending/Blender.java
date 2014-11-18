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
 * Uses a FrameCombiner to blend move requests, and sends the results to a 
 * BlenderOutput.
 * 
 * @param <MF> MotionFrame type used by this Blender
 * @param <FS> FrameSource type used by this Blender
 * @param <PosMap> PositionMap type used by this Blender
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface Blender<
        MF extends MotionFrame<PosMap>,
        FS extends FrameSource<PosMap>,
        PosMap extends JointPositionMap> {
    /**
     * Used for specifying the type of position map used by this blender.
     */
    public final static String PROP_POSITION_MAP_TYPE = "positionMapType";
    /**
     * Sets the Blender's FrameCombiner.
     * @param combiner FrameCombiner to set
     */
    public void setFrameCombiner(FrameCombiner<MF,FS,PosMap> combiner);
    /**
     * Returns the Blender's FrameCombiner.
     * @return Blender's FrameCombiner
     */
    public FrameCombiner<MF,FS,PosMap> getFrameCombiner();

    /**
     * Sets the Blender's BlenderOutput.
     * @param out BlenderOutput to set
     */
    public void setOutput(BlenderOutput<PosMap> out);
    
    /**
     * Uses the FrameCombiner to blend the given Frames, and sends the results 
     * to the BlenderOutput.
     * @param time time of the move request
     * @param interval time since the previous move request
     * @param frames map of Frames and their FrameSources
     */
    public void blend(long time, long interval, 
            Map<? extends MF, 
                    ? extends FS> frames);
}
