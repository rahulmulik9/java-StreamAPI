package phase5_primitive_special;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - summaryStatistics(): count, sum, min, max and average in ONE pass
 *
 * PROBLEM:
 *   Calling sum(), average(), min() and max() separately walks the data four times,
 *   and each one needs its own stream.
 *
 * SOLUTION:
 *   summaryStatistics() collects everything in a single traversal.
 */
public class P26_SummaryStatisticsDemo {
    public static void main(String[] args) {
        List<Integer> marks = List.of(70, 85, 90, 60);

        // 1. Broken version: four passes
        int sum = marks.stream().mapToInt(m -> m).sum();
        double avg = marks.stream().mapToInt(m -> m).average().orElse(0);
        int min = marks.stream().mapToInt(m -> m).min().getAsInt();
        int max = marks.stream().mapToInt(m -> m).max().getAsInt();
        System.out.println("four passes : " + sum + ", " + avg + ", " + min + ", " + max);

        // 2. Fixed version: one pass
        IntSummaryStatistics s = marks.stream().mapToInt(m -> m).summaryStatistics();
        System.out.println("count   : " + s.getCount());
        System.out.println("sum     : " + s.getSum());
        System.out.println("min     : " + s.getMin());
        System.out.println("max     : " + s.getMax());
        System.out.println("average : " + s.getAverage());

        // Empty stream: min and max are placeholders, not real values
        IntSummaryStatistics empty = IntStream.empty().summaryStatistics();
        System.out.println("empty min / max : " + empty.getMin() + " / " + empty.getMax());
    }
}