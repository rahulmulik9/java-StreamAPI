package phase7_interview_questions;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Sum, max, product and string concatenation using reduce
 *   - reduce (combine into ONE value) vs collect (build a container)
 *
 * PROBLEM:
 *   reduce without an identity returns an Optional, and get() on an empty stream throws.
 *   Using reduce to build a list changes the same identity object again and again,
 *   which breaks on parallel streams.
 *
 * SOLUTION:
 *   Use reduce for single values (sum, max, product). Use collect for lists, sets,
 *   maps and joined text.
 */
public class P47_ReduceInterviewDemo {
    public static void main(String[] args) {
        List<Integer> numbers = List.of(3, 1, 4, 1, 5, 9, 2, 6);
        List<String> words = List.of("java", "is", "fun");

        // 1. Broken version
        try {
            List.<Integer>of().stream().reduce(Integer::max).get();
        } catch (NoSuchElementException e) {
            System.out.println("max of empty list : NoSuchElementException");
        }

        List<Integer> viaReduce = numbers.stream().reduce(new ArrayList<Integer>(),
                (list, n) -> { list.add(n); return list; },       // changes the identity object
                (a, b) -> a);
        System.out.println("list via reduce (not safe in parallel) : " + viaReduce);

        // 2. Fixed version
        System.out.println("sum     : " + numbers.stream().reduce(0, Integer::sum));
        System.out.println("product : " + numbers.stream().reduce(1, (a, b) -> a * b));
        System.out.println("max     : " + numbers.stream().reduce(Integer::max).get());
        System.out.println("max, empty-safe : " + List.<Integer>of().stream().reduce(Integer::max).orElse(-1));
        System.out.println("concat  : " + words.stream().reduce("", String::concat));
        System.out.println("longest : " + words.stream().reduce((a, b) -> a.length() >= b.length() ? a : b).get());

        System.out.println("joining : " + words.stream().collect(Collectors.joining(" ")));
        System.out.println("collect list : " + numbers.stream().collect(Collectors.toList()));
    }
}