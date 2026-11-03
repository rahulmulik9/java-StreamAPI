package phase5_primitive_special;

import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Stream.iterate(seed, next), Stream.iterate(seed, hasNext, next), Stream.generate(supplier)
 *   - limit / takeWhile are the exit for an infinite stream
 *
 * PROBLEM:
 *   An infinite stream with no exit never ends. Operations that need ALL the
 *   elements (sorted, distinct on endless data, collect) hang forever. A quieter
 *   trap: int overflow turns values negative without any error.
 *
 * SOLUTION:
 *   Always add limit() or a stop condition, keep sorted() out of infinite
 *   pipelines, and use long when numbers grow fast.
 */
public class P27_InfiniteStreamDemo {
    public static void main(String[] args) {

        // 1. Broken version
        // Stream.iterate(1, n -> n + 1).forEach(System.out::println);   // never ends
        // Stream.iterate(1, n -> n + 1).sorted().limit(5);              // sorted() waits for ALL elements
        System.out.print("int overflow  : ");
        Stream.iterate(1, n -> n * 2).skip(30).limit(4).forEach(n -> System.out.print(n + " "));
        System.out.println();

        // 2. Fixed version
        System.out.print("long          : ");
        Stream.iterate(1L, n -> n * 2).skip(30).limit(4).forEach(n -> System.out.print(n + " "));
        System.out.println();

        System.out.println("iterate + limit  : " + Stream.iterate(1, n -> n * 2).limit(6).toList());
        System.out.println("iterate + stop   : " + Stream.iterate(1, n -> n <= 100, n -> n * 3).toList());
        System.out.println("generate + limit : " + Stream.generate(() -> "hi").limit(3).toList());
        System.out.println("takeWhile        : " + Stream.iterate(1, n -> n + 1).takeWhile(n -> n < 5).toList());
    }
}