/*
 *  Copyright 2014 the MechIO Project. All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *  
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *  
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE MECHIO PROJECT "AS IS" AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE MECHIO PROJECT OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of the MechIO Project.
 */

package org.mechio.impl.motion.pololu.osgi;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jflux.api.common.rk.services.ConfigurationLoader;
import org.jflux.api.common.rk.services.Constants;
import org.jflux.api.common.rk.services.ServiceUtils;
import org.mechio.api.motion.servos.utils.ServoIdReader;
import org.mechio.api.motion.servos.utils.ServoJointAdapter;
import org.mechio.impl.motion.pololu.MaestroConnector;
import org.mechio.impl.motion.pololu.MaestroController;
import org.mechio.impl.motion.pololu.MaestroControllerConfig;
import org.mechio.impl.motion.pololu.MaestroJointAdapter;
import org.mechio.impl.motion.pololu.MaestroServoIdReader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Activates the Pololu servo bundle and registers a MaestroConnector to
 * the OSGi service registry.
 *
 * @author Matthew Stevenson
 */
public class Activator implements BundleActivator {
	private static final Logger theLogger = LoggerFactory.getLogger(Activator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		theLogger.info("PololuServoBundle Activation Begin.");
		registerMaestroConnector(context);
		registerServoJointAdapter(context);
		registerServoIdReader(context);
		registerMaestroConfigLoader(context);
		theLogger.info("PololuServoBundle Activation Complete.");
	}

	private void registerMaestroConnector(BundleContext context) {
		MaestroConnector connector = new MaestroConnector();
		ServiceUtils.registerFactory(context, connector);
		theLogger.info("MaestroConnector Service Registered Successfully.");
	}

	private void registerServoJointAdapter(BundleContext context) {
		MaestroJointAdapter adapter = new MaestroJointAdapter();
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(Constants.SERVICE_VERSION,
				MaestroController.VERSION.toString());
		context.registerService(
				ServoJointAdapter.class.getName(), adapter, props);
		theLogger.info("MaestroJointAdapter Service Registered Successfully.");
	}

	private void registerServoIdReader(BundleContext context) {
		MaestroServoIdReader reader = new MaestroServoIdReader();
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(Constants.SERVICE_VERSION,
				MaestroController.VERSION.toString());
		context.registerService(ServoIdReader.class.getName(), reader, props);
		theLogger.info("MaestroServoIdReader Service Registered Successfully.");
	}

	private void registerMaestroConfigLoader(BundleContext context) {
		MaestroControllerConfig.Reader reader =
				new MaestroControllerConfig.Reader();
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(Constants.CONFIG_FORMAT_VERSION,
				MaestroControllerConfig.Reader.VERSION.toString());
		props.put(Constants.CONFIG_CLASS,
				MaestroControllerConfig.class.getName());
		props.put(Constants.CONFIG_PARAM_CLASS,
				HierarchicalConfiguration.class.getName());
		context.registerService(ConfigurationLoader.class.getName(),
				reader, props);
		theLogger.info("SerialServoControllerConfig XML Reader");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
