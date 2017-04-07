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

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Utility methods for LDAP filters. Originally created to convert maps to LDAP filters.
 *
 * LDAP Filter documentation here: https://docs.oracle.com/cd/E19693-01/819-0997/gdxpo/index.html
 *
 * @author ben
 * @since 4/3/2017.
 */
public class LDAPFilters {

	/**
	 * Based on the "Using Compound Search Filters" section of https://docs.oracle.com/cd/E19693-01/819-0997/gdxpo/index.html
	 */
	enum BooleanOperator {
		/**
		 * All specified filters must be true for the statement to be true.For example,
		 *
		 * (&(filter)(filter)(filter)...)
		 */
		AND("&"),
		/**
		 * At least one specified filter must be true for the statement to be true.For example,
		 *
		 * (|(filter)(filter)(filter)...)
		 */
		OR("|"),
		/**
		 * The specified statement must not be true for the statement to be true. Only one filter is
		 * affected by the NOT operator. For example,
		 *
		 * (!(filter))
		 */
		NOT("!");
		private final String symbol;

		BooleanOperator(final String symbol) {
			this.symbol = symbol;
		}

		String getSymbol() {
			return symbol;
		}
	}

	public static String createLDAPFilter(final String key, final String value) {
		checkNotNull(key, "key cannot be null");
		checkNotNull(value, "value cannot be null");
		return "(" + key + "=" + value + ")";
	}

	/**
	 * Returns LDAP Filter string from a map. Example map with "key1=value1" and "key2=value2" and
	 * {@link BooleanOperator#AND} would return "(&(key1=value1)(key2=value2))"
	 *
	 * @param animationProperties key value entries of animation properties
	 * @param booleanOperator     How multiple search filter components are combined.
	 * @return LDAP filter string
	 * @throws IllegalArgumentException if {@code animationProperties} is null or empty.
	 */
	public static String createLDAPFilter(final Map<String, String> animationProperties,
										  final BooleanOperator booleanOperator) {
		checkNotNull(animationProperties, "animationProperties cannot be null");
		checkNotNull(booleanOperator, "booleanOperator cannot be null");
		checkState(!animationProperties.isEmpty(), "Animation properties cannot be empty.");

		final StringBuilder entryBuilder = new StringBuilder();
		for (final Map.Entry<String, String> entry : animationProperties.entrySet()) {
			entryBuilder.append(createLDAPFilter(entry.getKey(), entry.getValue()));
		}

		if (animationProperties.size() == 1 && booleanOperator != BooleanOperator.NOT) {
			return entryBuilder.toString();
		} else {
			return "(" + booleanOperator.getSymbol() + entryBuilder + ")";
		}
	}

	private LDAPFilters() {
	}
}
