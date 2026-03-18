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

import org.junit.Test;

import com.diffplug.spotless.StepHarness;
import com.diffplug.spotless.generic.IndentStep;

public class SpockStepTest extends com.diffplug.spotless.ResourceHarness {

	// -----------------------------------------------------------------------
	// Resource-based tests — compare .dirty against .clean expected files
	// -----------------------------------------------------------------------

	@Test
	public void basicGivenWhenThen() throws Exception {
		StepHarness.forStep(SpockStep.create())
				.testResource("groovy/spock/basicGivenWhenThen.dirty",
						"groovy/spock/basicGivenWhenThen.clean");
	}

	@Test
	public void withDescriptions() throws Exception {
		StepHarness.forStep(SpockStep.create())
				.testResource("groovy/spock/withDescriptions.dirty",
						"groovy/spock/withDescriptions.clean");
	}

	@Test
	public void withAndBlock() throws Exception {
		StepHarness.forStep(SpockStep.create())
				.testResource("groovy/spock/withAndBlock.dirty",
						"groovy/spock/withAndBlock.clean");
	}

	@Test
	public void withWhereTable() throws Exception {
		StepHarness.forStep(SpockStep.create())
				.testResource("groovy/spock/withWhereTable.dirty",
						"groovy/spock/withWhereTable.clean");
	}

	@Test
	public void nestedCode() throws Exception {
		StepHarness.forStep(SpockStep.create())
				.testResource("groovy/spock/nestedCode.dirty",
						"groovy/spock/nestedCode.clean");
	}

	@Test
	public void multipleMethodsInFile() throws Exception {
		StepHarness.forStep(SpockStep.create())
				.testResource("groovy/spock/multipleMethodsInFile.dirty",
						"groovy/spock/multipleMethodsInFile.clean");
	}

	@Test
	public void withSetupCleanup() throws Exception {
		StepHarness.forStep(SpockStep.create())
				.testResource("groovy/spock/withSetupCleanup.dirty",
						"groovy/spock/withSetupCleanup.clean");
	}

	// -----------------------------------------------------------------------
	// Idempotency: running the formatter twice yields the same result
	// -----------------------------------------------------------------------

	@Test
	public void idempotent_basicGivenWhenThen() throws Exception {
		StepHarness.forStep(SpockStep.create())
				.testResourceUnchanged("groovy/spock/basicGivenWhenThen.clean");
	}

	@Test
	public void idempotent_withDescriptions() throws Exception {
		StepHarness.forStep(SpockStep.create())
				.testResourceUnchanged("groovy/spock/withDescriptions.clean");
	}

	@Test
	public void idempotent_withWhereTable() throws Exception {
		StepHarness.forStep(SpockStep.create())
				.testResourceUnchanged("groovy/spock/withWhereTable.clean");
	}

	@Test
	public void idempotent_nestedCode() throws Exception {
		StepHarness.forStep(SpockStep.create())
				.testResourceUnchanged("groovy/spock/nestedCode.clean");
	}

	// -----------------------------------------------------------------------
	// Non-Spock methods should not be touched
	// -----------------------------------------------------------------------

	@Test
	public void nonSpockMethodUnchanged() throws Exception {
		String code = String.join("\n",
				"class Foo {",
				"    def regularMethod() {",
				"        def x = 1",
				"        return x",
				"    }",
				"}",
				"");
		StepHarness.forStep(SpockStep.create()).testUnchanged(code);
	}

	// -----------------------------------------------------------------------
	// Slightly-off indentation is normalized (e.g. 1-space content indent)
	// -----------------------------------------------------------------------

	@Test
	public void offByOneIndentIsNormalized() throws Exception {
		String dirty = String.join("\n",
				"class Spec extends Specification {",
				"    def \"test\"() {",
				"        when:",
				"         list.remove(0)",  // 9 spaces (1 extra vs label's 8)
				"",
				"        then:",
				"         list == [2, 3]",  // 9 spaces
				"    }",
				"}",
				"");
		String clean = String.join("\n",
				"class Spec extends Specification {",
				"    def \"test\"() {",
				"        when:",
				"            list.remove(0)",  // 12 spaces (label 8 + indent 4)
				"",
				"        then:",
				"            list == [2, 3]",
				"    }",
				"}",
				"");
		StepHarness.forStep(SpockStep.create()).test(dirty, clean);
	}

	// -----------------------------------------------------------------------
	// Custom indent size (2 spaces)
	// -----------------------------------------------------------------------

	@Test
	public void customIndentSize2() throws Exception {
		String dirty = String.join("\n",
				"class Spec extends Specification {",
				"  def \"test\"() {",
				"    given:",
				"    def x = 1",
				"",
				"    then:",
				"    x == 1",
				"  }",
				"}",
				"");
		String clean = String.join("\n",
				"class Spec extends Specification {",
				"  def \"test\"() {",
				"    given:",
				"      def x = 1",
				"",
				"    then:",
				"      x == 1",
				"  }",
				"}",
				"");
		StepHarness.forStep(SpockStep.create(2)).test(dirty, clean);
	}

	// -----------------------------------------------------------------------
	// Expect-only tests (no given/when)
	// -----------------------------------------------------------------------

	@Test
	public void expectOnly() throws Exception {
		String dirty = String.join("\n",
				"class Spec extends Specification {",
				"    def \"simple expectation\"() {",
				"        expect:",
				"        Math.max(1, 2) == 2",
				"    }",
				"}",
				"");
		String clean = String.join("\n",
				"class Spec extends Specification {",
				"    def \"simple expectation\"() {",
				"        expect:",
				"            Math.max(1, 2) == 2",
				"    }",
				"}",
				"");
		StepHarness.forStep(SpockStep.create()).test(dirty, clean);
	}

	// -----------------------------------------------------------------------
	// Single-quoted method names
	// -----------------------------------------------------------------------

	@Test
	public void singleQuotedMethodName() throws Exception {
		String dirty = String.join("\n",
				"class Spec extends Specification {",
				"    def 'should work with single quotes'() {",
				"        given:",
				"        def x = 1",
				"",
				"        then:",
				"        x == 1",
				"    }",
				"}",
				"");
		String clean = String.join("\n",
				"class Spec extends Specification {",
				"    def 'should work with single quotes'() {",
				"        given:",
				"            def x = 1",
				"",
				"        then:",
				"            x == 1",
				"    }",
				"}",
				"");
		StepHarness.forStep(SpockStep.create()).test(dirty, clean);
	}

	// -----------------------------------------------------------------------
	// Equality semantics (required by Spotless for caching)
	// -----------------------------------------------------------------------

	@Test
	public void equality() {
		new com.diffplug.spotless.StepEqualityTester()
				.addStep(SpockStep.create())
				.addStep(SpockStep.create())
				.addStep(SpockStep.create(2))
				.test();
	}
}
