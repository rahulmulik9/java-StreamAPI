package phase4_collectors;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Collectors.partitioningBy(predicate): split into exactly two groups (true / false)
 *
 * PROBLEM:
 *   groupingBy on a boolean only creates keys that actually occur. If every
 *   element is true, there is no "false" key and get(false) returns null.
 *
 * SOLUTION:
 *   partitioningBy always gives BOTH keys, even when one group is empty.
 */
public class P21_PartitioningByDemo {
    public static void main(String[] args) {
        List<Integer> allEven = List.of(2, 4, 6);

        // 1. Broken version: groupingBy on a boolean
        Map<Boolean, List<Integer>> grouped = allEven.stream()
                .collect(Collectors.groupingBy(n -> n % 2 == 0));
        System.out.println("groupingBy, odd numbers     : " + grouped.get(false));

        // 2. Fixed version: partitioningBy
        Map<Boolean, List<Integer>> parts = allEven.stream()
                .collect(Collectors.partitioningBy(n -> n % 2 == 0));
        System.out.println("partitioningBy, odd numbers : " + parts.get(false));
        System.out.println("partitioningBy, even numbers: " + parts.get(true));

        // With a downstream collector
        Map<Boolean, Long> counts = Stream.of(1, 2, 3, 4, 5)
                .collect(Collectors.partitioningBy(n -> n > 3, Collectors.counting()));
        System.out.println("counts : " + counts);
    }
}