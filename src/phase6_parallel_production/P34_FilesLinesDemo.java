package phase6_parallel_production;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Files.lines(path): a lazy stream of lines that holds a file open
 *   - try-with-resources closes the stream (and the file)
 *   - Finish the work INSIDE the try block
 *
 * PROBLEM:
 *   Files.lines keeps the file open until the stream is closed. If you forget to
 *   close it, the file handle leaks. If you return the stream out of the try block,
 *   it is already closed when the caller uses it.
 *
 * SOLUTION:
 *   Open it in try-with-resources and return a RESULT (a List, a count), not the stream.
 */
public class P34_FilesLinesDemo {

    static Stream<String> brokenRead(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path)) {
            return lines;                       // closed as soon as the method returns
        }
    }

    static List<String> goodRead(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .filter(line -> !line.isBlank())
                    .skip(1)                    // skip the header
                    .map(line -> line.split(",")[1])
                    .toList();
        }
    }

    public static void main(String[] args) throws IOException {
        Path path = Files.createTempFile("streams-demo", ".csv");
        path.toFile().deleteOnExit();
        Files.write(path, List.of("id,name", "1,Alice", "", "2,Bob", "3,Charlie"));

        // 1. Broken version
        // long count = Files.lines(path).count();    // works, but the file stays open
        try {
            brokenRead(path).count();
        } catch (IllegalStateException e) {
            System.out.println("returned stream : IllegalStateException (already closed)");
        }

        // 2. Fixed version
        System.out.println("names : " + goodRead(path));

        try (Stream<String> lines = Files.lines(path)) {
            System.out.println("first data line : " + lines.skip(1).findFirst().orElse("none"));
        }

        try (Stream<String> lines = Files.lines(path)) {
            System.out.println("non-blank lines : " + lines.filter(l -> !l.isBlank()).count());
        }
    }
}