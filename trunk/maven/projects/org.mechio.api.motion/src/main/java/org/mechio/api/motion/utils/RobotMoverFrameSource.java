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

package org.mechio.api.motion.utils;

import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.utils.Utils;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.JointId;
import org.mechio.api.motion.Robot.RobotPositionHashMap;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.protocol.DefaultMotionFrame;
import org.mechio.api.motion.protocol.MotionFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;

/**
 * A FrameSource which acts as a Robot move proxy.
 * Accepts movements as a RobotPositionMap and time length.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotMoverFrameSource implements RobotFrameSource {
	private static final Logger theLogger = LoggerFactory.getLogger(RobotMoverFrameSource.class);
	private Robot myRobot;
	private RobotPositionMap myPreviousPositions;
	private RobotPositionMap myGoalPositions;
	private long myMoveLengthMsec;
	private long myMoveStartTime;
	private boolean myFinsihedFlag;

	/**
	 * Creates a new RobotMoverFrameSource for the given Robot.
	 *
	 * @param robot Robot to move
	 */
	public RobotMoverFrameSource(Robot robot) {
		myRobot = robot;
	}

	@Override
	public void setRobot(Robot robot) {
		myRobot = robot;
	}

	@Override
	public Robot getRobot() {
		return myRobot;
	}

	/**
	 * Moves the Robot to the given positions in the given time length.
	 *
	 * @param positions   goal positions
	 * @param lenMillisec length of the movement in milliseconds
	 */
	public void move(RobotPositionMap positions, long lenMillisec) {
		if (positions == null || positions.isEmpty()) {
			myGoalPositions = null;
			return;
		}
		myFinsihedFlag = false;
		myGoalPositions = positions;
		myMoveLengthMsec = lenMillisec;
		myMoveStartTime = 0L;
	}

	@Override
	public MotionFrame getMovements(long currentTimeUTC, long moveLengthMilliSec) {
		if (myRobot == null || myGoalPositions == null ||
				myMoveLengthMsec == 0L || myFinsihedFlag) {
			return null;
		}
		if (myMoveStartTime == 0L) {
			myMoveStartTime = currentTimeUTC;
			myPreviousPositions = myRobot.getCurrentPositions();
		} else if (myPreviousPositions == null) {
			myPreviousPositions = myRobot.getCurrentPositions();
		}
		long elapsed = currentTimeUTC + moveLengthMilliSec - myMoveStartTime;
		elapsed = Utils.bound(elapsed, 0L, myMoveLengthMsec);
		if (elapsed == myMoveLengthMsec) {
			myFinsihedFlag = true;
		}
		double timeRatio = (double) elapsed / myMoveLengthMsec;
		MotionFrame frame = new DefaultMotionFrame();
		frame.setTimestampMillisecUTC(currentTimeUTC);
		frame.setFrameLengthMillisec(moveLengthMilliSec);
		frame.setPreviousPositions(new RobotPositionHashMap(myPreviousPositions));
		RobotPositionMap goals = new Robot.RobotPositionHashMap(myGoalPositions.size());
		for (Entry<JointId, NormalizedDouble> e : myGoalPositions.entrySet()) {
			JointId jointId = e.getKey();
			NormalizedDouble cur = myPreviousPositions.get(jointId);
			NormalizedDouble goal = e.getValue();
			if (goal == null) {
				continue;
			} else if (cur == null) {
				cur = goal;
			}
			double curVal = cur.getValue();
			double diff = goal.getValue() - curVal;
			double step = diff * timeRatio + curVal;
			NormalizedDouble interval = new NormalizedDouble(step);
			goals.put(jointId, interval);
		}
		frame.setGoalPositions(goals);
		return frame;
	}
}
