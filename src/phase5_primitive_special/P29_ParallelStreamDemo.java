package phase6_parallel_production;

import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - parallel() / parallelStream(): split the work across several threads
 *   - forEach vs forEachOrdered, and collect keeps the encounter order
 *   - When parallel helps and when it hurts
 *
 * PROBLEM:
 *   Parallel forEach prints in a random order, because threads finish at different
 *   times. And parallel is not free: on small data, the cost of splitting and
 *   merging is larger than the work itself.
 *
 * SOLUTION:
 *   Use forEachOrdered (or collect) when order matters, and use parallel only for
 *   large data, CPU-heavy work and sources that split well (arrays, ArrayList, ranges).
 */
public class P29_ParallelStreamDemo {
    public static void main(String[] args) {

        // 1. Broken version: order is not predictable
        System.out.print("parallel forEach : ");
        IntStream.rangeClosed(1, 10).parallel().forEach(n -> System.out.print(n + " "));
        System.out.println();

        System.out.println("--- which threads run the work ---");
        IntStream.range(0, 4).parallel()
                .forEach(n -> System.out.println("  " + n + " on " + Thread.currentThread().getName()));

        // 2. Fixed version
        System.out.print("forEachOrdered   : ");
        IntStream.rangeClosed(1, 10).parallel().forEachOrdered(n -> System.out.print(n + " "));
        System.out.println();

        System.out.println("collect keeps order : " + IntStream.rangeClosed(1, 10).parallel().boxed().toList());

        long sum = LongStream.rangeClosed(1, 10_000_000).parallel().sum();
        System.out.println("big parallel sum    : " + sum);

        System.out.println("isParallel : " + IntStream.range(0, 3).parallel().isParallel());
    }
}