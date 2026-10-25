package phase1_basics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Functional interface = an interface with exactly ONE abstract method
 *   - The five you will see everywhere in streams:
 *       Predicate<T>    T -> boolean     test()     used by filter, anyMatch
 *       Function<T,R>   T -> R           apply()    used by map, flatMap
 *       Consumer<T>     T -> void        accept()   used by forEach, peek
 *       Supplier<T>     () -> T          get()      used by generate, orElseGet
 *       Comparator<T>   (T, T) -> int    compare()  used by sorted, min, max
 *   - Variants: BiFunction, UnaryOperator, BinaryOperator
 *   - Combining them: and, or, negate, andThen, compose, thenComparing, reversed
 *   - The 4 kinds of method references
 *
 * PROBLEM:
 *   Without lambdas, every small piece of logic needs an anonymous class:
 *   5 to 6 lines of boilerplate for one line of real logic. Pipelines become
 *   unreadable, and nobody knows which interface fits which stream method.
 *
 * SOLUTION:
 *   Write the logic as a lambda or method reference, and know the shape of each
 *   interface (what goes in, what comes out). The shape tells you where it fits.
 */
public class P05_FunctionalInterfacesDemo {

    // helper so the output lines up
    private static void show(String label, Object value) {
        System.out.printf("%-24s: %s%n", label, value);
    }

    public static void main(String[] args) {

        List<String> names = Arrays.asList("Alice", "bob", "Charlie", "david", "Eve");

        // ------------------------------------------------------------
        // 1. Broken / verbose version: anonymous classes
        // ------------------------------------------------------------
        Predicate<String> oldLong = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.length() > 3;
            }
        };
        Function<String, String> oldUpper = new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s.toUpperCase();
            }
        };
        List<String> oldResult = names.stream()
                .filter(oldLong)
                .map(oldUpper)
                .collect(Collectors.toList());
        show("Anonymous classes", oldResult);

        // Another trap: a lambda cannot change a local variable.
        //   int counter = 0;
        //   names.forEach(n -> counter++);
        //   -> compile error: local variables referenced from a lambda
        //      expression must be final or effectively final

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) Predicate<T>: T -> boolean
        Predicate<String> isLong = s -> s.length() > 3;
        Predicate<String> startsUpper = s -> Character.isUpperCase(s.charAt(0));
        show("isLong.test(Alice)", isLong.test("Alice"));
        show("isLong.test(bob)", isLong.test("bob"));
        show("and", names.stream().filter(isLong.and(startsUpper)).collect(Collectors.toList()));
        show("or", names.stream().filter(isLong.or(startsUpper)).collect(Collectors.toList()));
        show("negate", names.stream().filter(isLong.negate()).collect(Collectors.toList()));

        // b) Function<T,R>: T -> R   (+ BiFunction, UnaryOperator, BinaryOperator)
        Function<String, Integer> toLength = String::length;
        show("toLength(Alice)", toLength.apply("Alice"));

        Function<String, String> trim = String::trim;
        Function<String, String> upper = String::toUpperCase;
        show("trim.andThen(upper)", trim.andThen(upper).apply("  java "));   // trim first, then upper
        show("upper.compose(trim)", upper.compose(trim).apply("  java "));   // compose runs the argument first

        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;         // two inputs
        show("BiFunction add(3, 4)", add.apply(3, 4));

        UnaryOperator<String> shout = s -> s.toUpperCase() + "!";            // Function<T,T>
        show("UnaryOperator shout", shout.apply("hey"));

        BinaryOperator<Integer> multiply = (a, b) -> a * b;                  // BiFunction<T,T,T>
        show("BinaryOperator multiply", multiply.apply(3, 4));

        // c) Consumer<T>: T -> void
        Consumer<String> printName = s -> System.out.print("[" + s + "]");
        Consumer<String> printLen = s -> System.out.println(" length=" + s.length());
        System.out.printf("%-24s: ", "Consumer andThen");
        printName.andThen(printLen).accept("Alice");

        // d) Supplier<T>: () -> T   (a factory, gives a value each time get() is called)
        Supplier<List<String>> listMaker = ArrayList::new;
        List<String> l1 = listMaker.get();
        List<String> l2 = listMaker.get();
        l1.add("x");
        show("Supplier l1 / l2", l1 + " / " + l2);
        show("Same object?", l1 == l2);

        // e) Comparator<T>: (T, T) -> int
        Comparator<String> byLength = Comparator.comparing(String::length);
        show("sorted by length", names.stream()
                .sorted(byLength.thenComparing(String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList()));
        show("length descending", names.stream()
                .sorted(byLength.reversed())
                .collect(Collectors.toList()));

        // f) The 4 kinds of method references
        Function<String, Integer> parse = Integer::parseInt;                 // 1. static method
        Function<String, String> greet = "Hello, "::concat;                  // 2. method of a specific object
        Function<String, String> lower = String::toLowerCase;                // 3. method of "any object of this type"
        Function<String, StringBuilder> newSb = StringBuilder::new;          // 4. constructor
        show("Integer::parseInt", parse.apply("42"));
        show("\"Hello, \"::concat", greet.apply("Alice"));
        show("String::toLowerCase", lower.apply("ALICE"));
        show("StringBuilder::new", newSb.apply("abc").reverse());

        // g) Everything together in one pipeline
        System.out.println("Pipeline (and + map + forEach):");
        names.stream()
                .filter(isLong.and(startsUpper))     // Predicate
                .map(String::toUpperCase)            // Function
                .forEach(n -> System.out.println("  " + n));   // Consumer

        // ------------------------------------------------------------
        // 3. Output explanation
        // ------------------------------------------------------------
        /*
         * Expected output:
         *   Anonymous classes       : [ALICE, CHARLIE, DAVID]
         *   isLong.test(Alice)      : true
         *   isLong.test(bob)        : false
         *   and                     : [Alice, Charlie]
         *   or                      : [Alice, Charlie, david, Eve]
         *   negate                  : [bob, Eve]
         *   toLength(Alice)         : 5
         *   trim.andThen(upper)     : JAVA
         *   upper.compose(trim)     : JAVA
         *   BiFunction add(3, 4)    : 7
         *   UnaryOperator shout     : HEY!
         *   BinaryOperator multiply : 12
         *   Consumer andThen        : [Alice] length=5
         *   Supplier l1 / l2        : [x] / []
         *   Same object?            : false
         *   sorted by length        : [bob, Eve, Alice, david, Charlie]
         *   length descending       : [Charlie, Alice, david, bob, Eve]
         *   Integer::parseInt       : 42
         *   "Hello, "::concat       : Hello, Alice
         *   String::toLowerCase     : alice
         *   StringBuilder::new      : cba
         *   Pipeline (and + map + forEach):
         *     ALICE
         *     CHARLIE
         *
         * - The anonymous class and the lambda do the same job. The lambda
         *   keeps only the logic: s -> s.length() > 3.
         *
         * - Predicate: and = both true, or = at least one true, negate = flip.
         *   "and" keeps Alice and Charlie (long AND starts uppercase).
         *   "Eve" is uppercase but short, so "and" drops it and "or" keeps it.
         *
         * - Function: andThen(g) runs this first, then g. compose(g) runs g first,
         *   then this. Both give JAVA here because trim must run before upper.
         *
         * - UnaryOperator<T> is Function<T,T>. BinaryOperator<T> is BiFunction<T,T,T>.
         *   Integer::sum is a ready-made BinaryOperator<Integer>, used in reduce.
         *
         * - Consumer returns nothing, so it is used for side effects like
         *   printing. andThen chains two consumers on the same input.
         *
         * - Supplier takes nothing and returns a value. Every get() can build a
         *   new object, so l1 and l2 are different lists.
         *
         * - Comparator: thenComparing breaks ties, reversed flips the order.
         *   Length 3 ties (bob, Eve) and length 5 ties (Alice, david) are broken
         *   alphabetically, ignoring case.
         *
         * - Method references are shortcuts for lambdas that only call one method:
         *     Integer::parseInt        = s -> Integer.parseInt(s)
         *     "Hello, "::concat        = s -> "Hello, ".concat(s)
         *     String::toLowerCase      = s -> s.toLowerCase()
         *     StringBuilder::new       = s -> new StringBuilder(s)
         *
         * - Rule of thumb: use a method reference when it reads clearly, and a
         *   lambda when you need extra logic.
         */
    }
}