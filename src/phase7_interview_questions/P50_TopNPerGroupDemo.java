package phase7_interview_questions;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Top 3 highest paid employees per department
 *   - groupingBy + collectingAndThen (sort and limit inside each group)
 *
 * PROBLEM:
 *   sorted(...).limit(3) on the whole list gives the top 3 OVERALL, not the top 3
 *   of each department.
 *
 * SOLUTION:
 *   Group first, then sort and limit inside each group with a downstream collector.
 */
public class P50_TopNPerGroupDemo {

    record Employee(String name, String dept, int salary) {}

    static <T> Collector<T, ?, List<T>> topN(int n, Comparator<T> comparator) {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> list.stream().sorted(comparator).limit(n).toList());
    }

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Alice", "IT", 90000),
                new Employee("Grace", "IT", 80000),
                new Employee("Charlie", "IT", 70000),
                new Employee("Henry", "IT", 65000),
                new Employee("Bob", "HR", 50000),
                new Employee("Eve", "HR", 55000),
                new Employee("Ivy", "Finance", 75000),
                new Employee("Frank", "Finance", 65000),
                new Employee("David", "Finance", 60000),
                new Employee("Jack", "Finance", 58000));

        Comparator<Employee> bySalaryDesc = Comparator.comparingInt(Employee::salary).reversed();

        // 1. Broken version: top 3 overall
        System.out.println("top 3 overall : " + employees.stream()
                .sorted(bySalaryDesc)
                .limit(3)
                .map(Employee::name)
                .toList());

        // 2. Fixed version
        Map<String, List<String>> top3 = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::dept,
                        TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(bySalaryDesc)
                                        .limit(3)
                                        .map(Employee::name)
                                        .toList())));
        top3.forEach((dept, names) -> System.out.println(dept + " : " + names));

        // Reusable version with the helper collector
        System.out.println("top 2 per dept (helper) : " + employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, topN(2, bySalaryDesc)))
                .entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().stream().map(Employee::name).toList())
                .toList());
    }
}