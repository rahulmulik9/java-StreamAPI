package phase3_terminal_ops;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - reduce(accumulator)                      -> Optional<T>   (no identity, may be empty)
 *   - reduce(identity, accumulator)            -> T             (empty stream gives identity)
 *   - reduce(identity, accumulator, combiner)  -> U             (result type differs from element type)
 *   - Rules: identity must be neutral, the operation must be associative
 *   - reduce (combine into ONE value) vs collect (build a mutable container)
 *
 * PROBLEM:
 *   A wrong identity (10 for a sum) gives a wrong answer, and in a parallel stream
 *   the identity is added once per chunk. A non-associative operation such as
 *   subtraction gives a different result on parallel streams. Using reduce to
 *   fill an ArrayList mutates the shared identity object and breaks in parallel.
 *
 * SOLUTION:
 *   Use a neutral identity (0 for sum, 1 for product), an associative operation,
 *   and use collect (not reduce) for mutable results.
 */
public class P15_ReduceDemo {

    record Employee(String name, double salary) {}

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    public static void main(String[] args) {

        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<String> words = List.of("java", "is", "fun");
        List<Employee> employees = List.of(
                new Employee("Alice", 90000),
                new Employee("Bob", 50000),
                new Employee("Charlie", 70000)
        );
        List<Integer> big = IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) Loop with a mutable accumulator
        int loopSum = 0;
        for (int n : numbers) {
            loopSum += n;
        }
        show("loop sum", loopSum);

        // b) Compile error: a lambda cannot change a local variable
        //    int total = 0;
        //    numbers.stream().forEach(n -> total += n);

        // c) Wrong identity: 10 is not neutral for a sum
        show("sequential, identity 10", numbers.stream().reduce(10, Integer::sum));          // 65, not 55
        show("parallel, identity 10 (usually not 5060)", big.parallelStream().reduce(10, Integer::sum));

        // d) Non-associative operation: subtraction
        show("sequential subtraction", numbers.stream().reduce((a, b) -> a - b).get());      // -53
        show("parallel subtraction (usually different)", numbers.parallelStream().reduce((a, b) -> a - b).get());

        // e) Without identity, an empty stream gives an empty Optional
        try {
            List.<Integer>of().stream().reduce(Integer::sum).get();
        } catch (java.util.NoSuchElementException e) {
            show("reduce(op).get() on empty", "NoSuchElementException");
        }

        // f) reduce used to build a mutable list: WRONG (do not run in parallel)
        //    numbers.parallelStream().reduce(new ArrayList<>(),
        //            (list, n) -> { list.add(n); return list; },
        //            (l1, l2) -> { l1.addAll(l2); return l1; });
        //    Every thread shares the same identity list, so elements are lost or an
        //    exception is thrown. The ArrayList is not thread-safe.

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) Sum, product, max, min
        show("sum (identity 0)", numbers.stream().reduce(0, Integer::sum));
        show("product (identity 1)", numbers.stream().reduce(1, (a, b) -> a * b));
        show("max (no identity)", numbers.stream().reduce(Integer::max));
        show("min (no identity)", numbers.stream().reduce(Integer::min));

        // b) Empty stream: identity version vs Optional version
        show("empty sum (identity)", List.<Integer>of().stream().reduce(0, Integer::sum));
        show("empty sum (Optional)", List.<Integer>of().stream().reduce(Integer::sum));

        // c) Three-argument form: element type String, result type Integer
        int totalLetters = words.stream()
                .reduce(0,
                        (acc, w) -> acc + w.length(),    // accumulator: (Integer, String) -> Integer
                        Integer::sum);                   // combiner: merges two partial results
        show("total letters", totalLetters);

        // d) Reduce objects: total salary
        show("total salary (reduce)", employees.stream().map(Employee::salary).reduce(0.0, Double::sum));
        show("total salary (mapToDouble)", employees.stream().mapToDouble(Employee::salary).sum());

        // e) Longest word
        Optional<String> longest = words.stream().reduce((a, b) -> a.length() >= b.length() ? a : b);
        show("longest word", longest.orElse("none"));

        // f) Factorial with a long stream
        show("10!", LongStream.rangeClosed(1, 10).reduce(1, (a, b) -> a * b));

        // g) String concatenation: reduce works, joining is better
        show("reduce concat", words.stream().reduce("", (a, b) -> a + "," + b));   // leading comma
        show("joining", words.stream().collect(Collectors.joining(",")));

        // h) Mutable result: use collect, not reduce
        List<Integer> collected = numbers.stream()
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        show("collect into ArrayList", collected);

        // i) Correct parallel reduce: neutral identity, associative operation
        show("parallel sum", big.parallelStream().reduce(0, Integer::sum));
        show("parallel max", big.parallelStream().reduce(Integer::max));

        // j) Prefer the built-in when one exists
        show("built-in sum", numbers.stream().mapToInt(Integer::intValue).sum());
    }
}