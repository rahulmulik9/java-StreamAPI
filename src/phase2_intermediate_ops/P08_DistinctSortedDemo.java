package phase2_intermediate_ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - distinct(): removes duplicates using equals() and hashCode()
 *   - sorted(): natural order (Comparable) or a custom Comparator
 *   - Comparator chains: comparing, thenComparing, reversed, nullsFirst
 *   - sorted() is stable, distinct() keeps the first occurrence
 *
 * PROBLEM:
 *   distinct() on objects without equals/hashCode removes nothing, because
 *   it compares references. sorted() on objects that are not Comparable
 *   throws ClassCastException. Hand-written comparators like (a, b) -> a - b
 *   can overflow, and natural String order puts "Banana" before "apple".
 *
 * SOLUTION:
 *   Use a record (or implement equals/hashCode) for distinct. Give sorted() a
 *   Comparator built with Comparator.comparing(...).thenComparing(...), and use
 *   Integer.compare instead of subtraction.
 */
public class P08_DistinctSortedDemo {

    // Plain class: NO equals/hashCode, NOT Comparable
    static class PlainPerson {
        final String name;
        final int age;

        PlainPerson(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return name + "(" + age + ")";
        }
    }

    // Record: equals, hashCode and toString are generated from all fields
    record Person(String name, String city, int age) {}

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    public static void main(String[] args) {

        List<Person> people = List.of(
                new Person("Alice", "Pune", 30),
                new Person("Bob", "Mumbai", 25),
                new Person("Alice", "Pune", 30),      // exact duplicate of the first
                new Person("Charlie", "Pune", 25),
                new Person("Alice", "Delhi", 28),     // same name, different city
                new Person("David", "Mumbai", 35)
        );

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) distinct() without equals/hashCode: nothing is removed
        List<PlainPerson> plain = List.of(new PlainPerson("Alice", 30), new PlainPerson("Alice", 30));
        show("distinct on plain class", plain.stream().distinct().count());

        // b) sorted() on a class that is not Comparable: ClassCastException
        try {
            plain.stream().sorted().collect(Collectors.toList());
        } catch (ClassCastException e) {
            show("sorted() on plain class", "ClassCastException");
        }

        // c) Natural String order is case-sensitive: uppercase comes first
        List<String> fruits = List.of("cherry", "Banana", "apple");
        show("natural String order", fruits.stream().sorted().collect(Collectors.toList()));

        // d) Comparator with subtraction can overflow
        List<Integer> risky = Arrays.asList(-1, Integer.MAX_VALUE);
        risky.stream().sorted((a, b) -> a - b).forEach(n -> System.out.print(n + " "));
        System.out.println("<- subtraction comparator, wrong order");

        // e) Manual duplicate removal with loops
        List<Person> loopDistinct = new ArrayList<>();
        for (Person p : people) {
            if (!loopDistinct.contains(p)) {
                loopDistinct.add(p);
            }
        }
        show("loop distinct size", loopDistinct.size());

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) distinct on records: first occurrence is kept, order is preserved
        List<Person> unique = people.stream().distinct().collect(Collectors.toList());
        show("distinct records", unique);

        // b) distinct by ONE field (name): keep the first person for each name
        List<Person> distinctByName = people.stream()
                .collect(Collectors.toMap(Person::name, p -> p, (first, second) -> first, LinkedHashMap::new))
                .values().stream()
                .collect(Collectors.toList());
        show("distinct by name", distinctByName);

        // c) Natural order, case-insensitive order, reverse order
        show("natural order", List.of(5, 3, 9, 1).stream().sorted().collect(Collectors.toList()));
        show("case-insensitive", fruits.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList()));
        show("reverse order", List.of(5, 3, 9, 1).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()));

        // d) Comparator chain: city A-Z, then age high to low, then name A-Z
        List<Person> chained = unique.stream()
                .sorted(Comparator.comparing(Person::city)
                        .thenComparing(Person::age, Comparator.reverseOrder())
                        .thenComparing(Person::name))
                .collect(Collectors.toList());
        chained.forEach(p -> System.out.println("  " + p));

        // e) reversed() flips the WHOLE comparator built before it
        List<Person> byAgeDesc = unique.stream()
                .sorted(Comparator.comparingInt(Person::age).reversed())
                .collect(Collectors.toList());
        show("age descending", byAgeDesc.stream().map(Person::age).collect(Collectors.toList()));
        // Pitfall: Comparator.comparing(p -> p.age()).reversed() does NOT compile,
        // because Java cannot infer the type of p. Use a method reference or
        // write Comparator.comparing((Person p) -> p.age()).reversed().

        // f) Integer.compare instead of subtraction
        show("safe comparator", risky.stream().sorted(Integer::compare).collect(Collectors.toList()));

        // g) nulls: nullsFirst / nullsLast
        List<String> withNulls = Arrays.asList("b", null, "a");
        show("nullsFirst", withNulls.stream()
                .sorted(Comparator.nullsFirst(Comparator.naturalOrder()))
                .collect(Collectors.toList()));

        // h) sorted() is stable: equal elements keep their original order
        List<String> sameCity = unique.stream()
                .sorted(Comparator.comparing(Person::city))
                .map(p -> p.city() + ":" + p.name())
                .collect(Collectors.toList());
        show("stable sort by city", sameCity);

        // i) Put distinct BEFORE sorted: sorting fewer elements costs less
        List<Integer> numbers = List.of(4, 1, 4, 3, 1, 2, 3);
        show("distinct then sorted", numbers.stream().distinct().sorted().collect(Collectors.toList()));
    }
}