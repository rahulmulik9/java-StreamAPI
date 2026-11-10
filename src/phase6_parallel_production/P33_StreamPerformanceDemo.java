package phase6_parallel_production;

import java.util.*;
import java.util.function.LongSupplier;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Loop vs stream, and the cost of boxing (int -> Integer)
 *   - A simple timing helper (not a real benchmark)
 *
 * PROBLEM:
 *   Stream<Integer> works on Integer objects, which costs memory and time.
 *   And timing code once, without warm-up, says more about the JVM starting
 *   up than about your code.
 *
 * SOLUTION:
 *   Use primitive streams (IntStream, mapToInt) for numeric work, warm up by
 *   running several times, and use JMH for serious measurements.
 */
public class P33_StreamPerformanceDemo {

    static void time(String label, LongSupplier task) {
        long best = Long.MAX_VALUE;
        long result = 0;
        for (int i = 0; i < 5; i++) {                  // repeat: the first runs are slow (warm-up)
            long start = System.nanoTime();
            result = task.getAsLong();
            best = Math.min(best, System.nanoTime() - start);
        }
        System.out.println(label + " : " + best / 1_000 + " us (result " + result + ")");
    }

    public static void main(String[] args) {
        int[] data = IntStream.range(0, 2_000_000).toArray();
        List<Integer> boxed = Arrays.stream(data).boxed().toList();

        // 1. Broken version: boxed numbers
        time("Stream<Integer> reduce   ", () -> boxed.stream().reduce(0L, (acc, n) -> acc + n, Long::sum));

        // 2. Fixed version: primitives
        time("for-loop on int[]        ", () -> {
            long sum = 0;
            for (int n : data) {
                sum += n;
            }
            return sum;
        });
        time("IntStream                ", () -> Arrays.stream(data).asLongStream().sum());
        time("Stream<Integer> mapToLong", () -> boxed.stream().mapToLong(Integer::longValue).sum());
    }
}