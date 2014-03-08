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

import org.jflux.api.common.rk.property.PropertyChangeNotifier;

/**
 * Default implementation for ServoConfig.  The DefaultServoConfig adds a 
 * PhysicalId property.
 * 
 * @param <Id> Servo Id Type used
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DefaultServoConfig<Id> extends 
        PropertyChangeNotifier implements ServoConfig<Id>{
    
    private Id myServoId;
    private String myName;
    private int myMinPosition;
    private int myMaxPosition;
    private int myDefaultPosition;

    /**
     * Creates a new ServoConfig with the given parameters.
     * @param servoId The Servo's Id with respect to the ServoController
     * @param name Servo name (used for display purposes)
     * @param minPos minimum position in absolute terms for the ServoController
     * @param maxPos maximum position in absolute terms for the ServoController
     * @param defPos default position in absolute terms for the ServoController
     */
    public DefaultServoConfig(Id servoId, String name,
            int minPos, int maxPos, int defPos){
        myServoId = servoId;
        myName = name;
        myMinPosition = minPos;
        myMaxPosition = maxPos;
        checkAbsPosition(defPos);
        myDefaultPosition = defPos;
    }

    /**
     * Returns the Servo id.
     * @return Servo id
     */
    @Override
    public Id getServoId(){
        return myServoId;
    }

    /**
     * Sets the Servo id.
     * @param id new Servo id
     */
    @Override
    public void setServoId(Id id){
        if(id == null){
            throw new NullPointerException("Cannot set null Servo id.");
        }
        Id oldId = myServoId;
        myServoId = id;
        firePropertyChange(ServoConfig.PROP_ID, oldId, id);
    }

    /**
     * Returns the Servo name.
     * @return Servo name
     */
    @Override
    public String getName(){
        return myName;
    }

    /**
     * Sets the Servo name.
     * @param name new Servo name
     */
    @Override
    public void setName(String name){
        String oldName = myName;
        myName = name;
        firePropertyChange(ServoConfig.PROP_NAME, oldName, name);
    }

    /**
     * Returns the Servo minimum position.
     * @return Servo minimum position
     */
    @Override
    public int getMinPosition(){
        return myMinPosition;
    }

    /**
     * Sets the Servo minimum position.
     * @param pos new Servo minimum position
     */
    @Override
    public void setMinPosition(Integer pos){
        if(pos == null){
            throw new NullPointerException("Cannot set null position.");
        }
        Integer oldPos = myMinPosition;
        myMinPosition = pos;
        firePropertyChange(ServoConfig.PROP_MIN_POSITION, oldPos, pos);
    }

    /**
     * Returns the Servo maximum position.
     * @return Servo maximum position
     */
    @Override
    public int getMaxPosition(){
        return myMaxPosition;
    }

    /**
     * Sets the Servo maximum position.
     * @param pos new Servo maximum position
     */
    @Override
    public void setMaxPosition(Integer pos){
        if(pos == null){
            throw new NullPointerException("Cannot set null position.");
        }
        Integer oldPos = myMaxPosition;
        myMaxPosition = pos;
        firePropertyChange(ServoConfig.PROP_MAX_POSITION, oldPos, pos);
    }

    /**
     * Returns the Servo default position.
     * @return Servo default position
     */
    @Override
    public int getDefaultPosition(){
        return myDefaultPosition;
    }
    /**
     * Sets the Servo default position.
     * @param pos new Servo default position
     */
    @Override
    public void setDefaultPosition(Integer pos){
        if(pos == null){
            throw new NullPointerException("Cannot set null position.");
        }
        checkAbsPosition(pos);
        Integer oldPos = myDefaultPosition;
        myDefaultPosition = pos;
        firePropertyChange(ServoConfig.PROP_DEF_POSITION, oldPos, pos);
    }
    
    private void checkAbsPosition(int pos){
        int max = Math.max(myMinPosition, myMaxPosition);
        int min = Math.min(myMinPosition, myMaxPosition);
        if(pos < min || pos > max){
            throw new IllegalArgumentException("Position (" + pos + 
                    ") out of range [" + min +", " + max + "].");
        }
    }
}
