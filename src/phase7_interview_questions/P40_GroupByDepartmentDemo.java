package phase7_interview_questions;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Group employees by department: count, average salary, highest paid
 *   - groupingBy with downstream collectors, summarizingDouble
 *
 * PROBLEM:
 *   Doing each question with its own loop and its own map means several passes
 *   and a lot of bookkeeping code.
 *
 * SOLUTION:
 *   groupingBy with a downstream collector answers each question in one expression.
 */
public class P40_GroupByDepartmentDemo {

    record Employee(String name, String dept, double salary) {}

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Alice", "IT", 90000),
                new Employee("Bob", "HR", 50000),
                new Employee("Charlie", "IT", 70000),
                new Employee("David", "Finance", 60000),
                new Employee("Eve", "HR", 55000),
                new Employee("Frank", "Finance", 65000));

        // 1. Broken version: loop and manual maps
        Map<String, Integer> loopCount = new TreeMap<>();
        for (Employee e : employees) {
            loopCount.merge(e.dept(), 1, Integer::sum);
        }
        System.out.println("loop count : " + loopCount);

        // 2. Fixed version
        System.out.println("count      : " + employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.counting())));

        Map<String, Double> average = employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new, Collectors.averagingDouble(Employee::salary)));
        System.out.println("average    : " + average);

        System.out.println("highest    : " + employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingDouble(Employee::salary)),
                                o -> o.get().name()))));

        System.out.println("best average dept : " + average.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("none"));

        // Count, average and max salary in one collector
        employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                        Collectors.summarizingDouble(Employee::salary)))
                .forEach((dept, s) -> System.out.println(
                        dept + " : count=" + s.getCount() + ", avg=" + s.getAverage() + ", max=" + s.getMax()));
    }
}