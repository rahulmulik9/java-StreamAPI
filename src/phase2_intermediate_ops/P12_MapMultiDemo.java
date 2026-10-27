package phase2_intermediate_ops;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - mapMulti(BiConsumer<T, Consumer<R>>) (Java 16): for each element, push
 *     ZERO, ONE or MANY results into a "sink" (the Consumer), using plain code
 *   - mapMulti vs flatMap: no temporary Stream is created per element
 *   - The type witness: .<R>mapMulti(...) tells Java what the sink accepts
 *   - Primitive variants: mapMultiToInt, mapMultiToLong, mapMultiToDouble
 *   - When flatMap is still the better choice
 *
 * PROBLEM:
 *   flatMap needs a Stream for every element, even when you only emit 0, 1 or 2
 *   values. For a big stream, that is a lot of tiny throw-away Stream objects.
 *   It also forces you to express "if/else/loop" logic as Stream.of(...) and
 *   Stream.empty(), which can read awkwardly. And a small mistake with
 *   mapMulti, forgetting the type witness, gives a Stream<Object> and a
 *   compile error far from the real cause.
 *
 * SOLUTION:
 *   Use mapMulti when each element produces a small number of results, or when
 *   the logic is easier to write with if/for and sink.accept(...). Add the type
 *   witness. Use flatMap when you already HAVE a stream or collection to flatten.
 */
public class P12_MapMultiDemo {

    record Student(String name, List<String> courses) {}

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    // A reusable mapper: emit the number n, n times
    static void repeat(Integer n, Consumer<Integer> sink) {
        for (int i = 0; i < n; i++) {
            sink.accept(n);
        }
    }

    public static void main(String[] args) {

        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);
        List<String> words = List.of("java", "is", "fun", "streams");
        List<Student> students = List.of(
                new Student("Alice", List.of("Java", "SQL")),
                new Student("Bob", List.of("Java", "Python")),
                new Student("Charlie", List.of())
        );

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) flatMap: one tiny Stream object is created for EVERY element
        List<Integer> viaFlatMap = numbers.stream()
                .flatMap(n -> n % 2 == 0 ? Stream.of(n, n * 10) : Stream.<Integer>empty())
                .collect(Collectors.toList());
        show("flatMap (even: n and n*10)", viaFlatMap);

        // b) Compile error: without the type witness, Java infers Stream<Object>
        //
        //    List<Integer> broken = numbers.stream()
        //            .mapMulti((n, sink) -> sink.accept(n))      // Stream<Object>
        //            .collect(Collectors.toList());               // List<Object> != List<Integer>
        //
        //    The sink is a Consumer<Object> here, so Java cannot know you want Integer.
        //    Fix: numbers.stream().<Integer>mapMulti((n, sink) -> sink.accept(n))

        // c) flatMap with a loop: you must build a collection or stream by hand
        List<Integer> viaFlatMapRepeat = numbers.stream()
                .flatMap(n -> {
                    List<Integer> copies = new java.util.ArrayList<>();
                    for (int i = 0; i < n; i++) {
                        copies.add(n);
                    }
                    return copies.stream();
                })
                .limit(10)
                .collect(Collectors.toList());
        show("flatMap with loop", viaFlatMapRepeat);

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) Same result as 1a: emit zero or two elements, with plain if
        List<Integer> viaMapMulti = numbers.stream()
                .<Integer>mapMulti((n, sink) -> {
                    if (n % 2 == 0) {
                        sink.accept(n);
                        sink.accept(n * 10);
                    }
                    // odd numbers: emit nothing
                })
                .collect(Collectors.toList());
        show("mapMulti (even: n and n*10)", viaMapMulti);

        // b) filter + map in a single step
        List<String> longUpper = words.stream()
                .<String>mapMulti((w, sink) -> {
                    if (w.length() > 3) {
                        sink.accept(w.toUpperCase());
                    }
                })
                .collect(Collectors.toList());
        show("filter + map in one", longUpper);

        // c) Emit each element twice
        List<String> doubled = words.stream()
                .limit(2)
                .<String>mapMulti((w, sink) -> {
                    sink.accept(w);
                    sink.accept(w);
                })
                .collect(Collectors.toList());
        show("each element twice", doubled);

        // d) Reusable mapper through a method reference
        List<Integer> repeated = Stream.of(1, 2, 3)
                .<Integer>mapMulti(P12_MapMultiDemo::repeat)
                .collect(Collectors.toList());
        show("repeat(n) via method reference", repeated);

        // e) Flatten mixed content: elements are either an Integer or a List of Integers
        List<Object> mixed = List.of(1, List.of(2, 3), 4, List.of(5));
        List<Integer> flat = mixed.stream()
                .<Integer>mapMulti((o, sink) -> {
                    if (o instanceof Integer i) {
                        sink.accept(i);
                    } else if (o instanceof List<?> list) {
                        for (Object item : list) {
                            if (item instanceof Integer inner) {
                                sink.accept(inner);
                            }
                        }
                    }
                })
                .collect(Collectors.toList());
        show("flatten mixed content", flat);

        // f) Keep the parent's data: "student:course" pairs
        List<String> pairs = students.stream()
                .<String>mapMulti((s, sink) -> {
                    for (String course : s.courses()) {
                        sink.accept(s.name() + ":" + course);
                    }
                })
                .collect(Collectors.toList());
        show("student:course pairs", pairs);

        // g) Primitive variant: no boxing at all, the sink is an IntConsumer
        int totalLetters = students.stream()
                .mapMultiToInt((s, sink) -> {
                    for (String course : s.courses()) {
                        sink.accept(course.length());
                    }
                })
                .sum();
        show("total letters in all courses", totalLetters);

        // h) flatMap is still the better choice when you ALREADY have a collection or stream
        List<String> viaFlatMapPairs = students.stream()
                .flatMap(s -> s.courses().stream().map(c -> s.name() + ":" + c))
                .collect(Collectors.toList());
        show("flatMap gives the same pairs", viaFlatMapPairs.equals(pairs));

        // i) Lazy like every other intermediate operation: nothing runs without a terminal operation
        numbers.stream().<Integer>mapMulti((n, sink) -> {
            System.out.println("  never printed: " + n);
            sink.accept(n);
        });
    }
}