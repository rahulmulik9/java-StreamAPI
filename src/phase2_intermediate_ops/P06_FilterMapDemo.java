package phase2_intermediate_ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - filter(Predicate): keep only the elements that match (same type, fewer elements)
 *   - map(Function): transform every element (same count, possibly a new type)
 *   - Chaining filter and map, and why the ORDER matters
 *   - Records (Java 16+) as immutable data, so "changing" means creating a new object
 *
 * PROBLEM:
 *   Filtering and transforming in a loop mixes both jobs together. With streams
 *   the usual mistakes are: using map when you meant filter (you get a list of
 *   booleans), calling map on a null element (NullPointerException), and putting
 *   an expensive map BEFORE the filter so it runs on elements you throw away.
 *
 * SOLUTION:
 *   filter decides WHICH elements stay, map decides WHAT they become.
 *   Filter nulls first, filter early, and map only what is left.
 */
public class P06_FilterMapDemo {

    // Immutable data carrier (Java 16+). Gives constructor, getters like name(),
    // equals, hashCode and toString automatically.
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

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) For-loop: filtering and transforming tangled together
        List<String> loopResult = new ArrayList<>();
        for (Employee e : employees) {
            if (e.salary() > 55000) {
                loopResult.add(e.name().toUpperCase());
            }
        }
        show("For-loop result", loopResult);

        // b) Mistake: map used where filter was needed -> List<Boolean>, not the employees
        List<Boolean> wrong = employees.stream()
                .map(e -> e.salary() > 55000)
                .collect(Collectors.toList());
        show("map used as filter", wrong);

        // c) Mistake: map on a list that contains null -> NullPointerException
        List<String> withNulls = Arrays.asList("alice", null, "bob", null, "charlie");
        try {
            withNulls.stream().map(String::toUpperCase).collect(Collectors.toList());
        } catch (NullPointerException ex) {
            show("null in list", "NullPointerException");
        }

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) filter + map: which elements, then what they become
        List<String> names = employees.stream()
                .filter(e -> e.salary() > 55000)      // Predicate<Employee>: keeps 3 of 5
                .map(Employee::name)                  // Function<Employee, String>: Employee -> String
                .map(String::toUpperCase)             // Function<String, String>
                .collect(Collectors.toList());
        show("filter + map", names);

        // b) Several filters chained = one filter with &&
        List<String> chained = employees.stream()
                .filter(e -> e.dept().equals("IT"))
                .filter(e -> e.salary() > 60000)
                .map(Employee::name)
                .collect(Collectors.toList());
        List<String> combined = employees.stream()
                .filter(e -> e.dept().equals("IT") && e.salary() > 60000)
                .map(Employee::name)
                .collect(Collectors.toList());
        show("chained filters", chained);
        show("single filter with &&", combined);

        // c) map can change the type: Employee -> String -> Integer
        List<Integer> lengths = employees.stream()
                .map(Employee::name)
                .map(String::length)
                .collect(Collectors.toList());
        show("map to another type", lengths);

        // d) map can build NEW objects (records are immutable, so we never modify the old one)
        List<Employee> raised = employees.stream()
                .filter(e -> e.dept().equals("HR"))
                .map(e -> new Employee(e.name(), e.dept(), e.salary() + 5000))
                .collect(Collectors.toList());
        show("map to new objects", raised);
        show("original Bob salary", employees.get(1).salary());   // unchanged

        // e) Null-safe: remove nulls BEFORE map
        List<String> safe = withNulls.stream()
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        show("null-safe", safe);

        // f) Order matters: filter first means map runs on fewer elements
        AtomicInteger filterFirstCalls = new AtomicInteger();
        employees.stream()
                .filter(e -> e.salary() > 55000)
                .map(e -> {
                    filterFirstCalls.incrementAndGet();
                    return new Employee(e.name().toUpperCase(), e.dept(), e.salary());
                })
                .collect(Collectors.toList());
        show("map calls (filter first)", filterFirstCalls.get());

        AtomicInteger mapFirstCalls = new AtomicInteger();
        employees.stream()
                .map(e -> {
                    mapFirstCalls.incrementAndGet();
                    return new Employee(e.name().toUpperCase(), e.dept(), e.salary());
                })
                .filter(e -> e.salary() > 55000)
                .collect(Collectors.toList());
        show("map calls (map first)", mapFirstCalls.get());

        // ------------------------------------------------------------
        // 3. Output explanation
        // ------------------------------------------------------------
        /*
         * Expected output:
         *   For-loop result : [ALICE, CHARLIE, DAVID]
         *   map used as filter : [true, false, true, true, false]
         *   null in list : NullPointerException
         *   filter + map : [ALICE, CHARLIE, DAVID]
         *   chained filters : [Alice, Charlie]
         *   single filter with && : [Alice, Charlie]
         *   map to another type : [5, 3, 7, 5, 3]
         *   map to new objects : [Employee[name=Bob, dept=HR, salary=55000.0], Employee[name=Eve, dept=HR, salary=60000.0]]
         *   original Bob salary : 50000.0
         *   null-safe : [ALICE, BOB, CHARLIE]
         *   map calls (filter first) : 3
         *   map calls (map first) : 5
         *
         * - filter keeps the SAME type and may return fewer elements.
         *   map returns the SAME number of elements and may change the type.
         *   Bob (50000) and Eve (55000) fail "salary > 55000", so only
         *   Alice, Charlie and David pass.
         *
         * - "map used as filter": map(e -> e.salary() > 55000) transforms every
         *   Employee into a boolean. Nothing is removed, you just get 5 booleans.
         *   If you want to drop elements, use filter.
         *
         * - Null in a list: String::toUpperCase is called on null, which throws
         *   NPE. filter(Objects::nonNull) removes the nulls before map sees them.
         *   (Stream.of(...) and List.of(...) do not allow null at all, which is
         *   why this example uses Arrays.asList.)
         *
         * - Chained filters and one filter with && give the same result. Chained
         *   filters read better when each condition is a separate idea. Use &&
         *   when the conditions belong together.
         *
         * - map to new objects: Employee is a record, so it is immutable. We do
         *   not change Bob, we create a new Employee with a higher salary. The
         *   original list still has Bob at 50000.0.
         *
         * - Order: filter first runs the map lambda 3 times (only for survivors).
         *   map first runs it 5 times, then filters. Same final result, more
         *   work. In real code the map may be a database call or a heavy
         *   calculation, so filter as early as you can.
         *   (The counter is used only to demonstrate. Do not count with side
         *   effects in real pipelines, see Class 31.)
         *
         * - map(Employee::salary) would give Stream<Double> with boxing.
         *   mapToDouble avoids it, which we cover in Phase 5.
         */
    }
}