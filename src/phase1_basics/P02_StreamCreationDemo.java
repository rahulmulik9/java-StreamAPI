package phase1_basics;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Ways to create a stream: collection.stream(), Arrays.stream, Stream.of,
 *     Stream.generate, Stream.iterate, Stream.ofNullable, IntStream.range
 *
 * PROBLEM:
 *   Creating a stream from the wrong source gives surprising results:
 *   Stream.of(int[]) gives ONE element (the array itself), and
 *   Stream.of(nullValue) gives one null element that crashes the pipeline.
 *
 * SOLUTION:
 *   Pick the factory that matches the source: Arrays.stream or IntStream.of for
 *   primitive arrays, Stream.ofNullable for values that may be null.
 */
public class P02_StreamCreationDemo {
    public static void main(String[] args) {

        // ------------------------------------------------------------
        // 1. Broken version: common creation mistakes
        // ------------------------------------------------------------
        int[] numbers = {10, 20, 30};

        // Mistake A: Stream.of with a primitive array -> Stream<int[]> with 1 element
        long wrongCount = Stream.of(numbers).count();
        System.out.println("Stream.of(int[]) count : " + wrongCount);        // 1, not 3

        // Mistake B: Stream.of with a null value -> one null element -> NPE later
        String maybeNull = null;
        try {
            Stream.of(maybeNull)
                    .map(String::length)
                    .forEach(System.out::println);
        } catch (NullPointerException e) {
            System.out.println("Stream.of(null value)     : NullPointerException");
        }

        // ------------------------------------------------------------
        // 2. Fixed version: all the ways to create a stream
        // ------------------------------------------------------------

        // a) From a collection
        List<String> list = List.of("A", "B", "C");
        System.out.println("Collection              : " + list.stream().collect(Collectors.toList()));

        // b) From a Map (stream its entrySet, keySet or values)
        Map<String, Integer> map = Map.of("one", 1);
        System.out.println("Map entries             : " + map.entrySet().stream().count());

        // c) From an array (the fix for Mistake A)
        System.out.println("Arrays.stream(int[]) count : " + Arrays.stream(numbers).count());   // 3
        String[] words = {"java", "streams"};
        System.out.println("Arrays.stream(String[]) : " + Arrays.stream(words).collect(Collectors.toList()));

        // d) Stream.of with values
        System.out.println("Stream.of               : " + Stream.of("x", "y", "z").collect(Collectors.toList()));

        // e) Stream.generate: infinite, values from a Supplier -> MUST use limit
        System.out.println("generate + limit        : " + Stream.generate(() -> "hi").limit(3).collect(Collectors.toList()));

        // f) Stream.iterate: infinite, each value built from the previous one
        System.out.println("iterate + limit         : " + Stream.iterate(1, n -> n * 2).limit(5).collect(Collectors.toList()));

        // g) Stream.iterate with a stop condition (Java 9+), like a for-loop
        System.out.println("iterate with predicate  : " + Stream.iterate(1, n -> n <= 100, n -> n * 3).collect(Collectors.toList()));

        // h) Stream.ofNullable (Java 9+): the fix for Mistake B
        System.out.println("ofNullable(null) count  : " + Stream.ofNullable(maybeNull).count());      // 0
        System.out.println("ofNullable(\"abc\") count : " + Stream.ofNullable("abc").count());        // 1

        // i) IntStream.range / rangeClosed: numeric ranges without a loop
        System.out.println("range(1, 5)             : " + IntStream.range(1, 5).boxed().collect(Collectors.toList()));
        System.out.println("rangeClosed(1, 5)       : " + IntStream.rangeClosed(1, 5).boxed().collect(Collectors.toList()));

        // ------------------------------------------------------------
        // 3. Output explanation
        // ------------------------------------------------------------
        /*
         * Expected output:
         *   Stream.of(int[]) count : 1
         *   Stream.of(null value)     : NullPointerException
         *   Collection              : [A, B, C]
         *   Map entries             : 1
         *   Arrays.stream(int[]) count : 3
         *   Arrays.stream(String[]) : [java, streams]
         *   Stream.of               : [x, y, z]
         *   generate + limit        : [hi, hi, hi]
         *   iterate + limit         : [1, 2, 4, 8, 16]
         *   iterate with predicate  : [1, 3, 9, 27, 81]
         *   ofNullable(null) count  : 0
         *   ofNullable("abc") count : 1
         *   range(1, 5)             : [1, 2, 3, 4]
         *   rangeClosed(1, 5)       : [1, 2, 3, 4, 5]
         *
         * - Stream.of(int[]) treats the whole array as ONE object. For a
         *   primitive array use Arrays.stream(arr) or IntStream.of(arr).
         *   (For an Integer[] or String[], Stream.of does spread the elements.)
         *
         * - Stream.of(nullVariable) creates a stream holding one null, so
         *   String::length throws NPE. Stream.ofNullable returns an empty
         *   stream for null, so the pipeline simply does nothing.
         *
         * - generate and iterate (without a predicate) are INFINITE.
         *   Always add limit() or a stop condition, or the program never ends.
         *
         * - range(1, 5) excludes 5. rangeClosed(1, 5) includes 5.
         */
    }
}