package phase5_primitive_special;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Stream.concat(a, b): join two streams into one
 *   - Stream.Builder: add elements one by one, then build the stream
 *
 * PROBLEM:
 *   Joining two sources through a temporary list is an extra step. And a Builder
 *   cannot be used again after build(): add() then throws IllegalStateException.
 *
 * SOLUTION:
 *   concat joins streams directly (flatMap for more than two), and a Builder is built exactly once.
 */
public class P28_ConcatBuilderDemo {
    public static void main(String[] args) {

        // 1. Broken version
        List<String> temp = new ArrayList<>(List.of("a", "b"));
        temp.addAll(List.of("c", "d"));
        System.out.println("temp list : " + temp.stream().toList());

        Stream.Builder<String> used = Stream.builder();
        used.add("x");
        used.build();
        try {
            used.add("y");
        } catch (IllegalStateException e) {
            System.out.println("add after build : IllegalStateException");
        }

        // 2. Fixed version
        System.out.println("concat  : " + Stream.concat(Stream.of("a", "b"), Stream.of("c", "d")).toList());

        System.out.println("three   : " + Stream.of(Stream.of(1, 2), Stream.of(3), Stream.of(4, 5))
                .flatMap(Function.identity())
                .toList());

        Stream.Builder<String> builder = Stream.builder();
        builder.add("x").add("y");
        if (temp.size() > 3) {
            builder.add("z");
        }
        System.out.println("builder : " + builder.build().toList());
    }
}