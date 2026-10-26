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
        int[] primitiveNumbers = {10, 20, 30};
        Integer[] objectNumbers = {10, 20, 30};

        // Mistake A: Stream.of with a primitive array -> Stream<int[]> with 1 element
        //int[], long[], double[], char[], boolean[]
        long primitiveCount = Stream.of(primitiveNumbers).count();
        long primtiveCountArray = Arrays.stream(primitiveNumbers).count();
        long objectCount = Stream.of(objectNumbers).count();
        System.out.println("Stream.of(int[]) primitive count     : " + primitiveCount);        // 1, not 3
        System.out.println("Arrays.stream(int[]) primitive count : " + primtiveCountArray);        // 1, not 3
        System.out.println("Stream.of(int[]) object count        : " + objectCount);

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
        Map<String, Integer> map = Map.of(
                "one", 1,
                "two", 2,
                "three", 3,
                "four", 4,
                "five", 5
        );
        System.out.println("Map entries             : " + map.keySet().stream().count());
        System.out.println("Map keys                : " + map.entrySet().stream().count());

        // c) for string class
        String[] words = {"java", "streams"};
        System.out.println("Arrays.stream(String[]) : " + Arrays.stream(words).collect(Collectors.toList()));

        // d) Stream.of with values
        System.out.println("Stream.of               : " + Stream.of("x", "y", "z").collect(Collectors.toList()));

        // e) Stream.generate: infinite, values from a Supplier -> MUST use limit
        System.out.println("generate + limit        : " + Stream.generate(() -> "hi").limit(3).collect(Collectors.toList()));

        // f) Stream.iterate: infinite, each value built from the previous one
        System.out.println("iterate + limit         : " + Stream.iterate(1, n -> n * 2).limit(10).collect(Collectors.toList()));

        // g) Stream.iterate with a stop condition (Java 9+), like a for-loop
        System.out.println("iterate with predicate  : " + Stream.iterate(1, n -> n <= 100, n -> n * 2).collect(Collectors.toList()));

        // h) Stream.ofNullable (Java 9+): the fix for Mistake B
        System.out.println("ofNullable(null) count  : " + Stream.ofNullable(maybeNull).count());      // 0
        System.out.println("ofNullable(\"abc\") count : " + Stream.ofNullable("abc").count());        // 1

        // i) IntStream.range / rangeClosed: numeric ranges without a loop
        System.out.println("range(1, 5)             : " + IntStream.range(1, 5).boxed().collect(Collectors.toList()));
        System.out.println("rangeClosed(1, 5)       : " + IntStream.rangeClosed(1, 5).boxed().collect(Collectors.toList()));

    }
}