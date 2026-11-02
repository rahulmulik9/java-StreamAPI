package phase4_collectors;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Collector.of(supplier, accumulator, combiner, finisher)
 *       supplier     : creates an empty container
 *       accumulator  : adds one element to the container
 *       combiner     : merges two containers (used by parallel streams)
 *       finisher     : converts the container into the final result
 *
 * PROBLEM:
 *   No built-in collector does "collect into a list in REVERSE order". The workaround
 *   needs a second step and relies on toList() returning a modifiable list,
 *   which is not guaranteed.
 *
 * SOLUTION:
 *   Write your own Collector once and reuse it in any pipeline.
 */
public class P24_CustomCollectorDemo {

    static Collector<String, List<String>, List<String>> reversing() {
        return Collector.of(
                ArrayList::new,                                        // supplier
                List::add,                                             // accumulator
                (left, right) -> { left.addAll(right); return left; }, // combiner
                list -> { Collections.reverse(list); return list; });  // finisher
    }

    public static void main(String[] args) {
        List<String> names = List.of("Alice", "Bob", "Charlie");

        // 1. Broken version: two steps, relies on a modifiable result
        List<String> list = names.stream().collect(Collectors.toList());
        Collections.reverse(list);
        System.out.println("two steps : " + list);

        // 2. Fixed version: one reusable collector
        System.out.println("custom    : " + names.stream().collect(reversing()));
        System.out.println("custom + filter : " + names.stream()
                .filter(n -> n.length() > 3)
                .collect(reversing()));
    }
}