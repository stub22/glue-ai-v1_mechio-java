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

package org.mechio.api.animation.stopper;

import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.impl.messaging.rk.JMSAvroMessageAsyncReceiver;
import org.jflux.impl.messaging.rk.ServiceCommandRecord;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test {@link OSGIAnimationStopperHost}
 *
 * @author ben
 * @since 4/3/2017.
 */
public class OSGIAnimationStopperHostTest {

	private OSGIAnimationStopperHost stopperHost;

	@Before
	public void setUp() throws Exception {
		final AnimationStopper animationStopper = mock(AnimationStopper.class);
		final JMSAvroMessageAsyncReceiver<ServiceCommand, ServiceCommandRecord> receiver = mock(JMSAvroMessageAsyncReceiver.class);

		stopperHost = OSGIAnimationStopperHost.create(animationStopper, receiver);

	}

	@Test
	public void parsePropertiesCommandShouldReturnMapWhenGivenSingleKeyValuePairs() throws Exception {
		final Map<String, String> properties = stopperHost.parsePropertiesCommand("robotId=RKR25 10000152");
		assertEquals(1, properties.size());
		
		assertTrue(properties.containsKey("robotId"));
		assertTrue(properties.containsValue("RKR25 10000152"));
		assertEquals("RKR25 10000152", properties.get("robotId"));
	}

	@Test
	public void parsePropertiesCommandShouldReturnMapWhenGivenMultipleKeyValuePairs() throws Exception {
		final Map<String, String> properties = stopperHost.parsePropertiesCommand("key1=val1,key2=val2");
		assertEquals(2, properties.size());

		assertTrue(properties.containsKey("key1"));
		assertTrue(properties.containsKey("key2"));

		assertTrue(properties.containsValue("val1"));
		assertTrue(properties.containsValue("val2"));

		assertEquals("val1", properties.get("key1"));
		assertEquals("val2", properties.get("key2"));
	}


}