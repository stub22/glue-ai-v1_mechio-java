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
package org.mechio.api.motion.servos.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.servos.ServoController;
import org.mechio.api.motion.servos.ServoController.ServoId;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public final class JointServoMap {
    private final static Logger theLogger = 
            Logger.getLogger(JointServoMap.class.getName());
    private List<Joint.Id> myJointIds;//Maintains joint order
    private Map<Joint.Id,ServoId<String>> myJointMap;
    private Map<ServoId<String>,Joint.Id> myServoMap;
    private Map<ServoController.Id,List<ServoId<String>>> myControllerMap;
    
    public JointServoMap(){
        myJointIds = new ArrayList();
        myJointMap = new HashMap();
        myServoMap = new HashMap();
        myControllerMap = new HashMap();
    }

    public void addServoId(Joint.Id jointId, 
            ServoController.Id controllerId, String servoId){
        if(myJointMap.containsKey(jointId)){
            theLogger.log(Level.WARNING, 
                    "Unable to add servo id, Joint id already exists. "
                    + "joint: {0}, controller: {1}, servo {2}", 
                    new Object[]{jointId, controllerId, servoId});
            return;
        }
        ServoId<String> id = new ServoId<String>(controllerId, servoId);
        if(myServoMap.containsKey(id)){
            theLogger.log(Level.WARNING, 
                    "Unable to add servo id, already in use. "
                    + "joint: {0}, controller: {1}, servo {2}", 
                    new Object[]{jointId, controllerId, servoId});
            return;
        }
        myJointIds.add(jointId);
        myJointMap.put(jointId, id);
        myServoMap.put(id, jointId);
        List<ServoId<String>> ids = myControllerMap.get(controllerId);
        if(ids == null){
            ids = new ArrayList<ServoId<String>>();
            myControllerMap.put(controllerId, ids);
        }
        ids.add(id);
    }
    
    public List<Joint.Id> getJointIds(){
        return myJointIds;
    }
    
    public List<ServoId<String>> getServoIds(
            ServoController.Id controllerId){
        return myControllerMap.get(controllerId);
    }
}
