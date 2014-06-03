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
package org.mechio.api.sensor.gpio;

import org.jflux.api.core.Notifier;
import org.mechio.api.sensor.packet.channel.ChannelBoolEvent;
import org.mechio.api.sensor.DeviceReadPeriodEvent;
import org.mechio.api.sensor.packet.stamp.SensorEventHeader;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface GpioService<T extends SensorEventHeader> 
        extends Notifier<ChannelBoolEvent<T>> {
    public static boolean IN = true;
    public static boolean OUT = false;
    
    public Boolean getPinDirection(int channel);
    public void setPinDirection(int channel, boolean direction);
    
    public Boolean getPinValue(int channel);
    public void setPinValue(int channel, boolean val);
    
    public void setReadPeriod(DeviceReadPeriodEvent<T> readPeriod);
}
