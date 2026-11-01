package phase4_collectors;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Collectors.teeing(collector1, collector2, merger): two collectors, one pass
 *
 * PROBLEM:
 *   To get an average you need a sum AND a count. A stream can only be used
 *   once, so you must build the stream twice (two passes over the data).
 *
 * SOLUTION:
 *   teeing runs two collectors on the same pass and merges their results.
 */
public class P23_TeeingDemo {
    public static void main(String[] args) {
        List<Integer> numbers = List.of(4, 8, 15, 16, 23, 42);

        // 1. Broken version: the same stream cannot be used twice
        Stream<Integer> stream = numbers.stream();
        long count = stream.count();
        try {
            stream.mapToInt(n -> n).sum();
        } catch (IllegalStateException e) {
            System.out.println("second pass failed : stream already used (count was " + count + ")");
        }

        // 2. Fixed version: sum and count in one pass
        double average = numbers.stream().collect(Collectors.teeing(
                Collectors.summingInt(n -> n),
                Collectors.counting(),
                (sum, cnt) -> (double) sum / cnt));
        System.out.println("average : " + average);

        // Another example: min and max together
        String range = numbers.stream().collect(Collectors.teeing(
                Collectors.minBy(Comparator.naturalOrder()),
                Collectors.maxBy(Comparator.naturalOrder()),
                (min, max) -> min.get() + " to " + max.get()));
        System.out.println("range   : " + range);
    }
}