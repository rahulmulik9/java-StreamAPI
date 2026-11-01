package phase4_collectors;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - joining(delimiter, prefix, suffix)
 *   - averagingInt, summingInt
 *   - summarizingInt: count, sum, min, average, max in one collector
 *
 * PROBLEM:
 *   Building text with reduce leaves a stray separator at the start. Also,
 *   joining only accepts text (CharSequence), so a stream of numbers does not compile.
 *
 * SOLUTION:
 *   Use joining for text (convert numbers with map(String::valueOf) first) and
 *   summarizingInt when you need several numbers at once.
 */
public class P22_JoiningSummarizingDemo {
    public static void main(String[] args) {
        List<String> names = List.of("Alice", "Bob", "Charlie");
        List<Integer> marks = List.of(70, 85, 90, 60);

        // 1. Broken version
        String broken = names.stream().reduce("", (a, b) -> a + ", " + b);
        System.out.println("reduce  : " + broken);
        // marks.stream().collect(Collectors.joining(","));   // compile error: not text

        // 2. Fixed version
        System.out.println("joining : " + names.stream().collect(Collectors.joining(", ", "[", "]")));
        System.out.println("numbers : " + marks.stream().map(String::valueOf).collect(Collectors.joining("-")));
        System.out.println("average : " + marks.stream().collect(Collectors.averagingInt(m -> m)));
        System.out.println("sum     : " + marks.stream().collect(Collectors.summingInt(m -> m)));

        IntSummaryStatistics stats = marks.stream().collect(Collectors.summarizingInt(m -> m));
        System.out.println("stats   : " + stats);
        System.out.println("min / max : " + stats.getMin() + " / " + stats.getMax());
    }
}