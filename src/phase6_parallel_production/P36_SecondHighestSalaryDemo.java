package phase7_interview_questions;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Find the 2nd (or Nth) highest salary using streams
 *   - distinct, sorted(Comparator.reverseOrder()), skip, findFirst
 *
 * PROBLEM:
 *   Sorting and picking index 1 gives the wrong answer when the top
 *   salary appears twice.
 *
 * SOLUTION:
 *   distinct() before sorted() and skip(n - 1).findFirst() returns the
 *   correct Nth highest value wrapped in an Optional.
 */
public class P36_SecondHighestSalaryDemo {

    record Employee(String name, int salary) {}

    static Optional<Integer> nthHighest(List<Integer> salaries, int n) {
        return salaries.stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .skip(n - 1)
                .findFirst();
    }

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Alice", 90000),
                new Employee("Bob", 70000),
                new Employee("Charlie", 90000),
                new Employee("David", 60000),
                new Employee("Eve", 70000));
        List<Integer> salaries = employees.stream().map(Employee::salary).toList();

        // 1. Broken version: sort and take index 1
        System.out.println("without distinct : " + salaries.stream()
                .sorted(Comparator.reverseOrder())
                .skip(1)
                .findFirst()
                .get());                                   // 90000, wrong

        // 2. Fixed version
        System.out.println("2nd highest      : " + nthHighest(salaries, 2).get());
        System.out.println("3rd highest      : " + nthHighest(salaries, 3).get());
        System.out.println("10th highest     : " + nthHighest(salaries, 10));    // Optional.empty

        int second = nthHighest(salaries, 2).orElseThrow();
        System.out.println("who earns it     : " + employees.stream()
                .filter(e -> e.salary() == second)
                .map(Employee::name)
                .toList());
    }
}