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

// Target path in Spotless repo:
// plugin-maven/src/main/java/com/diffplug/spotless/maven/groovy/Spock.java

package com.diffplug.spotless.maven.groovy;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.groovy.SpockStep;
import com.diffplug.spotless.maven.FormatterStepConfig;
import com.diffplug.spotless.maven.SpotlessConfig;

/**
 * Maven configuration object for the Spock formatter step.
 *
 * <p>Usage in {@code pom.xml}:
 * <pre>{@code
 * <configuration>
 *   <groovy>
 *     <spock/>                 <!-- default 4-space indent -->
 *     <!-- or: -->
 *     <spock>
 *       <indentSize>2</indentSize>
 *     </spock>
 *   </groovy>
 * </configuration>
 * }</pre>
 */
public class Spock implements FormatterStepConfig {

	/** Number of spaces per indentation level. Default: 4. */
	private int indentSize = 4;

	public void setIndentSize(int indentSize) {
		this.indentSize = indentSize;
	}

	@Override
	public FormatterStep newFormatterStep(SpotlessConfig spotlessConfig) {
		return SpockStep.create(indentSize);
	}
}
