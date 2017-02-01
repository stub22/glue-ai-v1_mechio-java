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
package org.mechio.api.sensor.imu;

import org.jflux.api.core.Listener;
import org.jflux.api.core.Notifier;
import org.jflux.api.core.util.DefaultNotifier;
import org.mechio.api.sensor.AccelerometerConfigEvent;
import org.mechio.api.sensor.DeviceReadPeriodEvent;
import org.mechio.api.sensor.FilteredVector3Event;
import org.mechio.api.sensor.packet.stamp.SensorEventHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
public class RemoteAccelerometerServiceClient<T extends SensorEventHeader>
		extends DefaultNotifier<FilteredVector3Event>
		implements AccelerometerService<T> {
	private static final Logger theLogger = LoggerFactory.getLogger(RemoteAccelerometerServiceClient.class);

	private Notifier<AccelerometerConfigEvent<T>> myConfigSender;
	private Notifier<DeviceReadPeriodEvent<T>> myReadPeriodSender;
	private Notifier<FilteredVector3Event> myInputValueReceiver;
	private AccelerometerValueListener myEventListener;

	public RemoteAccelerometerServiceClient(
			Notifier<AccelerometerConfigEvent<T>> configSender,
			Notifier<DeviceReadPeriodEvent<T>> readPeriodSender,
			Notifier<FilteredVector3Event> inputValueReceiver) {
		if (configSender == null || readPeriodSender == null ||
				inputValueReceiver == null) {
			theLogger.error("Null parameters.");
			throw new IllegalArgumentException("Parameter cannot be null.");
		}

		myConfigSender = configSender;
		myReadPeriodSender = readPeriodSender;
		myInputValueReceiver = inputValueReceiver;

		myEventListener = new AccelerometerValueListener();
		myInputValueReceiver.addListener(myEventListener);
	}

	@Override
	public void sendConfig(AccelerometerConfigEvent<T> config) {
		if (config == null) {
			theLogger.warn("Null config.");
			throw new IllegalArgumentException("Config cannot be null.");
		}

		myConfigSender.notifyListeners(config);
	}

	@Override
	public void setReadPeriod(DeviceReadPeriodEvent<T> readPeriod) {
		if (readPeriod == null) {
			theLogger.warn("Null read period.");
			throw new IllegalArgumentException("Read period cannot be null.");
		}

		myReadPeriodSender.notifyListeners(readPeriod);
	}

	class AccelerometerValueListener implements Listener<FilteredVector3Event> {
		@Override
		public void handleEvent(FilteredVector3Event t) {
			notifyListeners(t);
		}

	}
}
