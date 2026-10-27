package phase2_intermediate_ops;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - limit(n): keep only the first n elements (short-circuiting)
 *   - skip(n): throw away the first n elements
 *   - Pagination with skip((page - 1) * size).limit(size)
 *   - Order dependence: skip/limit work on the stream AS IT IS at that point,
 *     so sorted/filter before or after them changes the result
 *   - Edge cases: skip larger than size, limit(0), negative values
 *
 * PROBLEM:
 *   Pagination with subList and index maths throws IndexOutOfBoundsException on the
 *   last page. Placing limit BEFORE sorted gives the "top 3" of an arbitrary
 *   first 3 elements, not the real top 3. Negative arguments throw an exception.
 *
 * SOLUTION:
 *   skip + limit never throw when the range is too large, they just return less.
 *   Put limit/skip AFTER the operations that decide order and membership
 *   (filter, sorted), and BEFORE the expensive ones you want to run on fewer elements.
 */
public class P09_LimitSkipDemo {

    record Product(String name, int price) {}

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    // Fixed pagination: page numbers start at 1
    static <T> List<T> page(List<T> items, int pageNumber, int pageSize) {
        return items.stream()
                .skip((long) (pageNumber - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    // Broken pagination: subList with index maths
    static <T> List<T> pageWithSubList(List<T> items, int pageNumber, int pageSize) {
        int from = (pageNumber - 1) * pageSize;
        return items.subList(from, from + pageSize);
    }

    public static void main(String[] args) {

        List<Integer> numbers = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());

        List<Product> products = List.of(
                new Product("Keyboard", 1500),
                new Product("Monitor", 12000),
                new Product("Mouse", 700),
                new Product("Laptop", 55000),
                new Product("Webcam", 3000),
                new Product("Headset", 4500)
        );

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) for-loop pagination
        List<Integer> loopPage = new ArrayList<>();
        for (int i = 3; i < 6 && i < numbers.size(); i++) {
            loopPage.add(numbers.get(i));
        }
        show("loop page", loopPage);

        // b) subList pagination: the last page is incomplete -> exception
        try {
            pageWithSubList(numbers, 3, 4);      // elements 9..12, but only 10 exist
        } catch (IndexOutOfBoundsException e) {
            show("subList last page", "IndexOutOfBoundsException");
        }

        // c) Wrong order: limit BEFORE sorted -> sorts only the first 3 products
        List<String> wrongTop3 = products.stream()
                .limit(3)
                .sorted(Comparator.comparingInt(Product::price).reversed())
                .map(Product::name)
                .collect(Collectors.toList());
        show("limit then sorted (wrong)", wrongTop3);

        // d) Negative arguments are not allowed
        try {
            numbers.stream().limit(-1).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            show("limit(-1)", "IllegalArgumentException");
        }
        try {
            numbers.stream().skip(-1).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            show("skip(-1)", "IllegalArgumentException");
        }

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) Basic limit and skip
        show("limit(3)", numbers.stream().limit(3).collect(Collectors.toList()));
        show("skip(7)", numbers.stream().skip(7).collect(Collectors.toList()));

        // b) Pagination: page size 4
        show("page 1", page(numbers, 1, 4));
        show("page 2", page(numbers, 2, 4));
        show("page 3 (last, partial)", page(numbers, 3, 4));
        show("page 4 (beyond the end)", page(numbers, 4, 4));

        // c) Correct order: sorted first, then limit
        List<String> top3 = products.stream()
                .sorted(Comparator.comparingInt(Product::price).reversed())
                .limit(3)
                .map(Product::name)
                .collect(Collectors.toList());
        show("sorted then limit (correct)", top3);

        // d) Second to fourth most expensive: skip + limit
        List<String> ranks2to4 = products.stream()
                .sorted(Comparator.comparingInt(Product::price).reversed())
                .skip(1)
                .limit(3)
                .map(Product::name)
                .collect(Collectors.toList());
        show("rank 2 to 4", ranks2to4);

        // e) skip and limit order matters
        show("limit(5).skip(2)", numbers.stream().limit(5).skip(2).collect(Collectors.toList()));
        show("skip(2).limit(5)", numbers.stream().skip(2).limit(5).collect(Collectors.toList()));

        // f) Edge cases
        show("skip(100)", numbers.stream().skip(100).collect(Collectors.toList()));
        show("limit(0)", numbers.stream().limit(0).collect(Collectors.toList()));
        show("limit(100)", numbers.stream().limit(100).count());

        // g) Infinite streams: limit makes them finite
        show("iterate + skip + limit", Stream.iterate(1, n -> n + 1).skip(5).limit(3).collect(Collectors.toList()));
        show("filter then limit", Stream.iterate(1, n -> n + 1)
                .filter(n -> n % 7 == 0)
                .limit(3)
                .collect(Collectors.toList()));
        show("limit then filter", Stream.iterate(1, n -> n + 1)
                .limit(3)
                .filter(n -> n % 7 == 0)
                .collect(Collectors.toList()));

        // h) limit before an expensive step: only 2 calls happen
        List<String> labels = products.stream()
                .limit(2)
                .map(p -> p.name().toUpperCase())     // pretend this is slow
                .collect(Collectors.toList());
        show("limit before expensive map", labels);
    }
}