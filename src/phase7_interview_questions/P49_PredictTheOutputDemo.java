package phase7_interview_questions;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Predict the output of lazy pipelines: peek, short-circuiting, stream reuse
 *
 * PROBLEM:
 *   Most people expect "all filters first, then all maps". Streams do not work
 *   like that, so predictions about the order of side effects are often wrong.
 *
 * SOLUTION:
 *   Remember three rules: nothing runs without a terminal operation, elements flow
 *   one at a time through the whole pipeline, and short-circuiting stops early.
 *
 * HOW TO USE:
 *   Read each case, write down your prediction, then run it and compare with the
 *   answer in the comment.
 */
public class P49_PredictTheOutputDemo {
    public static void main(String[] args) {

        System.out.println("--- Case 1 ---");
        Stream.of("a", "b", "c")
                .peek(x -> System.out.println("peek " + x))
                .filter(x -> !x.equals("b"))
                .map(String::toUpperCase);
        // Answer: nothing is printed (no terminal operation)

        System.out.println("--- Case 2 ---");
        Stream.of(1, 2, 3, 4, 5)
                .peek(n -> System.out.print("A" + n + " "))
                .filter(n -> n % 2 == 0)
                .peek(n -> System.out.print("B" + n + " "))
                .findFirst();
        System.out.println();
        // Answer: A1 A2 B2

        System.out.println("--- Case 3 ---");
        Stream.of(5, 3, 1)
                .peek(n -> System.out.print("P" + n + " "))
                .sorted()
                .peek(n -> System.out.print("S" + n + " "))
                .findFirst();
        System.out.println();
        // Answer: P5 P3 P1 S1  (sorted must see everything first)

        System.out.println("--- Case 4 ---");
        Stream<Integer> reused = Stream.of(1, 2, 3);
        System.out.println(reused.count());
        try {
            System.out.println(reused.count());
        } catch (IllegalStateException e) {
            System.out.println("IllegalStateException");
        }
        // Answer: 3, then IllegalStateException

        System.out.println("--- Case 5 ---");
        Stream.iterate(1, n -> n + 1)
                .filter(n -> n % 3 == 0)
                .limit(2)
                .forEach(n -> System.out.print(n + " "));
        System.out.println();
        // Answer: 3 6

        System.out.println("--- Case 6 ---");
        List<Integer> doubled = List.of(1, 2, 3).stream()
                .map(n -> {
                    System.out.print("M" + n + " ");
                    return n * 2;
                })
                .limit(2)
                .toList();
        System.out.println(doubled);
        // Answer: M1 M2 [2, 4]  (M3 never runs)

        System.out.println("--- Case 7 ---");
        long c1 = List.of(1, 2, 3).stream()
                .peek(n -> System.out.print("C" + n + " "))
                .count();
        System.out.println(c1);
        long c2 = List.of(1, 2, 3).stream()
                .filter(n -> n > 1)
                .peek(n -> System.out.print("F" + n + " "))
                .count();
        System.out.println(c2);
        // Answer: 3, then F2 F3 2  (count skips peek when the size is known, Java 9+)

        System.out.println("--- Case 8 ---");
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
        Stream<Integer> lazy = list.stream().map(n -> n * 2);
        list.add(4);
        System.out.println(lazy.toList());
        // Answer: [2, 4, 6, 8]  (the source is read when the terminal operation runs)
    }
}