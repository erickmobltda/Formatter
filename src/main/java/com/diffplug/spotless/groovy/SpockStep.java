/*
 * Copyright 2024 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.spotless.groovy;

import java.io.Serializable;
import java.util.Objects;

import com.diffplug.spotless.FormatterStep;

/**
 * Formatter step for Spock specification files.
 *
 * <p>Ensures that the code inside each Spock block label
 * ({@code given}, {@code when}, {@code then}, {@code and}, {@code where},
 * {@code expect}, {@code cleanup}, {@code setup}) is indented one extra level
 * beyond the label itself, producing canonical Spock formatting:
 *
 * <pre>
 * // Before (typical AI-generated / IDE default)
 * def "should remove first element"() {
 *     given:
 *     def list = [1, 2, 3]
 *
 *     when:
 *     list.remove(0)
 *
 *     then:
 *     list == [2, 3]
 * }
 *
 * // After
 * def "should remove first element"() {
 *     given:
 *         def list = [1, 2, 3]
 *
 *     when:
 *         list.remove(0)
 *
 *     then:
 *         list == [2, 3]
 * }
 * </pre>
 *
 * <p>Block labels may include an optional description string:
 * <pre>
 *     given: "a logged-in user"
 *         def user = loggedInUser()
 * </pre>
 *
 * <p>The step is idempotent: running it on already-formatted code produces
 * no changes. Nested structures within blocks preserve their relative
 * indentation.
 *
 * <p>The formatting algorithm is implemented in {@link SpockFormatter}.
 */
public final class SpockStep {

	static final String NAME = "spockFormat";
	private static final int DEFAULT_INDENT_SIZE = 4;

	private SpockStep() {}

	/** Creates a formatter step using the default indent size of 4 spaces. */
	public static FormatterStep create() {
		return create(DEFAULT_INDENT_SIZE);
	}

	/**
	 * Creates a formatter step using the specified indent size.
	 *
	 * @param indentSize number of spaces for each indentation level (must be >= 1)
	 */
	public static FormatterStep create(int indentSize) {
		return FormatterStep.create(NAME, new State(indentSize), State::format);
	}

	// -------------------------------------------------------------------------
	// Serializable state (drives equals/hashCode/caching in Spotless)
	// -------------------------------------------------------------------------

	static final class State implements Serializable {
		private static final long serialVersionUID = 1L;
		private final int indentSize;

		State(int indentSize) {
			if (indentSize < 1) {
				throw new IllegalArgumentException("indentSize must be >= 1, was: " + indentSize);
			}
			this.indentSize = indentSize;
		}

		String format(String rawUnix) {
			return SpockFormatter.format(rawUnix, indentSize);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof State)) return false;
			return indentSize == ((State) o).indentSize;
		}

		@Override
		public int hashCode() {
			return Objects.hash(indentSize);
		}
	}
}
