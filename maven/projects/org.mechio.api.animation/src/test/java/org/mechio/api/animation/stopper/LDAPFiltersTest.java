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

import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link OSGIAnimationStopper}
 *
 * @author ben
 * @since 4/3/2017.
 */
public class LDAPFiltersTest {

	@Test
	public void createLDAPFilterShouldReturnFilterGivenSingleKeyValuePair() throws Exception {
		final Map<String, String> properties = new TreeMap<>();
		properties.put("robotId", "RKR25 10000152");
		final String filter = LDAPFilters.createLDAPFilter(properties, LDAPFilters.BooleanOperator.AND);
		assertEquals("(robotId=RKR25 10000152)", filter);
	}

	@Test
	public void createLDAPFilterShouldReturnFilterGivenMultipleKeyValuePair() throws Exception {
		final Map<String, String> properties = new TreeMap<>();
		properties.put("key1", "value1");
		properties.put("key2", "value2");
		final String filter = LDAPFilters.createLDAPFilter(properties, LDAPFilters.BooleanOperator.AND);
		assertEquals("(&(key1=value1)(key2=value2))", filter);
	}
}