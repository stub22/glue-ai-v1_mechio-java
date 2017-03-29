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

package org.mechio.impl.animation.osgi;

import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.api.registry.Registry;
import org.jflux.api.service.util.ServiceLauncher;
import org.jflux.impl.messaging.rk.JMSAvroMessageAsyncReceiver;
import org.jflux.impl.messaging.rk.JMSAvroServiceFacade;
import org.jflux.impl.messaging.rk.ServiceCommandRecord;
import org.jflux.impl.messaging.rk.config.RKMessagingConfigUtils;
import org.jflux.impl.registry.OSGiRegistry;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import org.mechio.api.animation.lifecycle.AnimationStopperHostLifecycle;
import org.mechio.api.animation.lifecycle.AnimationStopperLifecycle;
import org.mechio.api.animation.messaging.JMSAvroMessageAsyncReceiverFactory;
import org.mechio.api.animation.protocol.AnimationEvent;
import org.mechio.api.animation.protocol.AnimationSignal;
import org.mechio.api.animation.protocol.PlayRequest;
import org.mechio.api.animation.xml.AnimationFileReader;
import org.mechio.api.animation.xml.AnimationFileWriter;
import org.mechio.api.animation.stopper.AnimationStopper;
import org.mechio.api.animation.stopper.AnimationStopperHost;
import org.mechio.api.animation.stopper.OSGIAnimationStopperHost;
import org.mechio.impl.animation.messaging.AnimationEventRecord;
import org.mechio.impl.animation.messaging.AnimationSignallingRecord;
import org.mechio.impl.animation.messaging.PlayRequestRecord;
import org.mechio.impl.animation.messaging.PortableAnimationEvent;
import org.mechio.impl.animation.messaging.PortableAnimationSignal;
import org.mechio.impl.animation.messaging.PortablePlayRequest;
import org.mechio.impl.animation.xml.AnimationXMLReader;
import org.mechio.impl.animation.xml.XPP3AnimationXMLWriter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class Activator implements BundleActivator {
	private static final Logger theLogger = LoggerFactory.getLogger(Activator.class);
	private static final String STOPPER_ID = "STOPPER_ID";
	private static final String MESSAGE_RECEIVER_ID = AnimationStopperHostLifecycle.theMessageReceiverDep.getDependencyName();

	@Override
	public void start(final BundleContext context) throws Exception {
		theLogger.info("AnimationImpl Activation Begin.");
		new OSGiComponent(context, new SimpleLifecycle(
				new AnimationXMLReader(), AnimationFileReader.class)).start();

		new OSGiComponent(context, new SimpleLifecycle(
				new XPP3AnimationXMLWriter(), AnimationFileWriter.class)).start();

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				AnimationEvent.class,
				AnimationEventRecord.class,
				AnimationEventRecord.SCHEMA$,
				new PortableAnimationEvent.MessageRecordAdapter(),
				new PortableAnimationEvent.RecordMessageAdapter(),
				JMSAvroServiceFacade.AVRO_MIME_TYPE, null,
				new OSGiComponentFactory(context));

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				PlayRequest.class,
				PlayRequestRecord.class,
				PlayRequestRecord.SCHEMA$,
				new PortablePlayRequest.MessageRecordAdapter(),
				new PortablePlayRequest.RecordMessageAdapter(),
				JMSAvroServiceFacade.AVRO_MIME_TYPE, null,
				new OSGiComponentFactory(context));

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				AnimationSignal.class,
				AnimationSignallingRecord.class,
				AnimationSignallingRecord.SCHEMA$,
				new PortableAnimationSignal.MessageRecordAdapter(),
				new PortableAnimationSignal.RecordMessageAdapter(),
				JMSAvroServiceFacade.AVRO_MIME_TYPE, null,
				new OSGiComponentFactory(context));

		registerSignalFactory(context);

		final Registry registry = new OSGiRegistry(context);
		launchAnimationStopperLifecycle(registry);
		launchAnimationStopperHostLifecycle(registry);

		final JMSAvroMessageAsyncReceiver<ServiceCommand, ServiceCommandRecord> messageReceiver =
				JMSAvroMessageAsyncReceiverFactory.createLocalMessageReceiver(OSGIAnimationStopperHost.DESTINATION_NAME);

		registerAnimationStopperBundleContext(context, AnimationStopperLifecycle.theBundleContextDependency.getDependencyName());
		registerAnimationStopperHostMessageReceiver(context, messageReceiver);

		theLogger.info("AnimationImpl Activation Complete.");
	}

	private ServiceRegistration registerAnimationStopperHostMessageReceiver(final BundleContext context,
																			final MessageAsyncReceiver<ServiceCommand> messageAsyncReceiver) {
		final Properties props = new Properties();
		props.put(AnimationStopperHostLifecycle.theMessageReceiverDep.getDependencyName(), MESSAGE_RECEIVER_ID);
		return context.registerService(MessageAsyncReceiver.class.getName(), messageAsyncReceiver, props);
	}

	private ServiceRegistration registerAnimationStopperBundleContext(final BundleContext context, final String contextId) {
		final Properties props = new Properties();
		props.put(AnimationStopperLifecycle.theBundleContextDependency.getDependencyName(), contextId);
		return context.registerService(BundleContext.class.getName(), context, props);
	}

	private void launchAnimationStopperLifecycle(final Registry registry) {
		final AnimationStopperLifecycle lifecycle = new AnimationStopperLifecycle();
		new ServiceLauncher<>(lifecycle)
				.bindEager(AnimationStopperLifecycle.theBundleContextDependency).property(
				AnimationStopperLifecycle.theBundleContextDependency.getDependencyName(), "animationStopperBundleContextId")
				.serviceRegistration().property(AnimationStopper.PROPERTY_ID, STOPPER_ID)
				.managerRegistration()
				.launchService(registry);
	}

	private void launchAnimationStopperHostLifecycle(final Registry registry) {
		final AnimationStopperHostLifecycle lifecycle = new AnimationStopperHostLifecycle();

		new ServiceLauncher<>(lifecycle)
				.bindEager(AnimationStopperHostLifecycle.theAnimationStopperDep).property(
				AnimationStopperHostLifecycle.theAnimationStopperDep.getDependencyName(), STOPPER_ID)
				.bindEager(AnimationStopperHostLifecycle.theMessageReceiverDep).property(
				AnimationStopperHostLifecycle.theMessageReceiverDep.getDependencyName(), MESSAGE_RECEIVER_ID)

				.serviceRegistration().property(AnimationStopperHost.PROPERTY_ID, "animationStopperHostId")
				.managerRegistration()
				.launchService(registry);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
	}

	private void registerSignalFactory(final BundleContext context) {
		if (OSGiUtils.serviceExists(
				context, AnimationSignal.AnimationSignalFactory.class, null)) {
			return;
		}
		new OSGiComponent(context,
				new SimpleLifecycle(
						new PortableAnimationSignal.Factory(),
						AnimationSignal.AnimationSignalFactory.class)
		).start();
	}
}
