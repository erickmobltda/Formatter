package com.diffplug.spotless.groovy;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Self-contained test runner — zero external dependencies.
 * Run via: javac ... && java SpockStepSelfTest
 *
 * This complements the Spotless-integrated SpockStepTest.java.
 * Its purpose is to allow rapid iteration on the algorithm without
 * needing to run the full Gradle build.
 */
public class SpockStepSelfTest {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        testBasicGivenWhenThen();
        testWithDescriptions();
        testWithAndBlock();
        testWithWhereTable();
        testNestedCode();
        testMultipleMethods();
        testNonSpockMethodUnchanged();
        testIdempotent();
        testOffByOneIndent();
        testCustomIndentSize2();
        testExpectOnly();
        testSingleQuotedMethodName();
        testSetupCleanup();
        testEmptyBlocks();
        testBraceCountingWithStrings();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    // -----------------------------------------------------------------------

    static void testBasicGivenWhenThen() {
        String dirty = lines(
                "class ListSpec extends Specification {",
                "",
                "    def \"Should be able to remove from list\"() {",
                "        given:",
                "        def list = [1, 2, 3, 4]",
                "",
                "        when:",
                "         list.remove(0)",
                "",
                "        then:",
                "         list == [2, 3, 4]",
                "    }",
                "}");

        String clean = lines(
                "class ListSpec extends Specification {",
                "",
                "    def \"Should be able to remove from list\"() {",
                "        given:",
                "            def list = [1, 2, 3, 4]",
                "",
                "        when:",
                "            list.remove(0)",
                "",
                "        then:",
                "            list == [2, 3, 4]",
                "    }",
                "}");

        assertFormat("basicGivenWhenThen", dirty, clean);
    }

    static void testWithDescriptions() {
        String dirty = lines(
                "class UserSpec extends Specification {",
                "    def \"should authenticate\"() {",
                "        given: \"a registered user\"",
                "        def user = new User()",
                "",
                "        when: \"correct credentials\"",
                "        def result = auth.login()",
                "",
                "        then: \"succeeds\"",
                "        result.isSuccess()",
                "    }",
                "}");

        String clean = lines(
                "class UserSpec extends Specification {",
                "    def \"should authenticate\"() {",
                "        given: \"a registered user\"",
                "            def user = new User()",
                "",
                "        when: \"correct credentials\"",
                "            def result = auth.login()",
                "",
                "        then: \"succeeds\"",
                "            result.isSuccess()",
                "    }",
                "}");

        assertFormat("withDescriptions", dirty, clean);
    }

    static void testWithAndBlock() {
        String dirty = lines(
                "class OrderSpec extends Specification {",
                "    def \"total\"() {",
                "        given:",
                "        def order = new Order()",
                "",
                "        and:",
                "        def tax = 0.10",
                "",
                "        when:",
                "        def total = order.calc(tax)",
                "",
                "        then:",
                "        total > 0",
                "",
                "        and:",
                "        total == order.subtotal * 1.1",
                "    }",
                "}");

        String clean = lines(
                "class OrderSpec extends Specification {",
                "    def \"total\"() {",
                "        given:",
                "            def order = new Order()",
                "",
                "        and:",
                "            def tax = 0.10",
                "",
                "        when:",
                "            def total = order.calc(tax)",
                "",
                "        then:",
                "            total > 0",
                "",
                "        and:",
                "            total == order.subtotal * 1.1",
                "    }",
                "}");

        assertFormat("withAndBlock", dirty, clean);
    }

    static void testWithWhereTable() {
        String dirty = lines(
                "class MathSpec extends Specification {",
                "    def \"add\"() {",
                "        expect:",
                "        a + b == result",
                "",
                "        where:",
                "        a | b | result",
                "        1 | 2 | 3",
                "        4 | 5 | 9",
                "    }",
                "}");

        String clean = lines(
                "class MathSpec extends Specification {",
                "    def \"add\"() {",
                "        expect:",
                "            a + b == result",
                "",
                "        where:",
                "            a | b | result",
                "            1 | 2 | 3",
                "            4 | 5 | 9",
                "    }",
                "}");

        assertFormat("withWhereTable", dirty, clean);
    }

    static void testNestedCode() {
        String dirty = lines(
                "class ServiceSpec extends Specification {",
                "    def \"filter\"() {",
                "        given:",
                "        def items = [1, 2, 3, 4, 5]",
                "",
                "        when:",
                "        def result = []",
                "        for (item in items) {",
                "            if (item > 3) {",
                "                result.add(item)",
                "            }",
                "        }",
                "",
                "        then:",
                "        result == [4, 5]",
                "    }",
                "}");

        String clean = lines(
                "class ServiceSpec extends Specification {",
                "    def \"filter\"() {",
                "        given:",
                "            def items = [1, 2, 3, 4, 5]",
                "",
                "        when:",
                "            def result = []",
                "            for (item in items) {",
                "                if (item > 3) {",
                "                    result.add(item)",
                "                }",
                "            }",
                "",
                "        then:",
                "            result == [4, 5]",
                "    }",
                "}");

        assertFormat("nestedCode", dirty, clean);
    }

    static void testMultipleMethods() {
        String dirty = lines(
                "class StackSpec extends Specification {",
                "    def \"push\"() {",
                "        given:",
                "        def stack = new Stack()",
                "",
                "        when:",
                "        stack.push(\"hello\")",
                "",
                "        then:",
                "        stack.peek() == \"hello\"",
                "    }",
                "",
                "    def \"pop\"() {",
                "        given:",
                "        def stack = new Stack()",
                "        stack.push(\"hello\")",
                "",
                "        when:",
                "        def item = stack.pop()",
                "",
                "        then:",
                "        item == \"hello\"",
                "    }",
                "}");

        String clean = lines(
                "class StackSpec extends Specification {",
                "    def \"push\"() {",
                "        given:",
                "            def stack = new Stack()",
                "",
                "        when:",
                "            stack.push(\"hello\")",
                "",
                "        then:",
                "            stack.peek() == \"hello\"",
                "    }",
                "",
                "    def \"pop\"() {",
                "        given:",
                "            def stack = new Stack()",
                "            stack.push(\"hello\")",
                "",
                "        when:",
                "            def item = stack.pop()",
                "",
                "        then:",
                "            item == \"hello\"",
                "    }",
                "}");

        assertFormat("multipleMethods", dirty, clean);
    }

    static void testNonSpockMethodUnchanged() {
        String code = lines(
                "class Foo {",
                "    def regularMethod() {",
                "        def x = 1",
                "        return x",
                "    }",
                "}");
        assertUnchanged("nonSpockMethod", code);
    }

    static void testIdempotent() {
        String dirty = lines(
                "class Spec extends Specification {",
                "    def \"test\"() {",
                "        given:",
                "        def x = 1",
                "",
                "        then:",
                "        x == 1",
                "    }",
                "}");
        String once = SpockFormatter.format(dirty, 4);
        String twice = SpockFormatter.format(once, 4);
        assertEqual("idempotent", once, twice);
    }

    static void testOffByOneIndent() {
        // 1-space extra (9 spaces instead of 8) should be normalized to 12
        String dirty = lines(
                "class Spec extends Specification {",
                "    def \"test\"() {",
                "        when:",
                "         list.remove(0)",
                "",
                "        then:",
                "         list == [2, 3]",
                "    }",
                "}");

        String clean = lines(
                "class Spec extends Specification {",
                "    def \"test\"() {",
                "        when:",
                "            list.remove(0)",
                "",
                "        then:",
                "            list == [2, 3]",
                "    }",
                "}");

        assertFormat("offByOneIndent", dirty, clean);
    }

    static void testCustomIndentSize2() {
        String dirty = lines(
                "class Spec extends Specification {",
                "  def \"test\"() {",
                "    given:",
                "    def x = 1",
                "",
                "    then:",
                "    x == 1",
                "  }",
                "}");

        String clean = lines(
                "class Spec extends Specification {",
                "  def \"test\"() {",
                "    given:",
                "      def x = 1",
                "",
                "    then:",
                "      x == 1",
                "  }",
                "}");

        assertEqual("customIndentSize2",
                SpockFormatter.format(dirty, 2),
                clean);
    }

    static void testExpectOnly() {
        String dirty = lines(
                "class Spec extends Specification {",
                "    def \"max\"() {",
                "        expect:",
                "        Math.max(1, 2) == 2",
                "    }",
                "}");

        String clean = lines(
                "class Spec extends Specification {",
                "    def \"max\"() {",
                "        expect:",
                "            Math.max(1, 2) == 2",
                "    }",
                "}");

        assertFormat("expectOnly", dirty, clean);
    }

    static void testSingleQuotedMethodName() {
        String dirty = lines(
                "class Spec extends Specification {",
                "    def 'single quoted'() {",
                "        given:",
                "        def x = 1",
                "",
                "        then:",
                "        x == 1",
                "    }",
                "}");

        String clean = lines(
                "class Spec extends Specification {",
                "    def 'single quoted'() {",
                "        given:",
                "            def x = 1",
                "",
                "        then:",
                "            x == 1",
                "    }",
                "}");

        assertFormat("singleQuotedMethodName", dirty, clean);
    }

    static void testSetupCleanup() {
        String dirty = lines(
                "class DbSpec extends Specification {",
                "    def \"save\"() {",
                "        setup:",
                "        def db = Database.connect()",
                "",
                "        given:",
                "        def entity = new Entity()",
                "",
                "        when:",
                "        db.save(entity)",
                "",
                "        then:",
                "        db.findById(entity.id) == entity",
                "",
                "        cleanup:",
                "        db.disconnect()",
                "    }",
                "}");

        String clean = lines(
                "class DbSpec extends Specification {",
                "    def \"save\"() {",
                "        setup:",
                "            def db = Database.connect()",
                "",
                "        given:",
                "            def entity = new Entity()",
                "",
                "        when:",
                "            db.save(entity)",
                "",
                "        then:",
                "            db.findById(entity.id) == entity",
                "",
                "        cleanup:",
                "            db.disconnect()",
                "    }",
                "}");

        assertFormat("setupCleanup", dirty, clean);
    }

    static void testEmptyBlocks() {
        // Blocks with only empty lines between them should not crash
        String code = lines(
                "class Spec extends Specification {",
                "    def \"empty blocks\"() {",
                "        given:",
                "",
                "        when:",
                "        doSomething()",
                "",
                "        then:",
                "        true",
                "    }",
                "}");

        String clean = lines(
                "class Spec extends Specification {",
                "    def \"empty blocks\"() {",
                "        given:",
                "",
                "        when:",
                "            doSomething()",
                "",
                "        then:",
                "            true",
                "    }",
                "}");

        assertFormat("emptyBlocks", code, clean);
    }

    static void testBraceCountingWithStrings() {
        // Braces inside string literals should not affect depth counting
        String dirty = lines(
                "class Spec extends Specification {",
                "    def \"braces in strings\"() {",
                "        given:",
                "        def json = '{\"key\": \"value\"}'",
                "",
                "        then:",
                "        json.contains('{')",
                "    }",
                "}");

        String clean = lines(
                "class Spec extends Specification {",
                "    def \"braces in strings\"() {",
                "        given:",
                "            def json = '{\"key\": \"value\"}'",
                "",
                "        then:",
                "            json.contains('{')",
                "    }",
                "}");

        assertFormat("bracesInStrings", dirty, clean);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    static String lines(String... lines) {
        return String.join("\n", lines);
    }

    static void assertFormat(String name, String input, String expected) {
        String actual = SpockFormatter.format(input, 4);
        assertEqual(name, expected, actual);
    }

    static void assertUnchanged(String name, String input) {
        assertFormat(name, input, input);
    }

    static void assertEqual(String name, String expected, String actual) {
        if (expected.equals(actual)) {
            System.out.println("  PASS: " + name);
            passed++;
        } else {
            System.out.println("  FAIL: " + name);
            System.out.println("    EXPECTED:\n" + indent(expected));
            System.out.println("    ACTUAL:\n" + indent(actual));
            failed++;
        }
    }

    static String indent(String s) {
        return "      " + s.replace("\n", "\n      ");
    }
}
