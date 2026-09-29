package tetris;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.engine.support.discovery.SelectorResolver;
import tetris.persistence.JsonFileStore;
import tetris.stats.MatchRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonFileStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void writesAndReadsBackTheSameRecords() throws IOException {
        JsonFileStore<MatchRecord> store = new JsonFileStore<>(tempDir.resolve("history.jsonl"));
        MatchRecord original = new MatchRecord(1200, 10, 20, Instant.parse("2026-09-30T00:00:00Z"));

        store.append(original, MatchRecord::toJson);
        List<MatchRecord> loaded = store.readAll(MatchRecord::fromJson);

        assertEquals(1, loaded.size());
        assertEquals(original, loaded.get(0));
    }
}
