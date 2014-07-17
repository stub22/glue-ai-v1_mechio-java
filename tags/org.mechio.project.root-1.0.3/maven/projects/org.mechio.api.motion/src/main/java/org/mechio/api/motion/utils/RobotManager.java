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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.mechio.api.motion.Robot;

/**
 * Manages Robots and provides RobotControllers.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotManager extends PropertyChangeNotifier{
    /**
     * Property change event name for adding a Robot.
     */
    public final static String PROP_ADD_ROBOT = "addRobot";
    /**
     * Property change event name for removing a Robot.
     */
    public final static String PROP_REMOVE_ROBOT = "removeRobot";
    
    private Map<Robot.Id,RobotController> myControllerMap;
    private Map<Robot.Id,ServiceRegistration> myRegistrationMap;
    private List<RobotController> myControllerList;
    
    /**
     * Creates a new RobotManager.
     * @param context BundleContext for OSGi
     */
    public RobotManager(BundleContext context){
        if(context == null){
            throw new NullPointerException();
        }
        myControllerMap = new HashMap();
        myRegistrationMap = new HashMap();
        myControllerList = new ArrayList<RobotController>();
    }
    
    /**
     * Adds a RobotController for the given Robot.
     * @param robotId id of the Robot to add
     */
    public void addRobot(Robot robot){
        if(robot == null || robot.getRobotId() == null){
            throw new NullPointerException();
        }
        if(myControllerMap.containsKey(robot.getRobotId())){
            return;
        }
        RobotController controller = new RobotController(this);
        controller.setRobot(robot);
        myControllerMap.put(robot.getRobotId(), controller);
        myControllerList.add(controller);
        int index = myControllerList.indexOf(controller);
        fireIndexedPropertyChange(PROP_ADD_ROBOT, index, null, controller);
    }
    
    /**
     * Removes a Robot from the manager.  If the Robot's ServiceRegistration is
     * available, the Robot is unregistered from OSGi.
     * @param robotId robot to remove
     */
    public void removeRobot(Robot.Id robotId){
        RobotController rc = myControllerMap.remove(robotId);
        int index = -1;
        if(rc != null){
            rc.disconnectRobot();
            index = myControllerList.indexOf(rc);
            myControllerList.remove(rc);
        }
        ServiceRegistration reg = myRegistrationMap.remove(robotId);
        if(reg != null){
            reg.unregister();
        }
        fireIndexedPropertyChange(PROP_REMOVE_ROBOT, index, null, rc);
    }
    
    /**
     * Returns the Manager's Map of Robot Ids and RobotControllers.
     * @return Manager's Map of Robot Ids and RobotControllers
     */
    public Map<Robot.Id, RobotController> getControllerMap(){
        return myControllerMap;
    }
    
    /**
     * Returns a List of RobotControllers owned by this RobotManager.
     * @return List of RobotControllers owned by this RobotManager
     */
    public List<RobotController> getControllers(){
        return myControllerList;
    }
}
