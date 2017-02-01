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
package org.mechio.impl.speech.osgi;

import org.jflux.api.common.rk.services.ServiceUtils;
import org.jflux.api.core.util.EmptyAdapter;
import org.jflux.impl.messaging.rk.JMSAvroServiceFacade;
import org.jflux.impl.messaging.rk.config.RKMessagingConfigUtils;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import org.mechio.api.speech.SpeechConfig;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speech.SpeechRequestFactory;
import org.mechio.impl.speech.PortableSpeechRequest;
import org.mechio.impl.speech.SpeechConfigRecord;
import org.mechio.impl.speech.SpeechEventListRecord;
import org.mechio.impl.speech.SpeechEventRecord;
import org.mechio.impl.speech.SpeechRequestRecord;
import org.mechio.impl.speech.viseme.VisemeBindingManagerAvroConfigLoader;
import org.mechio.impl.speech.viseme.VisemeBindingManagerAvroConfigWriter;
import org.mechio.impl.speech.viseme.VisemeBindingManagerConfigAvroStreamLoader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class Activator implements BundleActivator {
	private static final Logger theLogger = LoggerFactory.getLogger(Activator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		theLogger.info("org.mechio.impl.speech Activation Begin.");
		ServiceUtils.registerConfigLoader(
				context, new VisemeBindingManagerAvroConfigLoader(), null);
		ServiceUtils.registerConfigLoader(
				context, new VisemeBindingManagerConfigAvroStreamLoader(), null);

		ServiceUtils.registerConfigWriter(
				context, new VisemeBindingManagerAvroConfigWriter(), null);

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				SpeechConfig.class,
				SpeechConfigRecord.class,
				SpeechConfigRecord.SCHEMA$,
				new EmptyAdapter(),
				new EmptyAdapter(),
				JMSAvroServiceFacade.CONFIG_MIME_TYPE, null,
				new OSGiComponentFactory(context));

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				SpeechEvent.class,
				SpeechEventRecord.class,
				SpeechEventRecord.SCHEMA$,
				new EmptyAdapter(),
				new EmptyAdapter(),
				JMSAvroServiceFacade.AVRO_MIME_TYPE, null,
				new OSGiComponentFactory(context));

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				SpeechEventList.class,
				SpeechEventListRecord.class,
				SpeechEventListRecord.SCHEMA$,
				new EmptyAdapter(),
				new EmptyAdapter(),
				JMSAvroServiceFacade.AVRO_MIME_TYPE, null,
				new OSGiComponentFactory(context));

		RKMessagingConfigUtils.registerAvroSerializationConfig(
				SpeechRequest.class,
				SpeechRequestRecord.class,
				SpeechRequestRecord.SCHEMA$,
				new EmptyAdapter(),
				new EmptyAdapter(),
				JMSAvroServiceFacade.AVRO_MIME_TYPE, null,
				new OSGiComponentFactory(context));

		new OSGiComponent(context,
				new SimpleLifecycle(new PortableSpeechRequest.Factory(),
						SpeechRequestFactory.class)).start();

		theLogger.info("org.mechio.impl.speech Activation Complete.");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		//TODO add deactivation code here
	}
}
