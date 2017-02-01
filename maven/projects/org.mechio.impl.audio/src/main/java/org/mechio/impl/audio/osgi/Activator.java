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

package org.mechio.impl.audio.osgi;

import org.jflux.api.common.rk.services.ServiceUtils;
import org.jflux.api.common.rk.services.addon.AddOnUtils;
import org.jflux.api.common.rk.services.addon.DefaultAddOnDriver;
import org.jflux.api.common.rk.services.addon.ServiceAddOnDriver;
import org.mechio.api.audio.WavBufferPlayer;
import org.mechio.api.audio.WavPlayer;
import org.mechio.impl.audio.config.WavPlayerConfigAdapter;
import org.mechio.impl.audio.config.WavPlayerConfigLoader;
import org.mechio.impl.audio.config.WavPlayerConfigWriter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
	private static final Logger theLogger = LoggerFactory.getLogger(Activator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		theLogger.info("org.mechio.impl.audio Activation Begin.");
		ServiceUtils.registerConfigLoader(
				context, new WavPlayerConfigLoader(), null);
		ServiceUtils.registerConfigWriter(
				context, new WavPlayerConfigWriter(), null);
		ServiceRegistration driverReg = registerWavPlayerAddOnDriver(context);
		theLogger.info("org.mechio.impl.audio Activation Complete.");
	}

	private ServiceRegistration registerWavPlayerAddOnDriver(BundleContext context) {
		WavPlayerConfigAdapter adapter = new WavPlayerConfigAdapter();
		ServiceAddOnDriver<WavPlayer> driver =
				new DefaultAddOnDriver<>(context,
						WavBufferPlayer.VERSION,
						WavPlayerConfigLoader.VERSION,
						adapter);
		return AddOnUtils.registerAddOnDriver(context, driver, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO add deactivation code here
	}

}
