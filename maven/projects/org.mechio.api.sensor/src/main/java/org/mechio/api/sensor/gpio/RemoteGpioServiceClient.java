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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.core.Listener;
import org.jflux.api.core.Notifier;
import org.jflux.api.core.Source;
import org.jflux.api.core.util.DefaultNotifier;
import org.mechio.api.sensor.DeviceReadPeriodEvent;
import org.mechio.api.sensor.GpioConfigEvent;
import org.mechio.api.sensor.packet.channel.ChannelBoolEvent;
import org.mechio.api.sensor.packet.stamp.SensorEventHeader;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteGpioServiceClient<T extends SensorEventHeader> 
        extends DefaultNotifier<ChannelBoolEvent<T>> implements GpioService<T>{
    private final static Logger theLogger = Logger.getLogger(RemoteGpioServiceClient.class.getName());

    private Source<T> myHeaderFactory;
    private Source<Long> myTimestampFactory; 
    private Source<? extends ChannelBoolEvent<T>> myBoolEventFactory;
    private Source<? extends GpioConfigEvent<T>> myConfigEventFactory;
    private Notifier<GpioConfigEvent<T>> myDirectionSender;
    private Notifier<ChannelBoolEvent<T>> myOutputValueSender;
    private Notifier<ChannelBoolEvent<T>> myInputValueReceiver;
    private Notifier<DeviceReadPeriodEvent<T>> myReadPeriodSender;
    
    private Map<Integer, GpioPin> myPins;
    private GpioValueListener myValueListener;
    private int myDirectionSequenceId;
    private int myDirectionFrameId;
    private int myOutputValueSequenceId;
    private int myOutputValueFrameId;
    private int myInputValueSequenceId;
    private int myMask;
    
    public RemoteGpioServiceClient(
            Source<T> headerFactory, Source<Long> timestampFactory, 
            Source<? extends ChannelBoolEvent<T>> eventFactory, 
            Source<? extends GpioConfigEvent<T>> configFactory,
            Notifier<GpioConfigEvent<T>> directionSender,
            Notifier<DeviceReadPeriodEvent<T>> readPeriodSender,
            Notifier<ChannelBoolEvent<T>> outputValueSender,
            Notifier<ChannelBoolEvent<T>> inputValueReceiver,
            int dirSeqId, int outValSeqId, int inValSeqId,
            int...availablePins){
        myHeaderFactory = headerFactory;
        myTimestampFactory = timestampFactory;
        myBoolEventFactory = eventFactory;
        myConfigEventFactory = configFactory;
        myDirectionSender = directionSender;
        myReadPeriodSender = readPeriodSender;
        myOutputValueSender = outputValueSender;
        myInputValueReceiver = inputValueReceiver;
        myDirectionSequenceId = dirSeqId;
        myDirectionFrameId = 0;
        myOutputValueSequenceId = outValSeqId;
        myOutputValueFrameId = 0;
        myInputValueSequenceId = inValSeqId;
        myPins = new HashMap<Integer, GpioPin>(availablePins.length);
        for(int i : availablePins){
            GpioPin pin = new GpioPin(i, OUT, false);
            myPins.put(i, pin);
        }
        myValueListener = new GpioValueListener();
        myInputValueReceiver.addListener(myValueListener);
        myMask = 0;
    }
    
    @Override
    public Boolean getPinDirection(int channel) {
        GpioPin pin = myPins.get(channel);
        if(pin == null){
            return null;
        }
        return pin.direction;
    }

    @Override
    public void setPinDirection(int channel, boolean direction) {
        GpioPin pin = myPins.get(channel);
        if(pin == null || myHeaderFactory == null || myTimestampFactory == null 
                || myBoolEventFactory == null || myDirectionSender == null){
            return;
        }
        T header = myHeaderFactory.getValue();
        header.setFrameId(myDirectionFrameId++);
        header.setSequenceId(myDirectionSequenceId);
        header.setTimestamp(myTimestampFactory.getValue());
        
        GpioConfigEvent<T> event = myConfigEventFactory.getValue();
        
        if(direction) {
            myMask |= 1 << channel;
        } else {
            myMask &= 0 << channel;
        }
        
        event.setHeader(header);
        event.setInputMask(myMask);
        
        myDirectionSender.notifyListeners(event);
        pin.direction = direction;        
    }

    @Override
    public Boolean getPinValue(int channel) {
        GpioPin pin = myPins.get(channel);
        if(pin == null){
            return null;
        }
        return pin.value;
    }

    @Override
    public void setPinValue(int channel, boolean val) {
        GpioPin pin = myPins.get(channel);
        if(pin == null || myHeaderFactory == null || myTimestampFactory == null 
                || myBoolEventFactory == null || myOutputValueSender == null){
            return;
        }
        if(pin.direction == IN){
            theLogger.log(Level.WARNING, 
                    "Unable to set pin value for channel {0}, "
                    + "direction must be set OUT.", channel);
            return;
        }
        T header = myHeaderFactory.getValue();
        header.setFrameId(myOutputValueFrameId++);
        header.setSequenceId(myOutputValueSequenceId);
        header.setTimestamp(myTimestampFactory.getValue());
        
        ChannelBoolEvent<T> event = myBoolEventFactory.getValue();
        event.setHeader(header);
        event.setChannelId(channel);
        event.setBoolValue(val);
        
        myOutputValueSender.notifyListeners(event);
        pin.value = val;        
    }
    
    @Override
    public void setReadPeriod(DeviceReadPeriodEvent<T> readPeriod) {
        if(readPeriod == null) {
            theLogger.log(Level.WARNING, "Null read period.");
            throw new IllegalArgumentException("Read period cannot be null.");
        }
        
        myReadPeriodSender.notifyListeners(readPeriod);
    }
    
    public void setHeaderFactory(Source<T> headerFactory){
        myHeaderFactory = headerFactory;
    }
    
    public void setTimestampFactory(Source<Long> timestampFactory){
        myTimestampFactory = timestampFactory;
    }
    
    public void setEventFactory(Source<ChannelBoolEvent<T>> eventFactory){
        myBoolEventFactory = eventFactory;
    }
    
    public void setDirectionSender(Notifier<GpioConfigEvent<T>> sender){
        myDirectionSender = sender;
    }
    
    public void setOutputValueSender(Notifier<ChannelBoolEvent<T>> sender){
        myOutputValueSender = sender;
    }
    
    public void setInputValueReceiver(Notifier<ChannelBoolEvent<T>> receiver){
        if(myInputValueReceiver != null){
            myInputValueReceiver.removeListener(myValueListener);
        }
        myInputValueReceiver = receiver;
        if(myInputValueReceiver != null){
            myInputValueReceiver.addListener(myValueListener);
        }
    }
    
    public class GpioPin{
        public int channel;
        public Boolean direction;
        public Boolean value;
        
        private GpioPin(int channel, Boolean direction, Boolean value){
            this.channel = channel;
            this.direction = direction;
            this.value = value;
        }
    }
    
    private void handPinEvent(ChannelBoolEvent<T> t){
        int chan = t.getChannelId();
        SensorEventHeader header = t.getHeader();
        int seqId = header.getSequenceId();
        if(myInputValueSequenceId != seqId){
            theLogger.log(Level.WARNING, 
                    "Unexpected sequenceId for Gpio value: {0}", seqId);
        }
        GpioPin pin = myPins.get(chan);
        if(pin == null){
            theLogger.log(Level.WARNING, 
                    "Received message for unknown gpio pin {0}", chan);
            return;
        }
        Boolean val = t.getBoolValue();
        pin.value = val;
        this.notifyListeners(t);
    }
    
    class GpioValueListener implements Listener<ChannelBoolEvent<T>>{
        @Override public void handleEvent(ChannelBoolEvent<T> t) {
            handPinEvent(t);
        }
    }
}
