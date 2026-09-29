package tetris.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/* threads, json, networking, file i/o, generics */

/**
 * simple append-only JSON-lines store, generic
 * keeps the file I/O logic seperate from knowing about MatchRecord
 * @param <T> the type of record being stored
 */

public final class JsonFileStore<T> {

    private final Path filePath;

    public JsonFileStore(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "filePath");
    }

    public void append(T record, Function<T, String> toJson) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String line = toJson.apply(record) + System.lineSeparator();
        Files.writeString(filePath, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public List<T> readAll(Function<String, T> fromJson) throws IOException {
        if (!Files.exists(filePath)) {
            return List.of();
        }

        List<T> records = new ArrayList<>();
        for (String line : Files.readAllLines(filePath)) {
            if (line.isBlank()) {
                continue;
            }
            try {
                records.add(fromJson.apply(line));
            } catch (RuntimeException malformedLine) {
                // skip a corrupted line
            }
        }
        return records;
    }

}
