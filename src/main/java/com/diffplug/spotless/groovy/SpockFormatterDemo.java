/*
 * Standalone demo — no external dependencies required.
 * Run with: javac SpockFormatterDemo.java && java SpockFormatterDemo
 * (works from the same directory)
 */
package com.diffplug.spotless.groovy;

/**
 * Quick demonstration of SpockStep formatting.
 *
 * <p>This class exercises the formatting logic directly (bypasses the
 * Spotless {@code FormatterStep} wrapper) so it can be compiled and run
 * without any additional dependencies.
 */
public class SpockFormatterDemo {

    public static void main(String[] args) {

        System.out.println("=== SpockStep Formatter Demo ===\n");

        // -------------------------------------------------------------------
        // Case 1: basic given / when / then (AI-generated style)
        // -------------------------------------------------------------------
        String case1 = String.join("\n",
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

        printBeforeAfter("Case 1 — basic given/when/then", case1, SpockFormatter.format(case1, 4));

        // -------------------------------------------------------------------
        // Case 2: block labels with descriptions
        // -------------------------------------------------------------------
        String case2 = String.join("\n",
                "class UserSpec extends Specification {",
                "",
                "    def \"should authenticate user\"() {",
                "        given: \"a registered user\"",
                "        def user = new User(name: \"Alice\")",
                "",
                "        when: \"the user provides correct credentials\"",
                "        def result = authService.login(\"Alice\", \"secret\")",
                "",
                "        then: \"authentication succeeds\"",
                "        result.isSuccess()",
                "        result.user == user",
                "    }",
                "}");

        printBeforeAfter("Case 2 — labels with descriptions", case2, SpockFormatter.format(case2, 4));

        // -------------------------------------------------------------------
        // Case 3: where: data table
        // -------------------------------------------------------------------
        String case3 = String.join("\n",
                "class MathSpec extends Specification {",
                "",
                "    def \"should add numbers\"() {",
                "        expect:",
                "        a + b == result",
                "",
                "        where:",
                "        a | b | result",
                "        1 | 2 | 3",
                "        4 | 5 | 9",
                "    }",
                "}");

        printBeforeAfter("Case 3 — expect/where with data table", case3, SpockFormatter.format(case3, 4));

        // -------------------------------------------------------------------
        // Case 4: nested code inside a block (if/for)
        // -------------------------------------------------------------------
        String case4 = String.join("\n",
                "class ServiceSpec extends Specification {",
                "",
                "    def \"should filter items\"() {",
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

        printBeforeAfter("Case 4 — nested code preserves relative indentation", case4, SpockFormatter.format(case4, 4));

        // -------------------------------------------------------------------
        // Case 5: idempotency check
        // -------------------------------------------------------------------
        String case5already = SpockFormatter.format(case1, 4);
        String case5rerun = SpockFormatter.format(case5already, 4);
        System.out.println("--- Case 5 — idempotency ---");
        System.out.println("Second run matches first run: " + case5already.equals(case5rerun));
        System.out.println();

        // -------------------------------------------------------------------
        // Case 6: and: blocks
        // -------------------------------------------------------------------
        String case6 = String.join("\n",
                "class OrderSpec extends Specification {",
                "",
                "    def \"should calculate total\"() {",
                "        given:",
                "        def order = new Order()",
                "",
                "        and:",
                "        def taxRate = 0.10",
                "",
                "        when:",
                "        def total = order.calculateTotal(taxRate)",
                "",
                "        then:",
                "        total > 0",
                "",
                "        and:",
                "        total == order.subtotal * (1 + taxRate)",
                "    }",
                "}");

        printBeforeAfter("Case 6 — and: blocks", case6, SpockFormatter.format(case6, 4));
    }

    private static void printBeforeAfter(String label, String before, String after) {
        System.out.println("--- " + label + " ---");
        System.out.println("BEFORE:");
        System.out.println(before);
        System.out.println();
        System.out.println("AFTER:");
        System.out.println(after);
        System.out.println();
    }
}
