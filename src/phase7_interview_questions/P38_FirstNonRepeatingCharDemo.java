package phase7_interview_questions;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - First non-repeating character in a string
 *   - groupingBy with LinkedHashMap, and the indexOf/lastIndexOf alternative
 *
 * PROBLEM:
 *   groupingBy returns a HashMap by default, which does not keep insertion
 *   order, so "first" non-repeating becomes a random one.
 *
 * SOLUTION:
 *   Collect into a LinkedHashMap (it keeps encounter order), then take the
 *   first entry with a count of 1.
 */
public class P38_FirstNonRepeatingCharDemo {

    static Optional<Character> firstNonRepeating(String s) {
        return s.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    static Optional<Character> viaIndexOf(String s) {
        return s.chars()
                .filter(c -> s.indexOf(c) == s.lastIndexOf(c))
                .mapToObj(c -> (char) c)
                .findFirst();
    }

    public static void main(String[] args) {

        // 1. Broken version: default HashMap, order is lost
        Optional<Character> wrong = "stress".chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .map(Map.Entry::getKey)
                .findFirst();
        System.out.println("HashMap (usually wrong) : " + wrong);

        // 2. Fixed version
        System.out.println("swiss   : " + firstNonRepeating("swiss"));
        System.out.println("stress  : " + firstNonRepeating("stress"));
        System.out.println("aabb    : " + firstNonRepeating("aabb"));
        System.out.println("indexOf : " + viaIndexOf("stress"));
    }
}