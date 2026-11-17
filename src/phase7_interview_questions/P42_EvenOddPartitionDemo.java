package phase7_interview_questions;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Separate even and odd numbers, and find the sum of squares of the even ones
 *   - partitioningBy with a downstream collector
 *
 * PROBLEM:
 *   Two filter() calls walk the data twice. And n % 2 == 1 is a classic bug:
 *   for negative numbers n % 2 is -1, so odd negatives are missed.
 *
 * SOLUTION:
 *   partitioningBy splits in one pass, and the odd test is n % 2 != 0.
 */
public class P42_EvenOddPartitionDemo {
    public static void main(String[] args) {
        List<Integer> numbers = List.of(-3, -2, 1, 2, 3, 4, 5, 6);

        // 1. Broken version
        List<Integer> evens1 = numbers.stream().filter(n -> n % 2 == 0).toList();
        List<Integer> odds1 = numbers.stream().filter(n -> n % 2 == 1).toList();   // misses -3
        System.out.println("two passes, odd (wrong) : " + odds1);

        // 2. Fixed version
        Map<Boolean, List<Integer>> parts = numbers.stream()
                .collect(Collectors.partitioningBy(n -> n % 2 == 0));
        System.out.println("even : " + parts.get(true));
        System.out.println("odd  : " + parts.get(false));

        System.out.println("sum of squares of evens : " + numbers.stream()
                .filter(n -> n % 2 == 0)
                .mapToInt(n -> n * n)
                .sum());

        System.out.println("sum of squares per group : " + numbers.stream()
                .collect(Collectors.partitioningBy(n -> n % 2 == 0, Collectors.summingInt(n -> n * n))));

        System.out.println("count per group : " + numbers.stream()
                .collect(Collectors.partitioningBy(n -> n % 2 == 0, Collectors.counting())));
    }
}