package phase7_interview_questions;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Sort a Map by value or by key
 *   - entrySet().stream(), Map.Entry.comparingByValue / comparingByKey, LinkedHashMap
 *
 * PROBLEM:
 *   Collecting sorted entries with toMap(key, value) gives a HashMap, so the
 *   sorted order is thrown away. Also, Map.Entry.comparingByValue().reversed()
 *   does not compile because Java cannot infer the types.
 *
 * SOLUTION:
 *   Collect into a LinkedHashMap (keeps encounter order) and give the
 *   comparator its types: comparingByValue(Comparator.reverseOrder()).
 */
public class P41_SortMapByValueDemo {
    public static void main(String[] args) {
        Map<String, Integer> scores = Map.of("Alice", 70, "Bob", 90, "Charlie", 80, "David", 90);

        // 1. Broken version
        Map<String, Integer> lost = scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println("toMap (HashMap, order lost) : " + lost);
        // .sorted(Map.Entry.comparingByValue().reversed())    // compile error: type inference

        // 2. Fixed version
        Map<String, Integer> ascending = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        System.out.println("by value ascending  : " + ascending);

        Map<String, Integer> descending = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        System.out.println("by value descending : " + descending);

        System.out.println("by key              : " + new TreeMap<>(scores));

        System.out.println("top 2 names         : " + descending.keySet().stream().limit(2).toList());
    }
}