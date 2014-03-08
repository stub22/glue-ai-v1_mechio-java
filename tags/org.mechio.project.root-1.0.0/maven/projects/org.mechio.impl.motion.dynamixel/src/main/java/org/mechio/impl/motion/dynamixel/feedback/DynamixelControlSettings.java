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
public class DynamixelControlSettings {
    private boolean myRunFlag;
    private boolean myMoveFlag;
    private boolean myCommandFlag;
    private boolean myUpdateFlag;
    private boolean myStatusReturnFlag;
    private int myReadCount;
    private int myReturnDelay;
    private double myMaxRunTemperature;
    private double myCooldownTemperature;
    private double myCommandSendDelay;
    private int myTimeoutLengthMillisec;

    public DynamixelControlSettings(
            int readCount, int returnDelay, 
            double maxRunTemperature, double cooldownTemperature,
            double commandSendDelay, int timeoutLengthMillisec) {
        myReadCount = readCount;
        myReturnDelay = returnDelay;
        myMaxRunTemperature = maxRunTemperature;
        myCooldownTemperature = cooldownTemperature;
        myRunFlag = false;
        myMoveFlag = true;
        myCommandFlag = true;
        myUpdateFlag = true;
        myStatusReturnFlag = true;
        myCommandSendDelay = commandSendDelay;
        myTimeoutLengthMillisec = timeoutLengthMillisec;
    }

    public boolean getCommandFlag() {
        return myCommandFlag;
    }

    public void setCommandFlag(boolean commandFlag) {
        myCommandFlag = commandFlag;
    }

    public double getCooldownTemperature() {
        return myCooldownTemperature;
    }

    public void setCooldownTemperature(double cooldownTemperature) {
        myCooldownTemperature = cooldownTemperature;
    }

    public double getMaxRunTemperature() {
        return myMaxRunTemperature;
    }

    public void setMaxRunTemperature(double maxRunTemperature) {
        myMaxRunTemperature = maxRunTemperature;
    }

    public boolean getMoveFlag() {
        return myMoveFlag;
    }

    public void setMoveFlag(boolean moveFlag) {
        myMoveFlag = moveFlag;
    }

    public int getReadCount() {
        return myReadCount;
    }

    public void setReadCount(int readCount) {
        myReadCount = readCount;
    }

    public int getReturnDelay() {
        return myReturnDelay;
    }

    public void setReturnDelay(int returnDelay) {
        myReturnDelay = returnDelay;
    }

    public boolean getRunFlag() {
        return myRunFlag;
    }

    public void setRunFlag(boolean runFlag) {
        myRunFlag = runFlag;
    }

    public boolean getStatusReturnFlag() {
        return myStatusReturnFlag;
    }

    public void setStatusReturnFlag(boolean statusReturnFlag) {
        myStatusReturnFlag = statusReturnFlag;
    }

    public boolean getUpdateFlag() {
        return myUpdateFlag;
    }

    public void setUpdateFlag(boolean updateFlag) {
        myUpdateFlag = updateFlag;
    }

    public double getCommandSendDelay() {
        return myCommandSendDelay;
    }

    public void setCommandSendDelay(double commandSendDelay) {
        myCommandSendDelay = commandSendDelay;
    }

    public int getTimeoutLengthMillisec() {
        return myTimeoutLengthMillisec;
    }

    public void setTimeoutLengthMillisec(int timeoutLengthMillisec) {
        myTimeoutLengthMillisec = timeoutLengthMillisec;
    }
}
