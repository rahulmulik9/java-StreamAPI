package phase1_basics;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Intermediate operations are lazy
 *   - Nothing runs until a terminal operation is called
 *   - Elements flow one by one through the whole pipeline, not stage by stage
 *   - Short-circuiting (findFirst, limit)
 *
 * PROBLEM:
 *   A stream with filter and map but no terminal operation prints nothing,
 *   so it looks like the code did not run.
 *
 * SOLUTION:
 *   Add a terminal operation. Use peek to see that elements flow one by one
 *   through the whole pipeline, not stage by stage.
 */
public class P03_LazyEvaluationDemo {
    public static void main(String[] args) {

        List<String> names = List.of("Alice", "Bob", "Charlie", "David", "Eve");

        // ------------------------------------------------------------
        // 1. Broken version: pipeline without a terminal operation
        // ------------------------------------------------------------
        System.out.println("--- 1. No terminal operation ---");
        Stream<String> lazy = names.stream()
                .peek(n -> System.out.println("  peek1 (source)  : " + n))
                .filter(n -> n.length() > 3)
                .map(String::toUpperCase);
        System.out.println("Pipeline built. Nothing was printed above this line.");

        // ------------------------------------------------------------
        // 2. Fixed version: add a terminal operation
        // ------------------------------------------------------------

        // a) collect: every element flows through the whole pipeline, one at a time
        System.out.println("\n--- 2a. With collect (terminal) ---");
        List<String> result = names.stream()
                .peek(n -> System.out.println("  peek1 (source)  : " + n))
                .filter(n -> n.length() > 3)
                .peek(n -> System.out.println("  peek2 (filtered): " + n))
                .map(String::toUpperCase)
                .peek(n -> System.out.println("  peek3 (mapped)  : " + n))
                .collect(Collectors.toList());
        System.out.println("Result: " + result);

        // b) findFirst: short-circuits, stops after the first match
        System.out.println("\n--- 2b. With findFirst (short-circuit) ---");
        Optional<String> first = names.stream()
                .peek(n -> System.out.println("  peek1 (source)  : " + n))
                .filter(n -> n.length() > 3)
                .peek(n -> System.out.println("  peek2 (filtered): " + n))
                .map(String::toUpperCase)
                .peek(n -> System.out.println("  peek3 (mapped)  : " + n))
                .findFirst();
        System.out.println("Result: " + first);

        // c) limit(2): stops once 2 elements have passed
        System.out.println("\n--- 2c. With limit(2) (short-circuit) ---");
        List<String> limited = names.stream()
                .peek(n -> System.out.println("  peek1 (source)  : " + n))
                .filter(n -> n.length() > 3)
                .map(String::toUpperCase)
                .peek(n -> System.out.println("  peek3 (mapped)  : " + n))
                .limit(2)
                .collect(Collectors.toList());
        System.out.println("Result: " + limited);

        // d) sorted: a stateful operation, must see ALL elements before passing any on
        System.out.println("\n--- 2d. With sorted (breaks the one-by-one flow) ---");
        List<String> sorted = names.stream()
                .peek(n -> System.out.println("  peek1 (source)  : " + n))
                .filter(n -> n.length() > 3)
                .sorted()
                .map(String::toUpperCase)
                .peek(n -> System.out.println("  peek3 (mapped)  : " + n))
                .collect(Collectors.toList());
        System.out.println("Result: " + sorted);

        // ------------------------------------------------------------
        // 3. Output explanation
        // ------------------------------------------------------------
        /*
         * Expected output:
         *
         *   --- 1. No terminal operation ---
         *   Pipeline built. Nothing was printed above this line.
         *
         *   --- 2a. With collect (terminal) ---
         *     peek1 (source)  : Alice
         *     peek2 (filtered): Alice
         *     peek3 (mapped)  : ALICE
         *     peek1 (source)  : Bob
         *     peek1 (source)  : Charlie
         *     peek2 (filtered): Charlie
         *     peek3 (mapped)  : CHARLIE
         *     peek1 (source)  : David
         *     peek2 (filtered): David
         *     peek3 (mapped)  : DAVID
         *     peek1 (source)  : Eve
         *   Result: [ALICE, CHARLIE, DAVID]
         *
         *   --- 2b. With findFirst (short-circuit) ---
         *     peek1 (source)  : Alice
         *     peek2 (filtered): Alice
         *     peek3 (mapped)  : ALICE
         *   Result: Optional[ALICE]
         *
         *   --- 2c. With limit(2) (short-circuit) ---
         *     peek1 (source)  : Alice
         *     peek3 (mapped)  : ALICE
         *     peek1 (source)  : Bob
         *     peek1 (source)  : Charlie
         *     peek3 (mapped)  : CHARLIE
         *   Result: [ALICE, CHARLIE]
         *
         *   --- 2d. With sorted (breaks the one-by-one flow) ---
         *     peek1 (source)  : Alice
         *     peek1 (source)  : Bob
         *     peek1 (source)  : Charlie
         *     peek1 (source)  : David
         *     peek1 (source)  : Eve
         *     peek3 (mapped)  : ALICE
         *     peek3 (mapped)  : CHARLIE
         *     peek3 (mapped)  : DAVID
         *   Result: [ALICE, CHARLIE, DAVID]
         *
         * - Section 1: filter, map and peek only DESCRIBE work. Without a
         *   terminal operation, no element is ever pulled from the source.
         *
         * - Section 2a: Alice goes through all three stages before Bob is
         *   even looked at. Elements move one at a time through the whole
         *   pipeline, not "all filters first, then all maps".
         *   Bob and Eve are dropped by filter, so they never reach peek2/peek3.
         *
         * - Section 2b: findFirst needs only one element, so David and Eve
         *   are never touched. Short-circuiting saves work.
         *
         * - Section 2c: limit(2) stops the pipeline after 2 elements passed
         *   through. David and Eve are never processed.
         *
         * - Section 2d: sorted cannot emit anything until it has seen every
         *   element, so all of peek1 runs first. This is why stateful
         *   operations (sorted, distinct) can be costly on big data.
         */
    }
}