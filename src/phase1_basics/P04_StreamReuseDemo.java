package phase1_basics;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - A stream can be used only ONCE
 *   - IllegalStateException: "stream has already been operated upon or closed"
 *   - Three fixes: create a new stream, use a Supplier<Stream>, or collect to a List first
 *
 * PROBLEM:
 *   Storing a stream in a variable and using it twice looks natural, but the
 *   second use throws IllegalStateException. This also happens when you try to
 *   "branch" the same stream into two different pipelines.
 *
 * SOLUTION:
 *   Keep the SOURCE (the list), not the stream. Create a fresh stream for every
 *   pipeline, wrap the creation in a Supplier, or collect the data once and reuse it.
 */
public class P04_StreamReuseDemo {
    public static void main(String[] args) {

        List<String> names = List.of("Alice", "Bob", "Charlie", "David", "Eve");

        // ------------------------------------------------------------
        // 1. Broken version
        // ------------------------------------------------------------

        // Mistake A: reuse a stream after a terminal operation
        /*
        The stream stores no data. It stores a reference to where the data lives, plus a description of what to do with it.*/
        Stream<String> stream = names.stream();
        long count = stream.filter(n -> n.length() > 3).count();      // terminal: stream is now CONSUMED
        System.out.println("Count of long names     : " + count);

        //Each stream object has an internal boolean flag, linkedOrConsumed, in the JDK class AbstractPipeline
        try {
            stream.map(String::toUpperCase).collect(Collectors.toList());   // second use
        } catch (IllegalStateException e) {
            System.out.println("Reuse after terminal    : IllegalStateException -> " + e.getMessage());
        }

        // Mistake B: branch one stream into two pipelines (no terminal operation needed to fail)
        Stream<String> base = names.stream();
        Stream<String> longNames = base.filter(n -> n.length() > 3);   // first operation on 'base'
        try {
            Stream<String> shortNames = base.filter(n -> n.length() <= 3);   // second operation on 'base'
        } catch (IllegalStateException e) {
            System.out.println("Branching same stream   : IllegalStateException -> " + e.getMessage());
        }

        // ------------------------------------------------------------
        // 2. Fixed version
        // ------------------------------------------------------------

        // Fix 1: create a new stream from the source each time
        long longCount = names.stream().filter(n -> n.length() > 3).count();
        List<String> upper = names.stream().map(String::toUpperCase).collect(Collectors.toList());
        System.out.println("Fix 1 (new stream)      : " + longCount + " " + upper);

        // Fix 2: Supplier<Stream<T>>, a "stream factory" that gives a fresh stream on every get()
        Supplier<Stream<String>> supplier = names::stream;
        long c = supplier.get().filter(n -> n.length() > 3).count();
        List<String> shortOnes = supplier.get().filter(n -> n.length() <= 3).collect(Collectors.toList());
        System.out.println("Fix 2 (Supplier)        : " + c + " " + shortOnes);

        // Fix 3: collect once into a List, then stream that List as many times as needed
        List<String> longOnes = names.stream().filter(n -> n.length() > 3).collect(Collectors.toList());
        long totalLetters = longOnes.stream().mapToInt(String::length).sum();
        String joined = longOnes.stream().collect(Collectors.joining(", "));
        System.out.println("Fix 3 (collect first)   : " + totalLetters + " letters, " + joined);

        // ------------------------------------------------------------
        // 3. Output explanation
        // ------------------------------------------------------------
        /*
         * Expected output:
         *   Count of long names     : 3
         *   Reuse after terminal    : IllegalStateException -> stream has already been operated upon or closed
         *   Branching same stream   : IllegalStateException -> stream has already been operated upon or closed
         *   Fix 1 (new stream)      : 3 [ALICE, BOB, CHARLIE, DAVID, EVE]
         *   Fix 2 (Supplier)        : 3 [Bob, Eve]
         *   Fix 3 (collect first)   : 17 letters, Alice, Charlie, David
         *
         * - A stream is a one-shot pipeline, like a water pipe: once the water
         *   has flowed through, you cannot run it again. It is NOT a collection.
         *
         * - Mistake A: count() is terminal and consumes the stream. Any later
         *   use of the same variable fails.
         *
         * - Mistake B: even an INTERMEDIATE operation "uses up" a stream.
         *   Calling filter() on 'base' twice fails on the second call, even
         *   though no terminal operation ran.
         *
         * - Fix 1: simplest. The list can create as many streams as you want.
         *
         * - Fix 2: Supplier<Stream<String>> is useful when you pass a "stream
         *   source" around. Each get() builds a brand-new stream. Note that
         *   each get() re-runs the source, so for expensive sources prefer Fix 3.
         *
         * - Fix 3: best when the first part of the pipeline is expensive
         *   (database call, big filter). Do it once, keep the List, and
         *   stream it again for each different result.
         *
         * - 17 letters = Alice (5) + Charlie (7) + David (5).
         */
    }
}