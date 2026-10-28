package phase3_terminal_ops;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - anyMatch, allMatch, noneMatch: answer a yes/no question about the elements
 *   - findFirst, findAny: return ONE element wrapped in an Optional
 *   - All five are short-circuiting terminal operations: they stop as soon as
 *     the answer is known
 *   - Empty stream behavior: anyMatch = false, allMatch = true, noneMatch = true
 *   - findFirst vs findAny (they differ only on parallel streams)
 *
 * PROBLEM:
 *   Checking "is there any?" with filter(...).count() > 0 tests every element.
 *   A for-loop with a boolean flag is long and easy to get wrong. Calling
 *   findFirst().get() on an empty result throws NoSuchElementException, and
 *   allMatch on an empty stream returns true, which is correct logic but a
 *   surprise in real code ("all employees in an empty team are well paid").
 *
 * SOLUTION:
 *   Use the match methods for yes/no questions and the find methods for "give
 *   me one". Handle the Optional with orElse, ifPresent or orElseThrow, and
 *   guard against the empty case when "all" must mean "at least one and all".
 */
public class P13_MatchFindDemo {

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
                new Employee("Eve", "HR", 55000)
        );
        List<Employee> noEmployees = List.of();

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) For-loop with a boolean flag
        boolean found = false;
        for (Employee e : employees) {
            if (e.salary() > 80000) {
                found = true;
                break;
            }
        }
        show("loop with flag", found);

        // b) filter + count: tests EVERY element, even after the first match
        AtomicInteger countCalls = new AtomicInteger();
        boolean viaCount = employees.stream()
                .filter(e -> {
                    countCalls.incrementAndGet();
                    return e.salary() > 80000;
                })
                .count() > 0;
        show("filter + count > 0", viaCount);
        show("predicate calls (count)", countCalls.get());

        // c) findFirst().get() on an empty result
        try {
            employees.stream()
                    .filter(e -> e.salary() > 1_000_000)
                    .findFirst()
                    .get();
        } catch (java.util.NoSuchElementException ex) {
            show("findFirst().get() on empty", "NoSuchElementException");
        }

        // d) allMatch on an empty stream is TRUE (surprise)
        show("allMatch on empty list", noEmployees.stream().allMatch(e -> e.salary() > 1_000_000));

        // e) findFirst on a stream whose first element is null
        try {
            Arrays.asList(null, "a").stream().findFirst();
        } catch (NullPointerException ex) {
            show("findFirst with null element", "NullPointerException");
        }

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) The three match operations
        show("anyMatch  (someone > 80000)", employees.stream().anyMatch(e -> e.salary() > 80000));
        show("allMatch  (everyone > 40000)", employees.stream().allMatch(e -> e.salary() > 40000));
        show("noneMatch (nobody in Sales)", employees.stream().noneMatch(e -> e.dept().equals("Sales")));

        // b) Short-circuiting: the predicate stops running once the answer is known
        AtomicInteger anyCalls = new AtomicInteger();
        employees.stream().anyMatch(e -> {
            anyCalls.incrementAndGet();
            return e.salary() > 80000;                 // Alice is first, so 1 call
        });
        show("predicate calls (anyMatch)", anyCalls.get());

        AtomicInteger allCalls = new AtomicInteger();
        employees.stream().allMatch(e -> {
            allCalls.incrementAndGet();
            return e.salary() > 52000;                 // Bob (50000) fails on the 2nd element
        });
        show("predicate calls (allMatch)", allCalls.get());

        // c) Empty stream behavior
        show("empty anyMatch", noEmployees.stream().anyMatch(e -> true));
        show("empty allMatch", noEmployees.stream().allMatch(e -> false));
        show("empty noneMatch", noEmployees.stream().noneMatch(e -> true));

        // d) Guard for "all, and at least one"
        boolean allWellPaid = !noEmployees.isEmpty()
                && noEmployees.stream().allMatch(e -> e.salary() > 1_000_000);
        show("allMatch with empty guard", allWellPaid);

        // e) findFirst and findAny return an Optional
        Optional<Employee> firstIt = employees.stream()
                .filter(e -> e.dept().equals("IT"))
                .findFirst();
        show("findFirst (IT)", firstIt);

        Optional<Employee> anyHr = employees.stream()
                .filter(e -> e.dept().equals("HR"))
                .findAny();
        show("findAny (HR, sequential)", anyHr.map(Employee::name).orElse("none"));

        // f) Handling the Optional safely
        String name1 = employees.stream()
                .filter(e -> e.salary() > 1_000_000)
                .map(Employee::name)
                .findFirst()
                .orElse("nobody");
        show("orElse", name1);

        employees.stream()
                .filter(e -> e.dept().equals("Finance"))
                .findFirst()
                .ifPresent(e -> System.out.println("ifPresent : " + e.name()));

        try {
            employees.stream()
                    .filter(e -> e.dept().equals("Sales"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No Sales employee"));
        } catch (IllegalStateException ex) {
            show("orElseThrow", ex.getMessage());
        }

        // g) "First" depends on ORDER: sort first when you mean "highest" or "lowest"
        Optional<Employee> topEarner = employees.stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .findFirst();
        show("top earner (sorted + findFirst)", topEarner.map(Employee::name).orElse("none"));

        // h) Null elements: remove them before findFirst
        Optional<String> firstNonNull = Arrays.asList(null, "a", "b").stream()
                .filter(Objects::nonNull)
                .findFirst();
        show("findFirst after nonNull filter", firstNonNull);

        // i) The relationship between the three matches
        boolean p1 = employees.stream().noneMatch(e -> e.salary() > 80000);
        boolean p2 = !employees.stream().anyMatch(e -> e.salary() > 80000);
        boolean p3 = employees.stream().allMatch(e -> e.salary() <= 80000);
        show("noneMatch == !anyMatch", p1 == p2);
        show("noneMatch == allMatch(not p)", p1 == p3);

        // j) Short-circuiting makes infinite streams safe (when the answer exists)
        show("infinite anyMatch", Stream.iterate(1, n -> n + 1).anyMatch(n -> n * n > 200));
        show("infinite allMatch (fails early)", Stream.iterate(1, n -> n + 1).allMatch(n -> n < 5));
        // Stream.iterate(1, n -> n + 1).allMatch(n -> n > 0);   // never ends: every element passes

        // k) findFirst vs findAny on a parallel stream
        List<Integer> numbers = Stream.iterate(1, n -> n + 1).limit(1000).collect(Collectors.toList());
        show("parallel findFirst", numbers.parallelStream().filter(n -> n % 7 == 0).findFirst().get());
        show("parallel findAny (may vary)", numbers.parallelStream().filter(n -> n % 7 == 0).findAny().get());
    }
}