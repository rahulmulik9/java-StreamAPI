package phase7_interview_questions;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - findFirst vs findAny, on sequential and parallel streams
 *
 * PROBLEM:
 *   On a sequential stream findAny usually looks the same as findFirst, so people
 *   assume they are equal. On a parallel stream findAny can return ANY matching element,
 *   and the result changes from run to run.
 *
 * SOLUTION:
 *   Use findFirst when the first match in encounter order matters.
 *   Use findAny when any match is fine, which is cheaper in parallel.
 */
public class P46_FindFirstVsFindAnyDemo {
    public static void main(String[] args) {
        List<Integer> numbers = IntStream.rangeClosed(1, 1000).boxed().toList();

        // 1. Broken version: assuming findAny returns the first match on a parallel stream
        Set<Integer> results = new TreeSet<>();
        for (int i = 0; i < 30; i++) {
            results.add(numbers.parallelStream().filter(n -> n % 100 == 0).findAny().get());
        }
        System.out.println("parallel findAny, 30 runs (may vary) : " + results);

        // 2. Fixed version
        System.out.println("sequential findFirst : " + numbers.stream().filter(n -> n % 100 == 0).findFirst().get());
        System.out.println("sequential findAny   : " + numbers.stream().filter(n -> n % 100 == 0).findAny().get());

        Set<Integer> firsts = new TreeSet<>();
        for (int i = 0; i < 30; i++) {
            firsts.add(numbers.parallelStream().filter(n -> n % 100 == 0).findFirst().get());
        }
        System.out.println("parallel findFirst, 30 runs : " + firsts);
    }
}