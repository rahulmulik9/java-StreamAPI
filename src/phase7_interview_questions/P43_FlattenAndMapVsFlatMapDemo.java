package phase7_interview_questions;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Difference between map and flatMap
 *   - Flatten a list of lists
 *
 * PROBLEM:
 *   map is one-to-one, so mapping a list of lists still gives a list of lists.
 *   Splitting sentences with map gives a list of arrays, not a list of words.
 *
 * SOLUTION:
 *   flatMap maps each element to a stream and merges all those streams into one.
 */
public class P43_FlattenAndMapVsFlatMapDemo {
    public static void main(String[] args) {
        List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
        List<String> sentences = List.of("java is fun", "streams are fun");

        // 1. Broken version: map keeps the nesting
        System.out.println("map     : " + nested.stream()
                .map(list -> list.stream().map(n -> n * 2).toList())
                .toList());

        System.out.println("map split : " + sentences.stream().map(s -> s.split(" ")).count() + " arrays, not words");

        // 2. Fixed version
        System.out.println("flatMap : " + nested.stream()
                .flatMap(List::stream)
                .map(n -> n * 2)
                .toList());

        System.out.println("words   : " + sentences.stream()
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .toList());

        System.out.println("unique words : " + sentences.stream()
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .distinct()
                .sorted()
                .toList());
    }
}