package phase6_parallel_production;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Shared mutable state breaks in parallel streams (race conditions)
 *   - The safe alternatives: collect, count, sum, reduce
 *   - All parallel streams share ONE common thread pool
 *
 * PROBLEM:
 *   Many threads writing to the same ArrayList or the same counter overwrite
 *   each other. The result has missing elements, a wrong count, or an exception.
 *   Worse, it can pass a quick test and fail in production.
 *
 * SOLUTION:
 *   Let the stream build the result (collect, count, sum) and never write to
 *   shared variables from inside a lambda.
 */
public class P30_ParallelPitfallsDemo {
    public static void main(String[] args) {

        // 1. Broken version: shared mutable state
        List<Integer> unsafe = new ArrayList<>();
        try {
            IntStream.range(0, 10_000).parallel().forEach(unsafe::add);
            System.out.println("unsafe list size (expected 10000) : " + unsafe.size());
        } catch (Exception e) {
            System.out.println("unsafe list threw : " + e.getClass().getSimpleName());
        }

        int[] counter = {0};
        IntStream.range(0, 10_000).parallel().forEach(n -> counter[0]++);
        System.out.println("unsafe counter (expected 10000)   : " + counter[0]);

        // 2. Fixed version
        List<Integer> safeList = IntStream.range(0, 10_000).parallel().boxed().collect(Collectors.toList());
        System.out.println("collect size : " + safeList.size());

        long count = IntStream.range(0, 10_000).parallel().count();
        System.out.println("count        : " + count);

        int total = IntStream.range(0, 10_000).parallel().reduce(0, Integer::sum);
        System.out.println("reduce sum   : " + total);

        // Common pool: parallel streams across the whole JVM share the same threads.
        // A slow or blocking task inside one parallel stream slows down all the others.
    }
}