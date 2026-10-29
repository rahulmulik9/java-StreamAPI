package phase3_terminal_ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Stream.toList()                    (Java 16): unmodifiable, allows null
 *   - Collectors.toList()                : modifiable in practice (no guarantee), allows null
 *   - Collectors.toUnmodifiableList()    (Java 10): unmodifiable, rejects null
 *   - Collectors.toCollection(...)       : you choose the exact collection type
 *   - Snapshot, not a view: changing the source later does not change the result
 *
 * PROBLEM:
 *   Replacing Collectors.toList() with toList() looks like a harmless shortcut,
 *   but code that later calls add, remove or sort on the result throws
 *   UnsupportedOperationException. In the other direction, toUnmodifiableList()
 *   throws NullPointerException on a null element.
 *
 * SOLUTION:
 *   Use toList() when the result is read-only, toCollection(ArrayList::new) when you
 *   need a list you can change, and remember that toList() is not a Collector, so
 *   it cannot be passed to groupingBy.
 */
public class P17_ToListVsCollectDemo {

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    private static String tryAdd(List<String> list) {
        try {
            list.add("new");
            return "mutable";
        } catch (UnsupportedOperationException e) {
            return "immutable (UnsupportedOperationException)";
        }
    }

    public static void main(String[] args) {

        List<String> names = List.of("charlie", "alice", "bob", "alice");

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) add to the result of toList()
        List<String> readOnly = names.stream().toList();
        try {
            readOnly.add("david");
        } catch (UnsupportedOperationException e) {
            show("add to toList()", "UnsupportedOperationException");
        }

        // b) sort the result of toList() in place
        try {
            readOnly.sort(String::compareTo);
        } catch (UnsupportedOperationException e) {
            show("sort toList() in place", "UnsupportedOperationException");
        }

        // c) null element in toUnmodifiableList()
        List<String> withNull = Arrays.asList("a", null, "b");
        try {
            withNull.stream().collect(Collectors.toUnmodifiableList());
        } catch (NullPointerException e) {
            show("toUnmodifiableList with null", "NullPointerException");
        }

        // d) Relying on Collectors.toList() being mutable: works today, but it is
        //    not guaranteed by the specification
        List<String> accidental = names.stream().collect(Collectors.toList());
        accidental.add("works by accident");
        show("Collectors.toList() add", accidental);

        // e) Assuming the result is a live view of the source
        List<String> source = new ArrayList<>(List.of("a", "b"));
        List<String> snapshot = source.stream().toList();
        source.add("c");
        show("source after add", source);
        show("snapshot (not a view)", snapshot);

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) Comparison of the three
        show("Stream.toList()", tryAdd(names.stream().toList()));
        show("Collectors.toList()", tryAdd(names.stream().collect(Collectors.toList())));
        show("Collectors.toUnmodifiableList()", tryAdd(names.stream().collect(Collectors.toUnmodifiableList())));

        // b) Nulls
        show("Stream.toList() with null", withNull.stream().toList());
        show("Collectors.toList() with null", withNull.stream().collect(Collectors.toList()));

        // c) You need to change the result: ask for the type you want
        List<String> editable = names.stream().collect(Collectors.toCollection(ArrayList::new));
        editable.add("david");
        editable.sort(String::compareTo);
        show("toCollection(ArrayList::new)", editable);

        List<String> copy = new ArrayList<>(names.stream().toList());
        copy.remove("bob");
        show("new ArrayList<>(toList())", copy);

        // d) toCollection with other types
        show("toCollection(TreeSet::new)", names.stream().collect(Collectors.toCollection(TreeSet::new)));

        // e) Sets
        Set<String> unique = names.stream().collect(Collectors.toSet());
        show("toSet size", unique.size());
        show("toUnmodifiableSet size", names.stream().collect(Collectors.toUnmodifiableSet()).size());

        // f) Stream.toList() is NOT a Collector: groupingBy and mapping need Collectors.toList()
        Map<Integer, List<String>> byLength = names.stream()
                .distinct()
                .collect(Collectors.groupingBy(String::length, Collectors.mapping(String::toUpperCase, Collectors.toList())));
        show("groupingBy + mapping + toList", byLength);

        // g) Arrays
        String[] array = names.stream().toArray(String[]::new);
        show("toArray", Arrays.toString(array));

        // h) A chain that ends with toList() is the shortest read-only form
        List<String> result = names.stream()
                .distinct()
                .sorted()
                .map(String::toUpperCase)
                .toList();
        show("distinct + sorted + map + toList", result);
    }
}