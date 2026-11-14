package phase7_interview_questions;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Find the duplicate elements in a list
 *   - Set.add() trick, and groupingBy + counting
 *
 * PROBLEM:
 *   distinct() returns the UNIQUE values, not the duplicated ones, so it
 *   is the opposite of what the question asks.
 *
 * SOLUTION:
 *   Keep elements that Set.add() rejects (already seen), or count occurrences
 *   with groupingBy and keep the groups with a count above 1.
 */
public class P37_FindDuplicatesDemo {
    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 2, 3, 2, 4, 3, 5, 2);

        // 1. Broken version
        System.out.println("distinct (not duplicates) : " + numbers.stream().distinct().toList());

        // 2. Fixed version
        // a) Set.add trick: add() returns false when the element is already in the set
        Set<Integer> seen = new HashSet<>();
        System.out.println("Set.add trick  : " + numbers.stream()
                .filter(n -> !seen.add(n))
                .distinct()
                .sorted()
                .toList());

        // b) groupingBy + counting: also gives the counts
        Map<Integer, Long> counts = numbers.stream()
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
        System.out.println("counts         : " + counts);
        System.out.println("duplicates     : " + counts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList());

        // c) Short, but compares every element with every other: O(n^2)
        System.out.println("frequency      : " + numbers.stream()
                .filter(n -> Collections.frequency(numbers, n) > 1)
                .distinct()
                .toList());
    }
}