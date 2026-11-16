package phase7_interview_questions;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Count the frequency of each character or word
 *   - groupingBy(identity, counting), toMap with a merge function
 *
 * PROBLEM:
 *   toMap without a merge function throws IllegalStateException as soon as
 *   a word appears twice. And split(" ") treats "The" and "the," as different words.
 *
 * SOLUTION:
 *   groupingBy(Function.identity(), counting()), or toMap with Integer::sum.
 *   Lowercase the text and split on non-word characters.
 */
public class P39_CharWordFrequencyDemo {
    public static void main(String[] args) {
        String text = "the cat and the hat and the bat";

        // 1. Broken version
        try {
            Arrays.stream(text.split(" ")).collect(Collectors.toMap(w -> w, w -> 1));
        } catch (IllegalStateException e) {
            System.out.println("toMap, no merge : IllegalStateException (duplicate key)");
        }

        // 2. Fixed version
        Map<String, Long> words = Arrays.stream(text.split(" "))
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
        System.out.println("words      : " + words);

        System.out.println("toMap merge: " + Arrays.stream(text.split(" "))
                .collect(Collectors.toMap(w -> w, w -> 1, Integer::sum, TreeMap::new)));

        System.out.println("characters : " + "banana".chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting())));

        System.out.println("most common: " + words.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("none"));

        System.out.println("clean text : " + Arrays.stream("The cat, the hat!".toLowerCase().split("\\W+"))
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting())));
    }
}