package phase7_interview_questions;

import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Check a palindrome and an anagram using IntStream and sorted chars
 *
 * PROBLEM:
 *   Comparing raw strings fails on case, spaces and punctuation, and comparing
 *   the characters of two anagrams without sorting them gives false.
 *
 * SOLUTION:
 *   Clean the text first (lowercase, letters and digits only). For a palindrome compare
 *   mirrored positions with IntStream.allMatch. For an anagram compare the sorted characters.
 */
public class P44_PalindromeAnagramDemo {

    static String clean(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    static boolean isPalindrome(String s) {
        String c = clean(s);
        return IntStream.range(0, c.length() / 2)
                .allMatch(i -> c.charAt(i) == c.charAt(c.length() - 1 - i));
    }

    static boolean isAnagram(String a, String b) {
        return clean(a).chars().sorted().boxed().toList()
                .equals(clean(b).chars().sorted().boxed().toList());
    }

    public static void main(String[] args) {
        String phrase = "A man, a plan, a canal: Panama";

        // 1. Broken version
        System.out.println("raw palindrome : " + IntStream.range(0, phrase.length() / 2)
                .allMatch(i -> phrase.charAt(i) == phrase.charAt(phrase.length() - 1 - i)));
        System.out.println("anagram without sorting : " + "listen".chars().boxed().toList()
                .equals("silent".chars().boxed().toList()));

        // 2. Fixed version
        System.out.println("palindrome  : " + isPalindrome(phrase));
        System.out.println("racecar     : " + isPalindrome("racecar"));
        System.out.println("java        : " + isPalindrome("java"));
        System.out.println("listen/silent      : " + isAnagram("listen", "silent"));
        System.out.println("Dormitory/Dirty room : " + isAnagram("Dormitory", "Dirty room"));
        System.out.println("abc/abd            : " + isAnagram("abc", "abd"));
    }
}