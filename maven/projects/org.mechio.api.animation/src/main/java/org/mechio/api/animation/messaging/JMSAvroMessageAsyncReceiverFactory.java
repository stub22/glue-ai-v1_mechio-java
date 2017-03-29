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
package org.mechio.api.animation.messaging;

import org.apache.qpid.client.AMQAnyDestination;
import org.apache.qpid.client.AMQConnectionFactory;
import org.jflux.api.core.util.EmptyAdapter;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.impl.messaging.rk.JMSAvroMessageAsyncReceiver;
import org.jflux.impl.messaging.rk.ServiceCommandRecord;
import org.jflux.impl.messaging.rk.utils.ConnectionManager;
import org.jflux.impl.messaging.rk.utils.ConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Used to create {@link JMSAvroMessageAsyncReceiver<ServiceCommand, ServiceCommandRecord>}
 * instances and their dependencies
 *
 * @author ben
 * @since 3/22/2017.
 */
public class JMSAvroMessageAsyncReceiverFactory {

	private static final Logger theLogger = LoggerFactory.getLogger(JMSAvroMessageAsyncReceiverFactory.class);

	public static JMSAvroMessageAsyncReceiver<ServiceCommand, ServiceCommandRecord> createLocalMessageReceiver(final String destinationName) {
		return createMessageReceiver("127.0.0.1", destinationName);
	}

	public static JMSAvroMessageAsyncReceiver<ServiceCommand, ServiceCommandRecord> createMessageReceiver(final String ipAddress, final String destinationName) {
		checkNotNull(ipAddress);
		checkNotNull(destinationName);

		final String port = "5672";
		final Connection connection = createConnection(ipAddress, port);

		try {
			connection.start();
		} catch (final Exception ex) {
			theLogger.error("Failed to start connection for ipAddress {} on port {}.", ipAddress, port, ex.getMessage());
			return null;
		}

		final Session session = createSession(connection);
		final Destination destination = createTopicDestination(destinationName);

		final JMSAvroMessageAsyncReceiver<ServiceCommand, ServiceCommandRecord> messageReceiver
				= new JMSAvroMessageAsyncReceiver<>(
				session,
				destination,
				ServiceCommandRecord.class,
				ServiceCommandRecord.SCHEMA$);
		messageReceiver.setAdapter(new EmptyAdapter());

		return messageReceiver;
	}

	/**
	 * @param destinationName Where messages are being listened for. Examples:
	 *                        r25StopAnimationCommand, gpio0Event, gpio0EventEnabled
	 */
	private static Destination createTopicDestination(final String destinationName) {
		final String full = destinationName + "; {create: always, node: {type: topic}}";
		try {
			return new AMQAnyDestination(full);
		} catch (final URISyntaxException ex) {
			theLogger.error("Failed to create destination for destinationName {}", destinationName, ex.getMessage());
			return null;
		}
	}

	/**
	 * Create a session from a connection.
	 *
	 * @param connection A connection which I believe needs to be previously started.
	 * @return a session from the connection.
	 */
	private static Session createSession(final Connection connection) {
		try {
			return connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		} catch (final JMSException ex) {
			theLogger.error("Failed to create session for connection {}.", connection, ex.getMessage());
			return null;
		}
	}

	private static Connection createConnection(final String host, final String port) {
		final String tcp = "tcp://" + host + ":" + port;
		final String amqpURL = ConnectionManager.createAMQPConnectionURL(
				ConnectionUtils.getUsername(), ConnectionUtils.getPassword(),
				"client1", "test", tcp);
		try {
			final ConnectionFactory cf = new AMQConnectionFactory(amqpURL);
			return cf.createConnection();
		} catch (final Exception ex) {
			theLogger.error("Failed to create connection for host {} and port {}.", host, port, ex.getMessage());
			return null;
		}
	}


}
