package phase1_basics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - What a stream is (a pipeline of operations over data, not a data structure)
 *   - The three parts of a pipeline: source, intermediate operations, terminal operation
 *   - for-loop vs stream: HOW to do it vs WHAT to do
 *   - A stream does not modify its source
 *
 * PROBLEM:
 *   A for-loop that filters and transforms data needs a temporary list,
 *   manual add() calls and nested if-blocks. The real intent (keep names
 *   longer than 3 characters, make them uppercase) is buried in the mechanics.
 *
 * SOLUTION:
 *   A stream pipeline states the intent step by step:
 *   source -> filter -> map -> collect. Each step does one job and reads top to bottom.
 */
public class StreamIntroDemo {
    public static void main(String[] args) {

        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve", "Frank");

        // ------------------------------------------------------------
        // 1. Broken / verbose version: for-loop
        // ------------------------------------------------------------
        List<String> loopResult = new ArrayList<>();      // temporary list to manage
        for (String name : names) {
            if (name.length() > 3) {                      // filter logic
                loopResult.add(name.toUpperCase());       // transform logic, mixed with adding
            }
        }
        System.out.println("For-loop result : " + loopResult);

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------
        List<String> streamResult = names.stream()        // SOURCE: where the data comes from
                .filter(name -> name.length() > 3)        // INTERMEDIATE: keep matching elements
                .map(String::toUpperCase)                 // INTERMEDIATE: transform each element
                .collect(Collectors.toList());            // TERMINAL: produce the final result
        System.out.println("Stream result   : " + streamResult);

        // The source list is untouched. A stream never modifies its source.
        System.out.println("Original list   : " + names);

        // ------------------------------------------------------------
        // 3. Output explanation
        // ------------------------------------------------------------
        /*
         * Expected output:
         *   For-loop result : [ALICE, CHARLIE, DAVID, FRANK]
         *   Stream result   : [ALICE, CHARLIE, DAVID, FRANK]
         *   Original list   : [Alice, Bob, Charlie, David, Eve, Frank]
         *
         * - Both approaches give the same result. "Bob" and "Eve" have only
         *   3 characters, so filter(length > 3) removes them.
         *
         * - The pipeline has 3 parts:
         *     SOURCE        names.stream()            creates the stream
         *     INTERMEDIATE  filter(...), map(...)     return a new Stream, can be chained
         *     TERMINAL      collect(...)              ends the pipeline, returns a result
         *
         * - A stream is NOT a collection. It stores no data. It describes
         *   a computation over the data held by the source.
         *
         * - The loop says HOW (create list, iterate, check, add).
         *   The stream says WHAT (filter, map, collect).
         *
         * - Streams are not always better. For a very simple loop, a for-loop is fine.
         *   Streams shine when you chain several steps.
         */
    }
}