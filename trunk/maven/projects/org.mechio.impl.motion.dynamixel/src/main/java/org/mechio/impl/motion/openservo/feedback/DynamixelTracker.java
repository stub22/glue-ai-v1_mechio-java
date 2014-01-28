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
package org.mechio.impl.motion.openservo.feedback;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jflux.api.core.Source;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.jflux.impl.services.rk.osgi.SingleServiceListener;
import org.osgi.framework.BundleContext;
import org.mechio.api.motion.servos.ServoController;
import org.mechio.impl.motion.dynamixel.DynamixelController;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlLoop;
import org.mechio.impl.motion.rxtx.serial.RXTXSerialPort;

/**
 *
 * @author matt
 */
public class DynamixelTracker implements Source<RXTXSerialPort>{
    private SingleServiceListener<ServoController> myTracker;

    public DynamixelTracker(final OpenServoControlLoop osLoop){
        BundleContext context = OSGiUtils.getBundleContext(ServoController.class);
        myTracker = new SingleServiceListener<ServoController>(
                ServoController.class, context, 
                OSGiUtils.createFilter(ServoController.PROP_VERSION, DynamixelController.VERSION.toString()));
        myTracker.addPropertyChangeListener(SingleServiceListener.PROP_SERVICE_TRACKED, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                DynamixelController controller = (DynamixelController)evt.getNewValue();
                if(controller == null){
                    throw new IllegalArgumentException();
                }
                DynamixelControlLoop loop = controller.getControlLoop();
                if(loop == null){
                    throw new IllegalArgumentException();
                }
                loop.setOpenServoLoop(osLoop);
            }
        });
        myTracker.start();
    }
    
    @Override
    public RXTXSerialPort getValue() {
        if(myTracker == null){
            return null;
        }
        DynamixelController c = (DynamixelController)myTracker.getService();
        if(c == null){
            return null;
        }
        return c.getPort();
    }
}
