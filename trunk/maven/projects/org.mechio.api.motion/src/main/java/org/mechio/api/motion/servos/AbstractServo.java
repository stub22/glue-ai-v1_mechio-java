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

package org.mechio.api.motion.servos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.mechio.api.motion.servos.config.ServoConfig;
import org.mechio.api.motion.servos.config.ServoControllerConfig;

/**
 * Abstract implementation providing much of the functionality need to implement
 * various Servos.
 * @param <Id> Identifier type for this Servo
 * @param <Conf> ServoConfig type for this Servo
 * @param <Ctrl> Servo's parent ServoController's type
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class AbstractServo<
        Id, 
        Conf extends ServoConfig<Id>, 
        Ctrl extends ServoController<Id, Conf,
                        ? extends AbstractServo<Id,Conf,Ctrl>,
                        ? extends ServoControllerConfig<Id,Conf>>>
        extends PropertyChangeNotifier 
        implements Servo<Id,Conf>, PropertyChangeListener{
    private static final Logger theLogger = Logger.getLogger(AbstractServo.class.getName());
    
    /**
     * The Servo's Id
     */
    protected Id myServoId;
    /**
     * The Servo's configuration parameters.
     */
    protected Conf myConfig;
    /**
     * The Servo's parent ServoController.
     */
    protected Ctrl myController;
    /**
     * The Servo's current goal position.
     */
    protected NormalizedDouble myGoalPosition;
    
    private NormalizableRange<Double> myRange;

    /**
     * Creates a new AbstractServo from the given Servo configuration parameters
     * and ServoController.
     * @param config Servo configuration parameters
     * @param controller Servo's parent ServoController
     */
    public AbstractServo(Conf config, Ctrl controller){
        if(config == null){
            throw new NullPointerException("Cannot create AbstractServo with null ServoConfig.");
        }
        myConfig = config;
        myServoId = myConfig.getServoId();
        if(myServoId == null){
            throw new NullPointerException();
        }
        myController = controller;
        myRange = new ServoRange();
    }

    @Override
    public Id getId() {
        return myServoId;
    }

    @Override
    public Ctrl getController() {
        return myController;
    }

    @Override
    public Conf getConfig() {
        return myConfig;
    }

    @Override
    public NormalizedDouble getGoalPosition(){
        return myGoalPosition;
    }

    /**
     * Returns the absolute goal position as used by the ServoController.
     * @return the absolute goal position as used by the ServoController
     */
    public Integer getAbsoluteGoalPosition(){
        if(myGoalPosition == null){
            return null;
        }
        double goal = myGoalPosition.getValue();
        double range = myConfig.getMaxPosition() - myConfig.getMinPosition();
        return (int)(goal*range + myConfig.getMinPosition());
    }

    @Override
    public void setGoalPosition(NormalizedDouble pos) {
        NormalizedDouble oldPos = myGoalPosition;
        myGoalPosition = pos;
        firePropertyChange(PROP_GOAL_POSITION, oldPos, pos);
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        firePropertyChange(pce);
    }
    
    @Override
    public NormalizableRange<Double> getPositionRange(){
        return myRange;
    }
    
    private class ServoRange implements NormalizableRange<Double>{
        @Override
        public boolean isValid(Double t) {
            int min = getMinPosition();
            int max = getMaxPosition();
            return max >= min 
                    ? t >= min && t <= max 
                    : t <= min && t >= max;
        }

        @Override
        public NormalizedDouble normalizeValue(Double val) {
            if(!isValid(val)){
                //If the value is out of range set it to the min or the max.
                if(Math.abs(val-getMin()) < Math.abs(val-getMax())){
                    val = getMin();
                }else{
                    val = getMax();
                }
                //return null;
            }
            int min = getMinPosition();
            double norm = (val - min)/(getMaxPosition() - min);
            return new NormalizedDouble(norm);
        }

        @Override
        public Double denormalizeValue(NormalizedDouble v) {
            int min = getMinPosition();
            return v.getValue()*(getMaxPosition() - min) + min;
        }

        @Override
        public Double getMin() {
            return (double)getMinPosition();
        }

        @Override
        public Double getMax() {
            return (double)getMaxPosition();
        }
    }
}
