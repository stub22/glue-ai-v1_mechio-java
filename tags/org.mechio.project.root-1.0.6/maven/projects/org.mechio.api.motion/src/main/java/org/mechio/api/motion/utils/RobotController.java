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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;

/**
 * Used by UI components to abstract away controlling a Robot.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotController extends PropertyChangeNotifier implements PropertyChangeListener{
    /**
     * Property String for the RobotController's Robot Id.
     */
    public final static String PROP_ROBOT = "robot";
    
    private Robot myRobot;
    private PositionTargetFrameSource myRobotMover;
    private RobotManager myManager;
    
    /**
     * Creates a new RobotController.
     * @param manager RobotManager to manage this RobotController
     */
    public RobotController(RobotManager manager){
        if(manager == null){
            throw new NullPointerException();
        }
        myManager = manager;
    }
    
    /**
     * Sets the id of the Robot to control.
     * @param robotId id of the robot to control
     */
    public void setRobot(Robot robot){
        Robot oldVal = getRobot();
        unsetRobot();
        myRobot = robot;
        if(myRobotMover != null){
            myRobotMover.setRobot(myRobot);
        }
        if(myRobot != null){
            myRobot.addPropertyChangeListener(this);
        }
        firePropertyChange(PROP_ROBOT, oldVal, myRobot);
    }
    
    private void unsetRobot(){
        Robot r = getRobot();
        if(r != null){
            r.removePropertyChangeListener(this);
        }
        if(myRobotMover != null){
            myRobotMover.setRobot(null);
        }
    }
    
    /**
     * Returns the Robot being controlled, null if it is unavailable.
     * @return Robot being controlled, null is it is unavailable
     */
    public Robot getRobot(){
        return myRobot;
    }
    
    /**
     * Connects the robot
     * @return true if successful
     */
    public boolean connectRobot(){
        Robot r = getRobot();
        if(r == null){
            return false;
        }
        if(r.isConnected()){
            return true;
        }
        return r.connect();
    }
    
    /**
     * Disconnects the Robot.
     * @return true if successful
     */
    public boolean disconnectRobot(){
        Robot r = getRobot();
        if(r == null){
            return false;
        }
        if(!r.isConnected()){
            return true;
        }
        r.disconnect();
        return !r.isConnected();
    }
    
    /**
     * Enables the Robot.
     * @return true if successful
     */
    public boolean enableRobot(){
        Robot r = getRobot();
        if(r == null){
            return false;
        }
        if(r.isEnabled()){
            return true;
        }
        r.setEnabled(true);
        return true;
    }
    
    /**
     * Disables the Robot.
     * @return true if successful
     */
    public boolean disableRobot(){
        Robot r = getRobot();
        if(r == null){
            return false;
        }
        if(!r.isEnabled()){
            return true;
        }
        r.setEnabled(false);
        return true;
    }
    
    /**
     * Unused
     * @return
     */
    public boolean selectRobot(){
        Robot r = getRobot();
        if(r == null){
            return false;
        }
        return false;
    }
    
    /**
     * Moves the Robot to its default positions.
     * @return true if successful
     */
    public boolean setDefaultPositions(){
        Robot r = getRobot();
        if(r == null || myRobotMover == null){
            return false;
        }
        RobotPositionMap defPos = r.getDefaultPositions();
        myRobotMover.putPositions(defPos);
        return true;
    }
    
    /**
     * Stops controlling the Robot.  setRobotId must be called for a new Robot
     * to be set.
     * @return true if successful
     */
    public boolean removeRobot(){
        unsetRobot();
        return true;
    }
    
    public void setRobotMover(PositionTargetFrameSource mover){
        myRobotMover = mover;
        if(myRobotMover != null && myRobotMover.getRobot() != myRobot){
            myRobotMover.setRobot(myRobot);
        }
        
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt);
    }
}
