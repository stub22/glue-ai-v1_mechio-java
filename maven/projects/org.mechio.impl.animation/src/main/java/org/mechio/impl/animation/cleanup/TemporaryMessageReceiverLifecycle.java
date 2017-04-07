/*
 *   Copyright 2014 by the MechIO Project. (www.mechio.org).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.mechio.impl.animation.cleanup;

import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.qpid.QpidBrokerStarted;
import org.jflux.api.messaging.rk.qpid.QpidBrokerStatus;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.jflux.impl.messaging.rk.JMSAvroMessageAsyncReceiver;
import org.jflux.impl.messaging.rk.ServiceCommandRecord;
import org.mechio.api.animation.messaging.JMSAvroMessageAsyncReceiverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * {@link ServiceLifecycle} for {@link MessageAsyncReceiver<ServiceCommand>}.
 *
 * MessageReceivers returned are *not* started upon creation. I would be ok with adding that
 * functionality if MessageReceivers monitored their state allowed "isRunning", "isStopped" types of
 * methods.
 *
 * I would also be ok with starting them if MessageReceivers internally checked to see if they had
 * already been started and handled a duplicate call gracefully.
 *
 * TODO(ben): Replace this class when {@link org.jflux.spec.messaging.MessageAsyncReceiverLifecycle}
 * can be started easily.
 *
 * @author Ben Jenkins <benjenkinsv95@gmail.com>
 */
public class TemporaryMessageReceiverLifecycle implements ServiceLifecycle<MessageAsyncReceiver<ServiceCommand>> {

	private static final Logger theLogger = LoggerFactory.getLogger(TemporaryMessageReceiverLifecycle.class);

	public final static String THE_DESTINATION_NAME = "messageReceiverDestinationNameId";
	public final static ServiceDependency theDestinationNameDep = new ServiceDependency(THE_DESTINATION_NAME,
			String.class.getName(),
			ServiceDependency.Cardinality.MANDATORY_UNARY,
			ServiceDependency.UpdateStrategy.STATIC,
			null);

	/**
	 * Optional. Will default to localhost if no IP address is given.
	 */
	public final static String THE_IP_ADDRESS = "messageReceiverIPAddressId";
	/**
	 * Optional. Will default to localhost if no IP address is given.
	 */
	public final static ServiceDependency theIPAddressDep = new ServiceDependency(THE_IP_ADDRESS,
			String.class.getName(),
			ServiceDependency.Cardinality.OPTIONAL_UNARY,
			ServiceDependency.UpdateStrategy.STATIC,
			null);

	private final static String theQpidBrokerStarted = QpidBrokerStatus.PROPERTY_ID;
	public final static ServiceDependency theQpidBrokerStartedDep = new ServiceDependency(theQpidBrokerStarted,
			QpidBrokerStarted.class.getName(),
			ServiceDependency.Cardinality.MANDATORY_UNARY,
			ServiceDependency.UpdateStrategy.STATIC,
			null);

	private final static List<ServiceDependency> theDependencies =
			Arrays.asList(theQpidBrokerStartedDep, theIPAddressDep, theDestinationNameDep);

	@Override
	public List<ServiceDependency> getDependencySpecs() {
		return theDependencies;
	}

	@Override
	public MessageAsyncReceiver<ServiceCommand> createService(final Map<String, Object> dependencyMap) {
		final QpidBrokerStarted qpidBrokerStarted = (QpidBrokerStarted) dependencyMap.get(theQpidBrokerStarted);
		final String destinationName = (String) dependencyMap.get(THE_DESTINATION_NAME);

		final String ipAddress;
		if (dependencyMap.containsKey(THE_IP_ADDRESS)) {
			ipAddress = (String) dependencyMap.get(THE_IP_ADDRESS);
		} else {
			ipAddress = "127.0.0.1";
		}

		final JMSAvroMessageAsyncReceiver<ServiceCommand, ServiceCommandRecord> messageReceiver =
				JMSAvroMessageAsyncReceiverFactory.createMessageReceiver(ipAddress, destinationName);

		theLogger.info("Creating message receiver. Destination name: {}, ipAddress: {}, Qpid Broker Started: {}", destinationName, ipAddress, qpidBrokerStarted);
		return messageReceiver;
	}


	/**
	 * Unused since all dependencies are
	 * {@link ServiceDependency.UpdateStrategy#STATIC}
	 */
	@Override
	public MessageAsyncReceiver<ServiceCommand> handleDependencyChange(
			final MessageAsyncReceiver<ServiceCommand> service, final String changeType, final String dependencyName,
			final Object dependency, final Map<String, Object> availableDependencies) {
		throw new UnsupportedOperationException("All dependencies have static update strategies.");
	}

	@Override
	public void disposeService(final MessageAsyncReceiver<ServiceCommand> service,
							   final Map<String, Object> availableDependencies) {
		theLogger.info("Stopping {}.", service);
		service.stop();
	}

	@Override
	public String[] getServiceClassNames() {
		return new String[]{MessageAsyncReceiver.class.getName(), JMSAvroMessageAsyncReceiver.class.getName()};
	}
}
