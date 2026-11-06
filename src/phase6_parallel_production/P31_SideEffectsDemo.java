package phase6_parallel_production;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Side effects: a lambda that changes something outside itself
 *   - Stateful lambdas: the result depends on a variable that changes during the pipeline
 *   - Never modify the source while streaming it
 *
 * PROBLEM:
 *   A counter or an outside list inside a lambda seems to work, but the result
 *   depends on execution order. In a parallel stream it is unpredictable, and
 *   changing the source list during the stream throws an exception.
 *
 * SOLUTION:
 *   Keep lambdas pure (output depends only on input). Let the stream build the
 *   result (toList, collect), use an index stream instead of a counter, and
 *   change the source only after the stream is finished.
 */
public class P31_SideEffectsDemo {
    public static void main(String[] args) {
        List<String> names = List.of("Alice", "Bob", "Charlie");

        // 1. Broken version
        List<Integer> source = new ArrayList<>(List.of(1, 2, 3));
        try {
            source.stream().forEach(n -> source.add(n * 10));
        } catch (ConcurrentModificationException e) {
            System.out.println("modify the source : ConcurrentModificationException");
        }

        int[] counter = {0};
        List<String> numbered = IntStream.rangeClosed(1, 8).parallel()
                .mapToObj(n -> n + "#" + ++counter[0])
                .toList();
        System.out.println("stateful lambda (may differ per run) : " + numbered);

        List<String> upper = new ArrayList<>();
        names.stream().map(String::toUpperCase).forEach(upper::add);   // works, but fragile
        System.out.println("forEach + add : " + upper);

        // 2. Fixed version
        System.out.println("toList        : " + names.stream().map(String::toUpperCase).toList());

        System.out.println("index stream  : " + IntStream.range(0, names.size())
                .mapToObj(i -> (i + 1) + ". " + names.get(i))
                .toList());

        List<Integer> extra = source.stream().map(n -> n * 10).toList();   // stream finishes first
        source.addAll(extra);                                              // then change the source
        System.out.println("source after addAll : " + source);

        List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4));
        numbers.removeIf(n -> n % 2 == 0);
        System.out.println("removeIf      : " + numbers);
    }
}