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

package org.mechio.impl.motion.dynamixel.osgi;

import org.jflux.api.common.rk.services.Constants;
import org.jflux.api.common.rk.services.ServiceUtils;
import org.mechio.api.motion.servos.utils.ServoIdReader;
import org.mechio.api.motion.servos.utils.ServoJointAdapter;
import org.mechio.impl.motion.dynamixel.DynamixelConnector;
import org.mechio.impl.motion.dynamixel.DynamixelController;
import org.mechio.impl.motion.dynamixel.utils.DynamixelControllerConfig;
import org.mechio.impl.motion.dynamixel.utils.DynamixelJointAdapter;
import org.mechio.impl.motion.dynamixel.utils.DynamixelServoIdReader;
import org.mechio.impl.motion.openservo.OpenServoConnector;
import org.mechio.impl.motion.openservo.OpenServoController;
import org.mechio.impl.motion.openservo.utils.OpenServoControllerConfig;
import org.mechio.impl.motion.openservo.utils.OpenServoIdReader;
import org.mechio.impl.motion.openservo.utils.OpenServoJointAdapter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Activates the Dynamixel servo bundle and registers a DynamixelConnector to
 * the OSGi service registry.
 *
 * @author Matthew Stevenson
 */
public class Activator implements BundleActivator {
	private static final Logger theLogger = LoggerFactory.getLogger(Activator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		theLogger.info("DynamixelServoBundle Activation Begin.");
		ServiceUtils.registerFactory(context, new DynamixelConnector());
		ServiceUtils.registerConfigLoader(
				context, new DynamixelControllerConfig.Reader());
		registerServoJointAdapter(context);
		registerServoIdReader(context);

		ServiceUtils.registerFactory(context, new OpenServoConnector());
		ServiceUtils.registerConfigLoader(
				context, new OpenServoControllerConfig.Reader());
		registerOpenServoJointAdapter(context);
		registerOpenServoIdReader(context);
		theLogger.info("DynamixelServoBundle Activation Complete.");
	}

	private void registerServoJointAdapter(BundleContext context) {
		DynamixelJointAdapter adapter = new DynamixelJointAdapter();
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(Constants.SERVICE_VERSION,
				DynamixelController.VERSION.toString());
		context.registerService(
				ServoJointAdapter.class.getName(), adapter, props);
		theLogger.info("DynamixelJointAdapter Service Registered Successfully.");
	}

	private void registerServoIdReader(BundleContext context) {
		DynamixelServoIdReader reader = new DynamixelServoIdReader();
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(Constants.SERVICE_VERSION,
				DynamixelController.VERSION.toString());
		context.registerService(ServoIdReader.class.getName(), reader, props);
		theLogger.info("DynamixelServoIdReader Service Registered Successfully.");
	}

	private void registerOpenServoJointAdapter(BundleContext context) {
		OpenServoJointAdapter adapter = new OpenServoJointAdapter();
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(Constants.SERVICE_VERSION,
				OpenServoController.VERSION.toString());
		context.registerService(
				ServoJointAdapter.class.getName(), adapter, props);
		theLogger.info("OpenServoJointAdapter Service Registered Successfully.");
	}

	private void registerOpenServoIdReader(BundleContext context) {
		OpenServoIdReader reader = new OpenServoIdReader();
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(Constants.SERVICE_VERSION,
				OpenServoController.VERSION.toString());
		context.registerService(ServoIdReader.class.getName(), reader, props);
		theLogger.info("OpenServoIdReader Service Registered Successfully.");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}
}
