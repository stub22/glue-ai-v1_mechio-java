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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Basic implementation of a Blender.
 *
 * @param <MF>     MotionFrame type used by this DefaultBlender
 * @param <FS>     FrameSource type used by this DefaultBlender
 * @param <PosMap> PositionMap type used by this DefaultBlender
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DefaultBlender<
		MF extends MotionFrame<PosMap>,
		FS extends FrameSource<PosMap>,
		PosMap extends JointPositionMap> implements Blender<MF, FS, PosMap> {
	private static final Logger theLogger = LoggerFactory.getLogger(DefaultBlender.class);
	private FrameCombiner<MF, FS, PosMap> myFrameCombiner;
	private BlenderOutput<PosMap> myOutput;

	@Override
	public void setFrameCombiner(FrameCombiner<MF, FS, PosMap> combiner) {
		myFrameCombiner = combiner;
	}

	@Override
	public FrameCombiner<MF, FS, PosMap> getFrameCombiner() {
		return myFrameCombiner;
	}

	@Override
	public void setOutput(BlenderOutput<PosMap> out) {
		myOutput = out;
	}

	@Override
	public void blend(long time, long interval, Map<? extends MF, ? extends FS> frames) {
		if (frames == null || frames.isEmpty() || myFrameCombiner == null ||
				myOutput == null) {
			return;
		}
		PosMap curPos = myOutput.getPositions();
		if (curPos == null || curPos.isEmpty()) {
			return;
		}
		PosMap pos = myFrameCombiner.combineFrames(time, interval, curPos, frames);
		myOutput.write(pos, interval);
	}
}
