package phase3_terminal_ops;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - count(): number of elements, returns a long
 *   - min(Comparator) and max(Comparator): return an Optional
 *   - Which element wins on a tie
 *   - Primitive streams: IntStream.max() needs no Comparator and returns OptionalInt
 *   - count() may skip the pipeline completely (Java 9+) when the size is known
 *
 * PROBLEM:
 *   A loop that starts from Integer.MIN_VALUE returns a fake maximum for an empty
 *   list. Stream.max() has no no-argument version for objects, count() returns
 *   long (not int), and max().get() on an empty stream throws an exception.
 *   Also, a map or peek before count() may never run.
 *
 * SOLUTION:
 *   Pass a Comparator to min/max, handle the Optional (orElse, map, ifPresent), and
 *   do not hide logic with side effects in a pipeline that ends with count().
 */
public class P14_CountMinMaxDemo {

    record Employee(String name, String dept, double salary) {}

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    public static void main(String[] args) {

        List<Employee> employees = List.of(
                new Employee("Alice", "IT", 90000),
                new Employee("Bob", "HR", 50000),
                new Employee("Charlie", "IT", 70000),
                new Employee("David", "Finance", 60000),
                new Employee("Eve", "HR", 55000),
                new Employee("Frank", "Finance", 90000)     // same salary as Alice
        );
        List<Employee> noEmployees = List.of();
        List<Integer> numbers = List.of(4, 9, 2, 7);
        List<String> words = List.of("stream", "map", "collector", "filter");

        // a) count and count with filter
        show("count all", employees.stream().count());
        show("count IT", employees.stream().filter(e -> e.dept().equals("IT")).count());

        // b) min and max with a Comparator
        Comparator<Employee> bySalary = Comparator.comparingDouble(Employee::salary);
        show("max salary", employees.stream().max(bySalary).map(Employee::name).orElse("none"));
        show("min salary", employees.stream().min(bySalary).map(Employee::name).orElse("none"));

        // c) Tie: Alice and Frank both earn 90000, the first one wins
        show("tie on max (first wins)", employees.stream().max(bySalary).map(Employee::name).orElse("none"));

        // d) Break the tie with thenComparing
        show("max with tie-breaker", employees.stream()
                .max(bySalary.thenComparing(Employee::name))
                .map(Employee::name)
                .orElse("none"));

        // e) Safe handling of the Optional
        show("max on empty (orElse)", noEmployees.stream().max(bySalary).map(Employee::name).orElse("nobody"));
        employees.stream().min(bySalary)
                .ifPresent(e -> System.out.println("ifPresent : lowest paid is " + e.name()));

        // f) Natural order and method references
        show("max number", numbers.stream().max(Comparator.naturalOrder()).get());
        show("min number", numbers.stream().min(Integer::compare).get());

        // g) Strings: longest and shortest word
        show("longest word", words.stream().max(Comparator.comparingInt(String::length)).get());
        show("shortest word", words.stream().min(Comparator.comparingInt(String::length)).get());

        // h) Primitive streams: no Comparator, returns OptionalInt
        OptionalInt intMax = numbers.stream().mapToInt(Integer::intValue).max();
        show("IntStream max", intMax.getAsInt());
        show("IntStream max on empty", IntStream.of().max().orElse(-1));

        // i) Filter first, then min or max
        show("lowest paid in IT", employees.stream()
                .filter(e -> e.dept().equals("IT"))
                .min(bySalary)
                .map(Employee::name)
                .orElse("none"));

        // j) Distinct count
        show("distinct count", Stream.of("a", "b", "a", "c", "b").distinct().count());

        // k) count returns long, so large streams are safe
        long multiples = IntStream.rangeClosed(1, 1_000_000).filter(n -> n % 3 == 0).count();
        show("multiples of 3 up to 1,000,000", multiples);

        // l) Filter before count: now the pipeline cannot be skipped
        AtomicInteger filterCalls = new AtomicInteger();
        employees.stream()
                .filter(e -> {
                    filterCalls.incrementAndGet();
                    return e.salary() > 55000;
                })
                .count();
        show("filter calls before count", filterCalls.get());
    }
}