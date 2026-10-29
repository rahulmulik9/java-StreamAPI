package phase3_terminal_ops;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Optional: a box that holds a value or is empty, instead of returning null
 *   - Creating: of, ofNullable, empty
 *   - Reading: orElse, orElseGet, orElseThrow, ifPresent, ifPresentOrElse
 *   - Transforming: map, flatMap, filter, or, stream
 *   - orElse (always evaluated) vs orElseGet (evaluated only when empty)
 *
 * PROBLEM:
 *   Returning null forces every caller to remember a null check, and one missed
 *   check gives a NullPointerException far from the cause. Optional.of(null)
 *   throws, get() on an empty Optional throws, and orElse(expensiveCall())
 *   runs expensiveCall() even when the value is present.
 *
 * SOLUTION:
 *   Return Optional from methods that may have no result, build it with ofNullable,
 *   chain with map/flatMap/filter, and finish with orElseGet or orElseThrow.
 *   Avoid isPresent() + get().
 */
public class P16_OptionalDemo {

    record User(String id, String name, String email) {}   // email may be null

    private static final Map<String, User> USERS = Map.of(
            "1", new User("1", "Alice", "alice@mail.com"),
            "2", new User("2", "Bob", null)
    );

    static User findUserOrNull(String id) {
        return USERS.get(id);
    }

    static Optional<User> findUser(String id) {
        return Optional.ofNullable(USERS.get(id));
    }

    static Optional<String> domainOf(String email) {
        int at = email.indexOf('@');
        return at >= 0 ? Optional.of(email.substring(at + 1)) : Optional.empty();
    }

    static String expensiveDefault() {
        System.out.println("  computing default...");
        return "guest@mail.com";
    }

    private static void show(String label, Object value) {
        System.out.println(label + " : " + value);
    }

    public static void main(String[] args) {

        // ------------------------------------------------------------
        // 1. Broken / verbose version
        // ------------------------------------------------------------

        // a) null returned, NullPointerException later
        try {
            findUserOrNull("9").email();
        } catch (NullPointerException e) {
            show("null user", "NullPointerException");
        }

        // b) Defensive null checks everywhere
        String emailLoop = "none";
        User u = findUserOrNull("2");
        if (u != null) {
            if (u.email() != null) {
                emailLoop = u.email().toUpperCase();
            }
        }
        show("nested null checks", emailLoop);

        // c) Optional.of(null)
        try {
            Optional.of((String) null);
        } catch (NullPointerException e) {
            show("Optional.of(null)", "NullPointerException");
        }

        // d) get() on an empty Optional
        try {
            findUser("9").get();
        } catch (NoSuchElementException e) {
            show("get() on empty", "NoSuchElementException");
        }

        // e) orElse is EAGER: expensiveDefault() runs even though the email exists
        System.out.println("--- orElse (eager) ---");
        String eager = findUser("1").map(User::email).orElse(expensiveDefault());
        show("result", eager);

        // f) isPresent + get: Optional used like a null check
        Optional<User> maybe = findUser("1");
        if (maybe.isPresent()) {
            show("isPresent + get", maybe.get().name());
        }

        // ------------------------------------------------------------
        // 2. Fixed / stream version
        // ------------------------------------------------------------

        // a) Creating
        show("of", Optional.of("value"));
        show("ofNullable(null)", Optional.ofNullable((String) null));
        show("empty", Optional.empty());

        // b) orElseGet is LAZY: runs only when the Optional is empty
        System.out.println("--- orElseGet (lazy), Alice has an email ---");
        show("Alice", findUser("1").map(User::email).orElseGet(P16_OptionalDemo::expensiveDefault));
        System.out.println("--- orElseGet (lazy), Bob has no email ---");
        show("Bob", findUser("2").map(User::email).orElseGet(P16_OptionalDemo::expensiveDefault));

        // c) orElse is fine for cheap constants
        show("orElse constant", findUser("9").map(User::name).orElse("unknown"));

        // d) orElseThrow
        try {
            findUser("9").orElseThrow(() -> new IllegalArgumentException("User 9 not found"));
        } catch (IllegalArgumentException e) {
            show("orElseThrow", e.getMessage());
        }
        show("orElseThrow (no argument, Java 10)", findUser("1").map(User::name).orElseThrow());

        // e) map: a null result becomes an empty Optional (Bob has no email)
        show("map to uppercase email", findUser("1").map(User::email).map(String::toUpperCase).orElse("NO EMAIL"));
        show("map on Bob", findUser("2").map(User::email).map(String::toUpperCase).orElse("NO EMAIL"));

        // f) flatMap: when the function itself returns an Optional
        show("flatMap domain", findUser("1").map(User::email).flatMap(P16_OptionalDemo::domainOf));
        show("map would nest", findUser("1").map(User::email).map(P16_OptionalDemo::domainOf));

        // g) filter
        show("filter name starts with A", findUser("1").filter(x -> x.name().startsWith("A")).isPresent());
        show("filter name starts with Z", findUser("1").filter(x -> x.name().startsWith("Z")).isPresent());

        // h) ifPresent and ifPresentOrElse (Java 9)
        findUser("1").ifPresent(x -> System.out.println("ifPresent : " + x.name()));
        findUser("9").ifPresentOrElse(
                x -> System.out.println("found " + x.name()),
                () -> System.out.println("ifPresentOrElse : user 9 not found"));

        // i) or: fall back to another Optional (Java 9)
        show("or", findUser("9").or(() -> findUser("1")).map(User::name).orElse("none"));

        // j) stream: turn an Optional into a stream of 0 or 1 elements (Java 9)
        List<String> names = List.of("1", "9", "2").stream()
                .map(P16_OptionalDemo::findUser)
                .flatMap(Optional::stream)
                .map(User::name)
                .collect(Collectors.toList());
        show("existing users", names);

        // k) isEmpty (Java 11) and equality
        show("isEmpty", findUser("9").isEmpty());
        show("equals compares content", Optional.of("a").equals(Optional.of("a")));

        // l) Primitive optionals: no boxing
        OptionalInt first = java.util.stream.IntStream.of(5, 3).findFirst();
        show("OptionalInt", first.getAsInt());

        // m) Guidelines (comments only):
        //    - Use Optional as a RETURN type for "may have no result".
        //    - Do not use it for fields, method parameters or collection elements.
        //    - Never return null from a method that returns Optional.
        //    - Avoid isPresent() + get(), use map / orElse / ifPresent instead.
    }
}