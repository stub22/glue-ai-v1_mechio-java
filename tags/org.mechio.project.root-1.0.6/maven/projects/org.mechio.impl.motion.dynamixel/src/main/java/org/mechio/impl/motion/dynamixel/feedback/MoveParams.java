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
package org.mechio.impl.motion.dynamixel.feedback;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface MoveParams<Id> {
    public Id getServoId();
    
    public void goalsSent();
    /**
     * Command Delay is the estimated number of milliseconds between sending a
     * move command and when the servo responds to the command.
     */    
    public long getCommandDelayMillisec();

    public int getCurrentPosition();

    public int getCurrentSpeed();

    public int getCurrentVoltage();

    public int getCurrentTemperature();

    public int getCurrentLoad();
    
    public long getCurPosTimestampUTC();

    public long getPrevGoalTargetTimeUTC();

    public int getPrevGoalPosition();

    public long getGoalTargetTimeUTC();

    public int getGoalPosition();
}
