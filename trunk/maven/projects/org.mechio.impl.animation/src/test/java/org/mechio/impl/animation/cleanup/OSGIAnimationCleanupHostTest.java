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

import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.impl.messaging.rk.JMSAvroMessageAsyncReceiver;
import org.jflux.impl.messaging.rk.ServiceCommandRecord;
import org.junit.Before;
import org.junit.Test;
import org.mechio.api.animation.player.AnimationPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import org.mechio.api.animation.Animation;
import static org.mockito.Mockito.mock;

/**
 * Test {@link OSGIAnimationCleanupHost}
 *
 * @author ben
 * @since 4/3/2017.
 */
public class OSGIAnimationCleanupHostTest {

	private OSGIAnimationCleanupHost cleanupHost;

	@Before
	public void setUp() throws Exception {
		final AnimationStopper animationStopper = mock(AnimationStopper.class);
		final JMSAvroMessageAsyncReceiver<ServiceCommand, ServiceCommandRecord> receiver = mock(JMSAvroMessageAsyncReceiver.class);
		final AnimationPlayer animationPlayer = mock(AnimationPlayer.class);
		final Animation defaultAnimation = mock(Animation.class);

		cleanupHost = OSGIAnimationCleanupHost.create(animationStopper, animationPlayer, defaultAnimation, receiver);
	}

	@Test
	public void parsePropertiesCommandShouldReturnMapWhenGivenSingleKeyValuePairs() throws Exception {
		final Map<String, String> properties = cleanupHost.parsePropertiesCommand("robotId=RKR25 10000152");
		assertEquals(1, properties.size());

		assertTrue(properties.containsKey("robotId"));
		assertTrue(properties.containsValue("RKR25 10000152"));
		assertEquals("RKR25 10000152", properties.get("robotId"));
	}

	@Test
	public void parsePropertiesCommandShouldReturnMapWhenGivenMultipleKeyValuePairs() throws Exception {
		final Map<String, String> properties = cleanupHost.parsePropertiesCommand("key1=val1,key2=val2");
		assertEquals(2, properties.size());

		assertTrue(properties.containsKey("key1"));
		assertTrue(properties.containsKey("key2"));

		assertTrue(properties.containsValue("val1"));
		assertTrue(properties.containsValue("val2"));

		assertEquals("val1", properties.get("key1"));
		assertEquals("val2", properties.get("key2"));
	}

	@Test
	public void splitOnUnquotedSpacesShouldReturnTokenListWhenGivenPhraseWithoutQuotedSpaces() throws Exception {
		final List<String> tokens = cleanupHost.splitOnUnquotedSpaces("stop all");

		assertEquals("stop", tokens.get(0));
		assertEquals("all", tokens.get(1));
		assertEquals(2, tokens.size());
	}

	@Test
	public void splitOnUnquotedSpacesShouldReturnTokenListWhenGivenPhraseWithoutQuotedSpaces2() throws Exception {
		final List<String> tokens = cleanupHost.splitOnUnquotedSpaces("stop robotId=avatar");

		assertEquals("stop", tokens.get(0));
		assertEquals("robotId=avatar", tokens.get(1));
		assertEquals(2, tokens.size());
	}

	@Test
	public void splitOnUnquotedSpacesShouldReturnTokenListWhenGivenPhraseWithQuotedSpaces() throws Exception {
		final List<String> tokens = cleanupHost.splitOnUnquotedSpaces("stop \"robotId=RKR25 10000152\"");

		assertEquals("stop", tokens.get(0));
		assertEquals("\"robotId=RKR25 10000152\"", tokens.get(1));
		assertEquals(2, tokens.size());
	}

	@Test
	public void trimQuotationMarksShouldRemoveQuotationMarksWhenGivenAListWithElementsSurroundedByQuotationMarks() throws Exception {
		final List<String> tokens = Arrays.asList("stop", "\"robotId=RKR25 10000152\"");
		final List<String> trimmedTokens = cleanupHost.trimQuotationMarks(tokens);

		assertNotSame(tokens, trimmedTokens);
		assertEquals("stop", trimmedTokens.get(0));
		assertEquals("robotId=RKR25 10000152", trimmedTokens.get(1));
		assertEquals(2, trimmedTokens.size());
	}

}