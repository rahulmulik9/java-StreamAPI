package phase5_primitive_special;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - IntStream, LongStream, DoubleStream: streams of primitives (no boxing)
 *   - range / rangeClosed, mapToInt, boxed, mapToObj
 *   - sum(), average(), max() available directly
 *
 * PROBLEM:
 *   Stream<Integer> has no sum() and boxes every number into an Integer object.
 *   Also, "abc".chars() is an IntStream of numbers, so printing it shows 97, 98, 99.
 *
 * SOLUTION:
 *   Convert with mapToInt when you need numeric operations, and back with
 *   boxed() or mapToObj() when you need objects.
 */
public class P25_PrimitiveStreamDemo {
    public static void main(String[] args) {
        List<Integer> numbers = List.of(3, 5, 8);

        // 1. Broken version
        // numbers.stream().sum();                                    // compile error
        int boxedSum = numbers.stream().reduce(0, Integer::sum);      // works, but boxes every step
        System.out.println("reduce sum : " + boxedSum);
        System.out.println("chars      : " + "abc".chars().boxed().collect(Collectors.toList()));

        // 2. Fixed version
        System.out.println("mapToInt sum : " + numbers.stream().mapToInt(Integer::intValue).sum());
        System.out.println("range        : " + IntStream.rangeClosed(1, 5).boxed().collect(Collectors.toList()));
        System.out.println("average      : " + IntStream.of(3, 5, 8).average().getAsDouble());
        System.out.println("mapToObj     : " + IntStream.range(0, 3).mapToObj(i -> "item" + i).toList());
        System.out.println("chars fixed  : " + "abc".chars().mapToObj(c -> (char) c).toList());
    }
}