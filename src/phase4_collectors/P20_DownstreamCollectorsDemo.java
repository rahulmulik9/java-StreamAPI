package phase4_collectors;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - A downstream collector decides what happens to the elements INSIDE each group
 *   - counting, summingInt/Double, averagingDouble, minBy/maxBy
 *   - mapping, filtering, flatMapping
 *   - collectingAndThen, reducing
 *   - filter BEFORE groupingBy vs Collectors.filtering INSIDE it
 *
 * PROBLEM:
 *   groupingBy alone gives Map<K, List<T>>, so you then loop over each group to
 *   count, add or pick a name. That is a second pass with extra code. Also,
 *   filtering before groupingBy silently removes groups that end up empty, which
 *   changes the keys of the result.
 *
 * SOLUTION:
 *   Pass a downstream collector as the second argument of groupingBy. It builds
 *   the final value for each group in a single pass, and Collectors.filtering
 *   keeps empty groups when you need them.
 */
public class P20_DownstreamCollectorsDemo {

    record Employee(String name, String dept, double salary, List<String> skills) {}

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    public static void main(String[] args) {

        List<Employee> employees = List.of(
                new Employee("Alice", "IT", 90000, List.of("Java", "SQL")),
                new Employee("Bob", "HR", 50000, List.of("Excel")),
                new Employee("Charlie", "IT", 70000, List.of("Java", "Python")),
                new Employee("David", "Finance", 60000, List.of("Excel", "SQL")),
                new Employee("Eve", "HR", 55000, List.of("Excel", "Communication")),
                new Employee("Frank", "Finance", 65000, List.of("SQL"))
        );

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) Group first, then loop over the groups again to count: two steps
        Map<String, List<Employee>> groups = employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.toList()));
        Map<String, Integer> loopCounts = new TreeMap<>();
        for (Map.Entry<String, List<Employee>> entry : groups.entrySet()) {
            loopCounts.put(entry.getKey(), entry.getValue().size());
        }
        show("loop counts", loopCounts);

        // b) filter BEFORE groupingBy: the Finance group disappears completely
        show("filter before groupingBy", employees.stream()
                .filter(e -> e.salary() > 80000)
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.counting())));

        // c) maxBy returns Optional values, not employees
        show("maxBy (Optional values)", employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                        Collectors.maxBy(Comparator.comparingDouble(Employee::salary))))
                .get("IT"));

        // d) counting returns Long, so Map<String, Integer> does not compile
        //    Map<String, Integer> wrong = employees.stream()
        //            .collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) counting
        show("counting", employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.counting())));

        // b) summingDouble and averagingDouble
        show("summingDouble", employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.summingDouble(Employee::salary))));
        show("averagingDouble", employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.averagingDouble(Employee::salary))));

        // c) maxBy, and collectingAndThen to unwrap the Optional
        Map<String, String> topEarner = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::dept,
                        TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingDouble(Employee::salary)),
                                o -> o.map(Employee::name).orElse("-"))));
        show("highest paid per dept", topEarner);

        // d) mapping: collect only a field of each element
        show("mapping to names", employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                        Collectors.mapping(Employee::name, Collectors.toList()))));
        show("mapping + joining", employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                        Collectors.mapping(Employee::name, Collectors.joining(", ")))));

        // e) filtering INSIDE the group: Finance stays, with an empty list
        show("Collectors.filtering", employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                        Collectors.filtering(e -> e.salary() > 80000,
                                Collectors.mapping(Employee::name, Collectors.toList())))));

        // f) flatMapping: all distinct skills per department
        show("flatMapping skills", employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                        Collectors.flatMapping(e -> e.skills().stream(),
                                Collectors.toCollection(java.util.TreeSet::new)))));

        // g) collectingAndThen: finish with an extra step
        show("unmodifiable name lists", employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.mapping(Employee::name, Collectors.toList()),
                                Collections::unmodifiableList))));

        // h) reducing: total salary with a custom reduction
        show("reducing", employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                        Collectors.reducing(0.0, Employee::salary, Double::sum))));

        // i) Nested downstream: department, then band, then count
        Map<String, Map<String, Long>> nested = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::dept,
                        TreeMap::new,
                        Collectors.groupingBy(e -> e.salary() >= 60000 ? "60k+" : "below 60k",
                                TreeMap::new, Collectors.counting())));
        show("nested counting", nested);

        // j) Downstream collectors work without groupingBy too
        show("total salary", employees.stream().collect(Collectors.summingDouble(Employee::salary)));
        show("average salary", employees.stream().collect(Collectors.averagingDouble(Employee::salary)));
        show("count", employees.stream().collect(Collectors.counting()));
    }
}