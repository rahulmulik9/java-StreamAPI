package phase2_intermediate_ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - flatMap(Function<T, Stream<R>>): one element in, ZERO or MORE elements out,
 *     and the resulting streams are merged into one flat stream
 *   - map vs flatMap: Stream<Stream<T>> vs Stream<T>
 *   - Flatten nested lists, split sentences into words, stream a collection
 *     held inside an object
 *   - Optional::stream (Java 9), flatMapToInt, and a cartesian product
 *
 * PROBLEM:
 *   With nested data (a list of lists, a list of sentences, objects that hold
 *   lists), map keeps the nesting. You get a Stream of Lists, a Stream of
 *   arrays or even a Stream of Streams, and the next filter or map is
 *   working on the wrong level. A manual fix needs nested for-loops.
 *
 * SOLUTION:
 *   flatMap maps each element to a stream and then flattens all those streams
 *   into one. The rest of the pipeline sees plain elements again.
 */
public class P07_FlatMapDemo {

    record Student(String name, List<String> courses) {}

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    public static void main(String[] args) {

        List<List<Integer>> nested = List.of(
                List.of(1, 2, 3),
                List.of(4, 5),
                List.of(6)
        );
        List<String> sentences = List.of("java streams are lazy", "flatMap flattens streams");

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) For-loop: nested loops just to flatten
        List<Integer> loopResult = new ArrayList<>();
        for (List<Integer> inner : nested) {
            for (Integer n : inner) {
                loopResult.add(n);
            }
        }
        show("Nested for-loops", loopResult);

        // b) Mistake: map on a list of lists -> a stream of streams (3 streams, not 6 numbers)
        List<java.util.stream.Stream<Integer>> streams = nested.stream()
                .map(List::stream)
                .collect(Collectors.toList());
        show("map(List::stream) size", streams.size() + "  (3 Stream objects, not 6 numbers)");

        // c) Mistake: map(split) -> a stream of String[] (2 arrays, not words)
        List<String[]> arrays = sentences.stream()
                .map(s -> s.split(" "))
                .collect(Collectors.toList());
        show("map(split) size", arrays.size() + " arrays, first has " + arrays.get(0).length + " words");

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) Flatten a list of lists
        List<Integer> flat = nested.stream()
                .flatMap(List::stream)                       // Function<List<Integer>, Stream<Integer>>
                .collect(Collectors.toList());
        show("flatMap flatten", flat);

        // b) After flattening, filter and map work on plain numbers
        List<Integer> evenSquares = nested.stream()
                .flatMap(List::stream)
                .filter(n -> n % 2 == 0)
                .map(n -> n * n)
                .collect(Collectors.toList());
        show("flatten + filter + map", evenSquares);

        // c) Sentences -> words
        List<String> words = sentences.stream()
                .flatMap(s -> Arrays.stream(s.split(" ")))   // String -> Stream<String>
                .collect(Collectors.toList());
        show("word count", words.size());
        List<String> uniqueSorted = words.stream()
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        show("unique sorted words", uniqueSorted);

        // d) An object that holds a collection
        List<Student> students = List.of(
                new Student("Alice", List.of("Java", "SQL")),
                new Student("Bob", List.of("Java", "Python")),
                new Student("Charlie", List.of())            // no courses
        );
        List<String> allCourses = students.stream()
                .flatMap(s -> s.courses().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        show("all distinct courses", allCourses);

        // e) Keep the parent's data: map INSIDE the flatMap
        List<String> pairs = students.stream()
                .flatMap(s -> s.courses().stream().map(c -> s.name() + ":" + c))
                .collect(Collectors.toList());
        show("student:course pairs", pairs);

        // f) Optional::stream (Java 9): present -> 1 element, empty -> 0 elements
        List<Optional<String>> optionals = List.of(Optional.of("a"), Optional.empty(), Optional.of("c"));
        List<String> present = optionals.stream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        show("Optional::stream", present);

        // g) Primitive version: flatMapToInt on a 2D int array
        int[][] matrix = {{1, 2}, {3, 4}, {5}};
        int matrixSum = Arrays.stream(matrix)                // Stream<int[]>
                .flatMapToInt(Arrays::stream)                // int[] -> IntStream
                .sum();
        show("matrix sum", matrixSum);

        // h) Cartesian product: every number with every letter
        List<String> combos = List.of(1, 2).stream()
                .flatMap(a -> List.of("x", "y").stream().map(b -> a + b))
                .collect(Collectors.toList());
        show("cartesian product", combos);

        // ------------------------------------------------------------
        // 3. Output explanation
        // ------------------------------------------------------------
        /*
         * Expected output:
         *   Nested for-loops : [1, 2, 3, 4, 5, 6]
         *   map(List::stream) size : 3  (3 Stream objects, not 6 numbers)
         *   map(split) size : 2 arrays, first has 4 words
         *   flatMap flatten : [1, 2, 3, 4, 5, 6]
         *   flatten + filter + map : [4, 16, 36]
         *   word count : 7
         *   unique sorted words : [are, flatmap, flattens, java, lazy, streams]
         *   all distinct courses : [Java, Python, SQL]
         *   student:course pairs : [Alice:Java, Alice:SQL, Bob:Java, Bob:Python]
         *   Optional::stream : [a, c]
         *   matrix sum : 15
         *   cartesian product : [1x, 1y, 2x, 2y]
         *
         * - map is one-to-one: 1 element in, exactly 1 element out.
         *   flatMap is one-to-many: 1 element in, a STREAM of 0..n elements out,
         *   and all those streams are merged into one.
         *
         *       map:      [ [1,2,3], [4,5], [6] ]  ->  Stream<Stream<Integer>>   (still nested)
         *       flatMap:  [ [1,2,3], [4,5], [6] ]  ->  Stream<Integer> 1,2,3,4,5,6
         *
         * - Mistake b: map(List::stream) turns each inner list into a Stream, so
         *   you get 3 streams. Mistake c: split returns String[], so map gives
         *   2 arrays. In both cases the next operation works on the wrong level.
         *   The fix is the same: the function passed to flatMap must RETURN A
         *   STREAM (List::stream, Arrays.stream(...), Optional::stream).
         *
         * - Words: 4 + 3 = 7 words in total. After toLowerCase, "streams" appears
         *   twice, and distinct removes the duplicate. "flatMap" becomes "flatmap".
         *   sorted() then gives alphabetical order.
         *
         * - Students: Charlie has an empty list, so he turns into an EMPTY stream
         *   and contributes nothing. This is a feature: flatMap can also drop
         *   elements (0 out). Java is repeated by Alice and Bob, and distinct
         *   keeps it once.
         *
         * - Section e: when you also need data from the parent object (the
         *   student's name), put a map INSIDE the flatMap lambda, where the
         *   parent is still in scope. After the flatMap it is gone.
         *
         * - Optional::stream: present Optional -> 1 element, empty -> 0 elements.
         *   It is the clean way to "filter out the empties and unwrap".
         *
         * - flatMapToInt, flatMapToLong and flatMapToDouble return primitive
         *   streams, so you avoid boxing. 1+2+3+4+5 = 15.
         *
         * - Cartesian product: for each a (1, 2), the inner stream builds
         *   "1x","1y" / "2x","2y". flatMap joins them in order. This replaces
         *   two nested for-loops.
         *
         * - Rule of thumb: if after map you see Stream<List<...>>,
         *   Stream<String[]> or Stream<Stream<...>>, you needed flatMap.
         */
    }
}