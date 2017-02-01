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

package org.mechio.impl.motion.osgi;

import org.jflux.api.common.rk.services.ServiceUtils;
import org.jflux.impl.messaging.rk.JMSAvroServiceFacade;
import org.jflux.impl.messaging.rk.config.RKMessagingConfigUtils;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import org.mechio.api.motion.jointgroup.RobotJointGroupFactory;
import org.mechio.api.motion.messaging.RobotResponseFactory;
import org.mechio.api.motion.protocol.MotionFrameEvent;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;
import org.mechio.api.motion.protocol.RobotRequest;
import org.mechio.api.motion.protocol.RobotResponse;
import org.mechio.api.motion.protocol.RobotResponse.RobotPositionResponse;
import org.mechio.api.motion.protocol.RobotResponse.RobotStatusResponse;
import org.mechio.impl.motion.config.RobotConfigXMLFileLoader;
import org.mechio.impl.motion.config.RobotConfigXMLReader;
import org.mechio.impl.motion.config.RobotConfigXMLStreamLoader;
import org.mechio.impl.motion.jointgroup.RobotJointGroupConfigXMLReader;
import org.mechio.impl.motion.jointgroup.RobotJointGroupXMLFileLoader;
import org.mechio.impl.motion.jointgroup.RobotJointGroupXMLStreamLoader;
import org.mechio.impl.motion.messaging.MotionFrameEventRecord;
import org.mechio.impl.motion.messaging.PortableMotionFrameEvent;
import org.mechio.impl.motion.messaging.PortableRobotDefinitionResponse;
import org.mechio.impl.motion.messaging.PortableRobotPositionResponse;
import org.mechio.impl.motion.messaging.PortableRobotRequest;
import org.mechio.impl.motion.messaging.PortableRobotResponse;
import org.mechio.impl.motion.messaging.PortableRobotStatusResponse;
import org.mechio.impl.motion.messaging.RobotDefinitionResponseRecord;
import org.mechio.impl.motion.messaging.RobotPositionResponseRecord;
import org.mechio.impl.motion.messaging.RobotRequestRecord;
import org.mechio.impl.motion.messaging.RobotStatusResponseRecord;
import org.mechio.impl.motion.sync.SynchronizedRobotConfigLoader;
import org.mechio.impl.motion.sync.SynchronizedRobotConfigWriter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class Activator implements BundleActivator {
	private static final Logger theLogger = LoggerFactory.getLogger(Activator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		theLogger.info("MotionAPI Activation Begin.");
		ServiceUtils.registerConfigLoader(
				context, new RobotConfigXMLReader(context));

		ServiceUtils.registerConfigLoader(
				context, new RobotConfigXMLFileLoader(context));

		ServiceUtils.registerConfigLoader(
				context, new RobotConfigXMLStreamLoader(context));

		ServiceUtils.registerConfigLoader(
				context, new RobotJointGroupConfigXMLReader());

		ServiceUtils.registerConfigLoader(
				context, new RobotJointGroupXMLFileLoader());

		ServiceUtils.registerConfigLoader(
				context, new RobotJointGroupXMLStreamLoader());

		ServiceUtils.registerFactory(
				context, new RobotJointGroupFactory());
		ServiceUtils.registerConfigLoader(
				context, new SynchronizedRobotConfigLoader());

		ServiceUtils.registerConfigWriter(
				context, new SynchronizedRobotConfigWriter(), null);

		context.registerService(RobotResponseFactory.class.getName(),
				new PortableRobotResponse.Factory(), null);

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				MotionFrameEvent.class,
				MotionFrameEventRecord.class,
				MotionFrameEventRecord.SCHEMA$,
				new PortableMotionFrameEvent.MessageRecordAdapter(),
				new PortableMotionFrameEvent.RecordMessageAdapter(),
				JMSAvroServiceFacade.AVRO_MIME_TYPE, null,
				new OSGiComponentFactory(context));

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				RobotRequest.class,
				RobotRequestRecord.class,
				RobotRequestRecord.SCHEMA$,
				new PortableRobotRequest.MessageRecordAdapter(),
				new PortableRobotRequest.RecordMessageAdapter(),
				JMSAvroServiceFacade.AVRO_MIME_TYPE, null,
				new OSGiComponentFactory(context));

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				RobotDefinitionResponse.class,
				RobotDefinitionResponseRecord.class,
				RobotDefinitionResponseRecord.SCHEMA$,
				new PortableRobotDefinitionResponse.MessageRecordAdapter(),
				new PortableRobotDefinitionResponse.RecordMessageAdapter(),
				PortableRobotResponse.MIME_ROBOT_DEFINITION_RESPONSE, null,
				new OSGiComponentFactory(context));

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				RobotPositionResponse.class,
				RobotPositionResponseRecord.class,
				RobotPositionResponseRecord.SCHEMA$,
				new PortableRobotPositionResponse.MessageRecordAdapter(),
				new PortableRobotPositionResponse.RecordMessageAdapter(),
				PortableRobotResponse.MIME_ROBOT_POSITION_RESPONSE, null,
				new OSGiComponentFactory(context));

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				RobotStatusResponse.class,
				RobotStatusResponseRecord.class,
				RobotStatusResponseRecord.SCHEMA$,
				new PortableRobotStatusResponse.MessageRecordAdapter(),
				new PortableRobotStatusResponse.RecordMessageAdapter(),
				PortableRobotResponse.MIME_ROBOT_STATUS_RESPONSE, null,
				new OSGiComponentFactory(context));

		RKMessagingConfigUtils.registerSerializationConfig(
				RobotResponse.class, BytesMessage.class,
				new PortableRobotResponse.MessageRecordAdapter(),
				new PortableRobotResponse.RecordMessageAdapter(),
				null, null, new OSGiComponentFactory(context));

		theLogger.info("MotionAPI Activation Complete.");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
