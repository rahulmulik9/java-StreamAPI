package phase6_parallel_production;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Habits that keep stream code readable and correct
 *   - When a plain loop is the better choice
 *
 * PROBLEM:
 *   Streams make it easy to write code that is short but hard to read: side effects
 *   in forEach, one giant line, isPresent + get, and filters in the wrong place.
 *
 * SOLUTION:
 *   Collect results instead of adding to lists, name your predicates, one step
 *   per line, use Optional properly, and filter before sorting.
 */
public class P35_BestPracticesDemo {

    record Employee(String name, String dept, double salary, boolean active) {}

    static boolean isActiveIt(Employee e) {
        return e.active() && e.dept().equals("IT");
    }

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Alice", "IT", 90000, true),
                new Employee("Bob", "HR", 50000, true),
                new Employee("Charlie", "IT", 70000, false),
                new Employee("David", "Finance", 85000, true));

        // 1. Broken version
        List<String> names1 = new ArrayList<>();
        employees.stream().filter(Employee::active).forEach(e -> names1.add(e.name()));   // side effect

        List<String> messy = employees.stream().filter(e -> e.active() && e.dept().equals("IT") || e.salary() > 80000).map(e -> e.name().toUpperCase()).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        Optional<Employee> found = employees.stream().filter(e -> e.salary() > 80000).findFirst();
        String who = found.isPresent() ? found.get().name() : "nobody";

        double total1 = employees.stream().map(Employee::salary).reduce(0.0, Double::sum);

        // 2. Fixed version
        // a) collect, do not add to an outside list
        List<String> names2 = employees.stream().filter(Employee::active).map(Employee::name).toList();

        // b) named predicate, one step per line
        List<String> itNames = employees.stream()
                .filter(P35_BestPracticesDemo::isActiveIt)
                .map(Employee::name)
                .sorted()
                .toList();

        // c) Optional: map + orElse
        String who2 = employees.stream()
                .filter(e -> e.salary() > 80000)
                .findFirst()
                .map(Employee::name)
                .orElse("nobody");

        // d) primitive stream for numbers
        double total2 = employees.stream().mapToDouble(Employee::salary).sum();

        // e) filter BEFORE sorted: sort fewer elements
        List<String> topActive = employees.stream()
                .filter(Employee::active)
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .map(Employee::name)
                .toList();

        System.out.println("names   : " + names2);
        System.out.println("IT      : " + itNames);
        System.out.println("who     : " + who2);
        System.out.println("total   : " + total2);
        System.out.println("top     : " + topActive);

        // f) A loop is better here: early exit with a side effect
        for (Employee e : employees) {
            if (e.salary() > 80000) {
                System.out.println("first high earner : " + e.name());
                break;
            }
        }
    }
}