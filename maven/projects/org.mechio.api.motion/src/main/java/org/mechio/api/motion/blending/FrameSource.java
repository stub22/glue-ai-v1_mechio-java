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

import org.mechio.api.motion.protocol.JointPositionMap;
import org.mechio.api.motion.protocol.MotionFrame;

/**
 * A FrameSource provided desired movements to a robot.
 * Typically a FrameSource will be called at a regular interval 
 * (~40 milliseconds) for the next moves to make.
 * 
 * @param <PosMap> Type of JointPositionMap returned by this FrameSource
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface FrameSource<PosMap extends JointPositionMap>{
    /**
     * Returns the desired Robot movement starting at the given time and lasting
     * for the given number of milliseconds.
     * The frequency and regularity of calls will depend on the Blender 
     * configuration used.  The DefaultBlender runs in a continuous loop calling
     * FrameSources at 40 millisecond intervals.
     * 
     * @param currentTimeUTC time of the move request
     * @param moveLengthMilliSec expected length of the movement returned, the
     * actual returned length does not have to be same, but the actual movement
     * results will depend on the Blender used
     * @return MotionFrame desired robot movement
     */
    public MotionFrame<PosMap> getMovements(
            long currentTimeUTC, long moveLengthMilliSec);
}
