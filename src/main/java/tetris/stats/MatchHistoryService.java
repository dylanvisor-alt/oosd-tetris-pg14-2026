package tetris.stats;

import tetris.persistence.JsonFileStore;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/* stores match results in local history file on background thread, so slow disk never stalls application thread when game-over screen attempting to show up */

public final class MatchHistoryService {

    private static final Path HISTORY_FILE = Path.of(
            System.getProperty("user.home"), ".oosd-tetris", "match-history.jsonl"
    );

    private final JsonFileStore<MatchRecord> store = new JsonFileStore<>(HISTORY_FILE);

    private final ExecutorService writerThread = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "match-history-writer");
        thread.setDaemon(true);
        return thread;
    });

    public void recordMatchAsync(int score, int boardWidth, int boardHeight) {
        MatchRecord record = new MatchRecord(score, boardWidth,boardHeight, Instant.now());
        writerThread.submit(() -> {
            try {
                store.append(record, MatchRecord::toJson);
            } catch (IOException writeFailed) {
                System.err.println("could not save match history: " + writeFailed.getMessage());
            }
        });
    }

    public List<MatchRecord> readHistory() throws IOException {
        return store.readAll(MatchRecord::fromJson);
    }

    public void shutdown() {
        writerThread.shutdown();
        try {
            writerThread.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

}
