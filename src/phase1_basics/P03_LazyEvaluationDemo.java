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
                .filter(n -> n.length() > 3)
                .map(String::toUpperCase)
                .findFirst();
        System.out.println("Result: " + first);

        // c) limit(2): stops once 2 elements have passed
        System.out.println("\n--- 2c. With limit(2) (short-circuit) ---");
        List<String> limited = names.stream()
                .filter(n -> n.length() > 3)
                .map(String::toUpperCase)
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

    }
}