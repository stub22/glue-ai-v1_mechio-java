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

package org.mechio.impl.motion.serial;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.mechio.api.motion.servos.ServoController;
import org.mechio.api.motion.servos.config.ServoControllerConfig;
import org.mechio.api.motion.servos.config.ServoConfig;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class SerialServoControllerConfig<Id, SC extends ServoConfig<Id>> extends 
        PropertyChangeNotifier implements 
        ServoControllerConfig<Id,SC>, PropertyChangeListener {

    /**
     * Property string for SerialServoControllerConfig port name.
     */
    public final static String PROP_PORT_NAME = "PortName";
    /**
     * Property string for SerialServoControllerConfig baud rate.
     */
    public final static String PROP_BAUD_RATE = "BaudRate";


    private ServoController.Id myServoControllerId;
    private VersionProperty myControllerType;
    private Map<Id,SC> myServoConfigMap;
    private String myPortName;
    private BaudRate myBaudRate;

    /**
     * Creates a new SerialServoControllerConfig with the given port name and baud 
     * rate.
     * @param portName port identifier
     * @param baud baud rate
     */
    public SerialServoControllerConfig(String portName, BaudRate baud){
        this();
        myPortName = portName;
        myBaudRate = baud;
    }

    /**
     * Creates an empty SerialServoControllerConfig.
     */
    protected SerialServoControllerConfig(){
        myServoConfigMap = new HashMap();
    }

    /**
     * Returns the controller type VersionProperty.
     * @return controller type VersionProperty
     */
    @Override
    public VersionProperty getControllerTypeVersion() {
        return myControllerType;
    }

    /**
     * Sets the controller type VersionProperty.
     * @param version controller type to set
     */
    @Override
    public void setControllerTypeVersion(VersionProperty version){
        VersionProperty oldVer = myControllerType;
        myControllerType = version;
        firePropertyChange(PROP_CONTROLLER_TYPE, oldVer, version);
    }

    /**
     * Returns the port identifier.
     * @return port identifier
     */
    public String getPortName(){
        return myPortName;
    }

    /**
     * Sets port identifier.
     * @param port port identifier to set
     */
    public void setPortName(String port){
        String oldPort = myPortName;
        myPortName = port;
        firePropertyChange(PROP_PORT_NAME, oldPort, port);
    }

    /**
     * Returns port baud rate.
     * @return port baud rate
     */
    public BaudRate getBaudRate(){
        return myBaudRate;
    }

    /**
     * Sets port baud rate.
     * @param rate BaudRate to set
     */
    public void setBaudRate(BaudRate rate){
        BaudRate oldRate = myBaudRate;
        myBaudRate = rate;
        firePropertyChange(PROP_BAUD_RATE, oldRate, rate);
    }

    @Override
    public void addServoConfig(SC config){
        Id id = config.getServoId();
        if(myServoConfigMap.containsKey(id)){
            return;
        }
        myServoConfigMap.put(id, config);
        firePropertyChange(PROP_SERVO_ADD, null, config);
    }

    @Override
    public void removeServoConfig(SC config) {
        Id id = config.getServoId();
        if(!myServoConfigMap.containsKey(id)){
            return;
        }
        myServoConfigMap.remove(id);
        firePropertyChange(PROP_SERVO_REMOVE, null, config);
    }

    @Override
    public int getServoCount() {
        return myServoConfigMap.size();
    }

    @Override
    public Map<Id,SC> getServoConfigs(){
        return Collections.unmodifiableMap(myServoConfigMap);
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if(ServoConfig.PROP_ID.equals(name)){
            changeServoId(
                    (Id)evt.getOldValue(), 
                    (Id)evt.getNewValue());
            return;
        }
    }

    private void changeServoId(Id oldId, Id newId){
        SC config = myServoConfigMap.remove(oldId);
        myServoConfigMap.put(newId, config);
        firePropertyChange(PROP_SERVOS, null, myServoConfigMap);
    }

    @Override
    public ServoController.Id getServoControllerId() {
        return myServoControllerId;
    }

    public void setServoControllerId(ServoController.Id scId) {
        myServoControllerId = scId;
    }
}
