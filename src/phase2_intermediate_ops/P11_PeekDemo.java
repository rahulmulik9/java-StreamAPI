package phase2_intermediate_ops;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - peek(Consumer): look at each element as it flows past, without changing the stream
 *   - Its one intended job: DEBUGGING a pipeline (see what is between two stages)
 *   - Why peek is NOT for real logic: it may run fewer times than you expect,
 *     in an unexpected order, or not at all
 *   - What to use instead: map (transform), forEach (act on the final result),
 *     collect (gather results)
 *
 * PROBLEM:
 *   peek looks like a handy place to add to a list, update a counter or change
 *   an object. But the JDK is allowed to skip it (count() on a sized source),
 *   short-circuiting stops it early (findFirst, limit), and in a parallel stream
 *   it runs on several threads in any order. Code that depends on peek works
 *   in a quick test and silently breaks later.
 *
 * SOLUTION:
 *   Use peek only to print or log while debugging, and remove it afterwards.
 *   Put real logic in map, filter, forEach or collect.
 */
public class P11_PeekDemo {

    // Mutable class, used to show the "change an object inside peek" trap
    static class Person {
        String name;

        Person(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    public static void main(String[] args) {

        List<String> names = List.of("alice", "bob", "charlie", "david");

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) peek without a terminal operation: nothing runs (see Class 3)
        System.out.println("--- 1a. peek without terminal operation ---");
        names.stream().peek(n -> System.out.println("  never printed: " + n));

        // b) count() can skip the whole pipeline when the size is known up front
        System.out.println("--- 1b. peek before count() ---");
        long count = names.stream()
                .peek(n -> System.out.println("  peek in count: " + n))
                .map(String::toUpperCase)
                .count();
        show("count", count);

        // c) peek used to collect data: short-circuiting makes the list incomplete
        System.out.println("--- 1c. peek used to fill a list ---");
        List<String> seen = new ArrayList<>();
        names.stream()
                .peek(seen::add)
                .filter(n -> n.length() > 3)
                .findFirst();
        show("seen list (expected all 4)", seen);

        // d) peek used to modify objects: works, but it hides a side effect on the SOURCE
        System.out.println("--- 1d. peek used to modify elements ---");
        List<Person> people = List.of(new Person("alice"), new Person("bob"));
        people.stream()
                .peek(p -> p.name = p.name.toUpperCase())
                .collect(Collectors.toList());
        show("source list was changed", people);

        // e) parallel stream: order and threads are not predictable
        //    names.parallelStream().peek(n -> list.add(n))...
        //    ArrayList is not thread-safe, so elements can be lost or an exception can be thrown.
        //    Even with a safe list, the ORDER of peek calls is not the order of the source.

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) Intended use: see what is flowing between stages
        System.out.println("--- 2a. peek for debugging ---");
        List<String> result = names.stream()
                .peek(n -> System.out.println("  before filter : " + n))
                .filter(n -> n.length() > 3)
                .peek(n -> System.out.println("  after filter  : " + n))
                .map(String::toUpperCase)
                .peek(n -> System.out.println("  after map     : " + n))
                .collect(Collectors.toList());
        show("result", result);

        // b) With a filter before count(), the pipeline cannot be skipped, so peek runs
        System.out.println("--- 2b. count() after filter ---");
        long filteredCount = names.stream()
                .filter(n -> n.length() > 3)
                .peek(n -> System.out.println("  peek in count: " + n))
                .count();
        show("count", filteredCount);

        // c) Gather data with collect, not with peek
        List<String> collected = names.stream()
                .filter(n -> n.length() > 3)
                .collect(Collectors.toList());
        show("collected", collected);

        // d) Act on every element with forEach (a terminal operation, so it always runs)
        System.out.print("forEach : ");
        names.stream().map(String::toUpperCase).forEach(n -> System.out.print(n + " "));
        System.out.println();

        // e) Transform with map: create NEW objects and leave the source alone
        List<Person> source = List.of(new Person("alice"), new Person("bob"));
        List<Person> upper = source.stream()
                .map(p -> new Person(p.name.toUpperCase()))
                .collect(Collectors.toList());
        show("new list", upper);
        show("source untouched", source);

        // f) peek after sorted: shows that sorted must see everything first
        System.out.println("--- 2f. peek around a stateful operation ---");
        names.stream()
                .peek(n -> System.out.println("  before sorted : " + n))
                .sorted((a, b) -> b.compareTo(a))
                .peek(n -> System.out.println("  after sorted  : " + n))
                .collect(Collectors.toList());

        // g) Temporary debugging helper: one line you can delete later
        List<String> debugged = names.stream()
                .filter(n -> n.contains("a"))
                .peek(n -> System.out.println("  DEBUG filtered: " + n))   // remove before committing
                .map(String::length)
                .map(String::valueOf)
                .collect(Collectors.toList());
        show("debugged", debugged);
    }
}