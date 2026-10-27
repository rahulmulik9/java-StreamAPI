package phase2_intermediate_ops;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - takeWhile(Predicate): take elements from the start, STOP at the first one that fails
 *   - dropWhile(Predicate): drop elements from the start, KEEP everything from the first one that fails
 *   - filter vs takeWhile: filter checks EVERY element, takeWhile stops early
 *   - They are only meaningful on ORDERED data (sorted, or "good elements first")
 *   - Both are available since Java 9
 *
 * PROBLEM:
 *   filter on sorted data keeps testing elements long after the answer is known,
 *   and on an infinite stream it never finishes. Using takeWhile/dropWhile on
 *   unordered data gives surprising results, because they stop or start at the
 *   FIRST failure and never look further.
 *
 * SOLUTION:
 *   Use takeWhile/dropWhile when the data is ordered and the condition flips only once.
 *   Use filter when the matching elements can be anywhere in the stream.
 */
public class P10_TakeWhileDropWhileDemo {

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    public static void main(String[] args) {

        List<Integer> sorted = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> unordered = List.of(2, 4, 6, 9, 8, 10, 3);

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) filter on sorted data: the predicate runs for ALL 10 elements
        AtomicInteger filterCalls = new AtomicInteger();
        List<Integer> filtered = sorted.stream()
                .filter(n -> {
                    filterCalls.incrementAndGet();
                    return n < 4;
                })
                .collect(Collectors.toList());
        show("filter result", filtered);
        show("filter predicate calls", filterCalls.get());

        // b) filter on an infinite stream: never finishes (do NOT run this)
        //    Stream.iterate(1, n -> n + 1).filter(n -> n < 5).collect(Collectors.toList());
        //    filter cannot know that no later element will match, so it keeps going forever.

        // c) takeWhile on UNORDERED data: stops at the first failure (9)
        show("filter even", unordered.stream().filter(n -> n % 2 == 0).collect(Collectors.toList()));
        show("takeWhile even (surprise)", unordered.stream().takeWhile(n -> n % 2 == 0).collect(Collectors.toList()));

        // d) dropWhile on UNORDERED data: 8 and 10 are even but still kept
        show("dropWhile even (surprise)", unordered.stream().dropWhile(n -> n % 2 == 0).collect(Collectors.toList()));

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) takeWhile on sorted data: stops at 4, so only 4 predicate calls
        AtomicInteger takeCalls = new AtomicInteger();
        List<Integer> taken = sorted.stream()
                .takeWhile(n -> {
                    takeCalls.incrementAndGet();
                    return n < 4;
                })
                .collect(Collectors.toList());
        show("takeWhile result", taken);
        show("takeWhile predicate calls", takeCalls.get());

        // b) dropWhile on sorted data
        show("dropWhile(n < 4)", sorted.stream().dropWhile(n -> n < 4).collect(Collectors.toList()));

        // c) Window: dropWhile + takeWhile together = a range
        show("window 3..6", sorted.stream()
                .dropWhile(n -> n < 3)
                .takeWhile(n -> n <= 6)
                .collect(Collectors.toList()));

        // d) Infinite stream: takeWhile makes it finite, no limit needed
        show("powers of 2 below 100", Stream.iterate(1, n -> n * 2)
                .takeWhile(n -> n < 100)
                .collect(Collectors.toList()));

        // e) Strings: leading digits, and the rest
        String text = "123abc456";
        show("leading digits", text.chars()
                .takeWhile(Character::isDigit)
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining()));
        show("after leading digits", text.chars()
                .dropWhile(Character::isDigit)
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining()));

        // f) Real use: "stop at the first problem" in a time-ordered series
        List<Integer> temperatures = List.of(18, 20, 22, 25, 31, 35, 29, 24);
        show("filter (<30)", temperatures.stream().filter(t -> t < 30).collect(Collectors.toList()));
        show("takeWhile (<30)", temperatures.stream().takeWhile(t -> t < 30).collect(Collectors.toList()));

        // g) takeWhile + dropWhile with the same predicate split the stream at the first failure
        List<Integer> head = temperatures.stream().takeWhile(t -> t < 30).collect(Collectors.toList());
        List<Integer> tail = temperatures.stream().dropWhile(t -> t < 30).collect(Collectors.toList());
        show("head", head);
        show("tail", tail);

        // h) Real use: skip the header lines of a file
        List<String> lines = List.of("# generated file", "# author: demo", "name,age", "Alice,30", "Bob,25");
        show("data lines", lines.stream()
                .dropWhile(line -> line.startsWith("#"))
                .collect(Collectors.toList()));

        // i) On an unordered or parallel stream the result of takeWhile/dropWhile is not
        //    predictable, because "the first failure" has no fixed position.
        //    If the order matters, keep the stream sequential.
    }
}