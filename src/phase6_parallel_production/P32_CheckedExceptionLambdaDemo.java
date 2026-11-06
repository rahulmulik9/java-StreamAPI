package phase6_parallel_production;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Standard functional interfaces cannot throw checked exceptions
 *   - Three fixes: try/catch inside the lambda, a wrapper that rethrows as
 *     unchecked, and skipping failures with flatMap
 *
 * PROBLEM:
 *   Calling a method that throws a checked exception (IOException) inside map
 *   does not compile, because Function.apply does not declare any exception.
 *
 * SOLUTION:
 *   Handle the exception inside the lambda, or wrap the checked exception in a
 *   RuntimeException with a small reusable helper.
 */
public class P32_CheckedExceptionLambdaDemo {

    @FunctionalInterface
    interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }

    static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    static String loadName(int id) throws IOException {
        if (id < 0) {
            throw new IOException("bad id " + id);
        }
        return "user" + id;
    }

    public static void main(String[] args) {
        List<Integer> ids = List.of(1, 2, -3, 4);

        // 1. Broken version
        // ids.stream().map(id -> loadName(id)).toList();
        // compile error: unreported exception IOException

        // 2. Fixed version
        // a) try/catch inside the lambda, with a default value
        List<String> withDefault = ids.stream().map(id -> {
            try {
                return loadName(id);
            } catch (IOException e) {
                return "unknown";
            }
        }).toList();
        System.out.println("try/catch : " + withDefault);

        // b) wrapper: the checked exception becomes a RuntimeException
        Function<Integer, String> safe = unchecked(P32_CheckedExceptionLambdaDemo::loadName);
        try {
            ids.stream().map(safe).toList();
        } catch (RuntimeException e) {
            System.out.println("wrapper   : " + e.getCause().getMessage());
        }

        // c) skip the failures
        List<String> skipped = ids.stream().flatMap(id -> {
            try {
                return Stream.of(loadName(id));
            } catch (IOException e) {
                return Stream.<String>empty();
            }
        }).toList();
        System.out.println("flatMap skip : " + skipped);
    }
}