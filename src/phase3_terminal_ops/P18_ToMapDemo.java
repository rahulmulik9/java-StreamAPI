package phase4_collectors;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Collectors.toMap(keyMapper, valueMapper)
 *   - Duplicate keys throw IllegalStateException
 *   - The merge function: toMap(key, value, (old, new) -> ...)
 *   - The map supplier: toMap(key, value, merge, LinkedHashMap::new)
 *   - Null values throw NullPointerException
 *   - Function.identity(), toUnmodifiableMap, reversing a map
 *
 * PROBLEM:
 *   A for-loop with put() silently overwrites a duplicate key, so data is lost
 *   without any warning. The two-argument toMap does the opposite and throws.
 *   A null value throws NullPointerException, and the default result is a
 *   HashMap, so the encounter order is lost.
 *
 * SOLUTION:
 *   Decide what a duplicate means (keep first, keep last, add, join, pick the
 *   max) and write it as the merge function. Choose the Map type with the
 *   supplier, and never produce a null value.
 */
public class P18_ToMapDemo {

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
        List<String> words = List.of("a", "b", "a", "c", "b", "a");

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) Loop with put(): duplicates are overwritten silently
        Map<String, String> loopMap = new HashMap<>();
        for (Employee e : employees) {
            loopMap.put(e.dept(), e.name());
        }
        show("loop put (last wins, silently)", new TreeMap<>(loopMap));

        // b) toMap with a duplicate key
        try {
            employees.stream().collect(Collectors.toMap(Employee::dept, Employee::name));
        } catch (IllegalStateException e) {
            show("duplicate key", e.getMessage());
        }

        // c) toMap with a null value
        try {
            Stream.of("a", "bb", "").collect(Collectors.toMap(s -> s, s -> s.isEmpty() ? null : s.length()));
        } catch (NullPointerException e) {
            show("null value", "NullPointerException");
        }

        // d) Default result is a HashMap: the order is not the encounter order
        show("default toMap order (not guaranteed)", employees.stream()
                .collect(Collectors.toMap(Employee::name, Employee::salary)));

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) Unique keys
        show("name -> salary", new TreeMap<>(employees.stream()
                .collect(Collectors.toMap(Employee::name, Employee::salary))));

        // b) Function.identity(): the element itself is the key
        show("word -> length", new TreeMap<>(Stream.of("java", "is", "fun")
                .collect(Collectors.toMap(Function.identity(), String::length))));

        // c) Merge function: count words
        show("word count", words.stream()
                .collect(Collectors.toMap(w -> w, w -> 1, Integer::sum, TreeMap::new)));

        // d) Merge function: keep the first or the last
        show("keep first", employees.stream()
                .collect(Collectors.toMap(Employee::dept, Employee::name, (first, second) -> first, TreeMap::new)));
        show("keep last", employees.stream()
                .collect(Collectors.toMap(Employee::dept, Employee::name, (first, second) -> second, TreeMap::new)));

        // e) Merge function: join the values
        show("join names", employees.stream()
                .collect(Collectors.toMap(Employee::dept, Employee::name, (a, b) -> a + ", " + b, TreeMap::new)));

        // f) Merge function: highest paid per department
        Map<String, Employee> topPerDept = employees.stream()
                .collect(Collectors.toMap(
                        Employee::dept,
                        e -> e,
                        BinaryOperator.maxBy(Comparator.comparingDouble(Employee::salary)),
                        TreeMap::new));
        topPerDept.forEach((dept, e) -> System.out.println("  " + dept + " -> " + e.name()));

        // g) Map supplier: keep the order after sorting
        Map<String, Double> bySalaryDesc = employees.stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .collect(Collectors.toMap(Employee::name, Employee::salary, (a, b) -> a, LinkedHashMap::new));
        show("sorted by salary desc", bySalaryDesc);

        // h) Null value: replace it before it reaches the map
        show("null-safe value", new TreeMap<>(Stream.of("a", "bb", "")
                .collect(Collectors.toMap(s -> s, s -> s.isEmpty() ? 0 : s.length()))));

        // i) Unmodifiable result
        Map<String, Integer> fixed = Stream.of("x", "yy")
                .collect(Collectors.toUnmodifiableMap(s -> s, String::length));
        try {
            fixed.put("zzz", 3);
        } catch (UnsupportedOperationException e) {
            show("toUnmodifiableMap put", "UnsupportedOperationException");
        }

        // j) Two lists into one map
        List<String> keys = List.of("one", "two", "three");
        List<Integer> values = List.of(1, 2, 3);
        show("two lists to map", IntStream.range(0, keys.size()).boxed()
                .collect(Collectors.toMap(keys::get, values::get, (a, b) -> a, LinkedHashMap::new)));

        // k) Reverse a map (the values must be unique)
        Map<String, Integer> original = Map.of("a", 1, "b", 2);
        show("reversed", new TreeMap<>(original.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey))));
    }
}