package phase7_interview_questions;

import java.util.*;
import java.util.stream.*;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - First N Fibonacci numbers and first N prime numbers
 *   - Stream.iterate with a pair, IntStream.iterate, limit, noneMatch
 *
 * PROBLEM:
 *   Fibonacci needs the TWO previous values, but iterate remembers only one.
 *   For primes, forgetting n > 1 makes 0 and 1 look prime, because noneMatch on
 *   an empty range is true.
 *
 * SOLUTION:
 *   Iterate over a pair (long[]) and keep only the first of the pair. For primes,
 *   start at 2 (or check n > 1) and test divisors up to the square root.
 */
public class P45_FibonacciPrimesDemo {

    static boolean isPrime(int n) {
        return n > 1 && IntStream.rangeClosed(2, (int) Math.sqrt(n)).noneMatch(i -> n % i == 0);
    }

    public static void main(String[] args) {

        // 1. Broken version: no check for n > 1
        System.out.println("primes up to 10 (wrong) : " + IntStream.rangeClosed(0, 10)
                .filter(n -> IntStream.rangeClosed(2, (int) Math.sqrt(n)).noneMatch(i -> n % i == 0))
                .boxed()
                .toList());

        // 2. Fixed version
        System.out.println("first 10 Fibonacci : " + Stream.iterate(new long[]{0, 1}, f -> new long[]{f[1], f[0] + f[1]})
                .limit(10)
                .map(f -> f[0])
                .toList());

        System.out.println("first 10 primes    : " + IntStream.iterate(2, n -> n + 1)
                .filter(P45_FibonacciPrimesDemo::isPrime)
                .limit(10)
                .boxed()
                .toList());

        System.out.println("primes up to 30    : " + IntStream.rangeClosed(2, 30)
                .filter(P45_FibonacciPrimesDemo::isPrime)
                .boxed()
                .toList());

        System.out.println("sum of first 10 Fibonacci : " + Stream.iterate(new long[]{0, 1}, f -> new long[]{f[1], f[0] + f[1]})
                .limit(10)
                .mapToLong(f -> f[0])
                .sum());
    }
}