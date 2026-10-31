package phase4_collectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Collectors.groupingBy(classifier)  -> Map<K, List<T>>
 *   - The classifier can be any function: a field, a computed band, a first letter
 *   - Choosing the Map type: groupingBy(classifier, TreeMap::new, Collectors.toList())
 *   - Null keys are not allowed
 *   - Multi-level grouping (a map inside a map)
 *
 * PROBLEM:
 *   Grouping with a loop means checking containsKey, creating a list, then adding.
 *   That is 5 lines for one idea, and it is easy to forget the "create the list"
 *   step. A null group key throws NullPointerException, and the default HashMap
 *   does not keep any useful order.
 *
 * SOLUTION:
 *   groupingBy states the idea in one line. Use a TreeMap or LinkedHashMap
 *   supplier when order matters, and map null to a default key such as "UNKNOWN".
 */
public class P19_GroupingByDemo {

    record Employee(String name, String dept, double salary) {}

    static String band(double salary) {
        if (salary >= 80000) return "HIGH";
        if (salary >= 60000) return "MID";
        return "LOW";
    }

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    private static String names(List<Employee> list) {
        return list.stream().map(Employee::name).collect(Collectors.joining(", "));
    }

    public static void main(String[] args) {

        List<Employee> employees = List.of(
                new Employee("Alice", "IT", 90000),
                new Employee("Bob", "HR", 50000),
                new Employee("Charlie", "IT", 70000),
                new Employee("David", "Finance", 60000),
                new Employee("Eve", "HR", 55000),
                new Employee("Frank", "Finance", 65000)
        );
        List<String> words = List.of("apple", "avocado", "banana", "blueberry", "cherry", "fig");

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) Loop with containsKey
        Map<String, List<Employee>> loopGroups = new HashMap<>();
        for (Employee e : employees) {
            if (!loopGroups.containsKey(e.dept())) {
                loopGroups.put(e.dept(), new ArrayList<>());
            }
            loopGroups.get(e.dept()).add(e);
        }
        show("loop groups", new TreeMap<>(loopGroups).keySet());

        // b) Better, but still manual: computeIfAbsent
        Map<String, List<Employee>> loopGroups2 = new HashMap<>();
        for (Employee e : employees) {
            loopGroups2.computeIfAbsent(e.dept(), k -> new ArrayList<>()).add(e);
        }
        show("computeIfAbsent groups", new TreeMap<>(loopGroups2).keySet());

        // c) Null key
        List<Employee> withNullDept = List.of(new Employee("Zed", null, 40000));
        try {
            withNullDept.stream().collect(Collectors.groupingBy(Employee::dept));
        } catch (NullPointerException e) {
            show("null key", "NullPointerException");
        }

        // d) The default Map is a HashMap: do not expect a useful order
        show("default key order (not guaranteed)", employees.stream()
                .collect(Collectors.groupingBy(Employee::dept)).keySet());

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) Group by a field
        Map<String, List<Employee>> byDept = employees.stream()
                .collect(Collectors.groupingBy(Employee::dept));
        System.out.println("By department:");
        new TreeMap<>(byDept).forEach((dept, list) -> System.out.println("  " + dept + " -> " + names(list)));

        // b) Sorted keys with TreeMap
        Map<String, List<Employee>> sortedKeys = employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.toList()));
        show("TreeMap keys", sortedKeys.keySet());

        // c) Keys in encounter order with LinkedHashMap
        Map<String, List<Employee>> encounterOrder = employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, LinkedHashMap::new, Collectors.toList()));
        show("LinkedHashMap keys", encounterOrder.keySet());

        // d) Computed keys: salary band
        Map<String, List<Employee>> byBand = employees.stream()
                .collect(Collectors.groupingBy(e -> band(e.salary()), TreeMap::new, Collectors.toList()));
        System.out.println("By salary band:");
        byBand.forEach((b, list) -> System.out.println("  " + b + " -> " + names(list)));

        // e) Other classifiers: first letter, length
        show("by first letter", words.stream()
                .collect(Collectors.groupingBy(w -> w.charAt(0), TreeMap::new, Collectors.toList())));
        show("by length", words.stream()
                .collect(Collectors.groupingBy(String::length, TreeMap::new, Collectors.toList())));

        // f) Null-safe key
        Map<String, List<Employee>> nullSafe = withNullDept.stream()
                .collect(Collectors.groupingBy(e -> e.dept() == null ? "UNKNOWN" : e.dept()));
        show("null-safe key", nullSafe.keySet());

        // g) Two levels: department, then band
        Map<String, Map<String, List<Employee>>> twoLevels = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::dept,
                        TreeMap::new,
                        Collectors.groupingBy(e -> band(e.salary()), TreeMap::new, Collectors.toList())));
        System.out.println("Department, then band:");
        twoLevels.forEach((dept, bands) -> {
            System.out.println("  " + dept);
            bands.forEach((b, list) -> System.out.println("    " + b + " -> " + names(list)));
        });

        // h) Group sizes (counting, summing and mapping come in Class 20)
        employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.toList()))
                .forEach((dept, list) -> System.out.println("  " + dept + " has " + list.size() + " employees"));
    }
}