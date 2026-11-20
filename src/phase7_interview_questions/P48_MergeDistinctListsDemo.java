package phase7_interview_questions;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Merge two lists, remove duplicates and sort
 *   - Stream.concat, distinct, sorted
 *
 * PROBLEM:
 *   addAll() changes the first list, and fails on an immutable list. Merging
 *   and sorting without distinct() keeps the duplicates.
 *
 * SOLUTION:
 *   Stream.concat(a, b).distinct().sorted() builds a new list and leaves both inputs alone.
 */
public class P48_MergeDistinctListsDemo {
    public static void main(String[] args) {
        List<Integer> a = List.of(5, 1, 3);
        List<Integer> b = List.of(3, 2, 5);

        // 1. Broken version
        try {
            a.addAll(b);
        } catch (UnsupportedOperationException e) {
            System.out.println("addAll on immutable list : UnsupportedOperationException");
        }
        System.out.println("no distinct : " + Stream.concat(a.stream(), b.stream()).sorted().toList());

        // 2. Fixed version
        System.out.println("merged      : " + Stream.concat(a.stream(), b.stream())
                .distinct()
                .sorted()
                .toList());

        System.out.println("descending  : " + Stream.concat(a.stream(), b.stream())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList());

        System.out.println("three lists : " + Stream.of(a, b, List.of(9, 1))
                .flatMap(List::stream)
                .distinct()
                .sorted()
                .toList());

        System.out.println("TreeSet     : " + Stream.concat(a.stream(), b.stream())
                .collect(Collectors.toCollection(TreeSet::new)));

        System.out.println("inputs unchanged : " + a + " " + b);
    }
}