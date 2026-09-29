package tetris.controller;

import tetris.model.ScoreEntry;
import tetris.persistence.ScoreRepository;
import tetris.persistence.SQLiteScoreRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Coordinates high-score storage without coupling JavaFX screens to SQLite. */
public class HighScoreController {

    private static final int HIGH_SCORE_LIMIT = 10;

    private final ScoreRepository scoreRepository;

    /** Uses the application's local SQLite database. */
    public HighScoreController() {
        this(new SQLiteScoreRepository(SQLiteScoreRepository.defaultDatabasePath()));
    }

    public HighScoreController(ScoreRepository scoreRepository) {
        this.scoreRepository = Objects.requireNonNull(scoreRepository, "scoreRepository");
    }

    public List<ScoreEntry> getTopScores() {
        List<ScoreEntry> scores = scoreRepository.findTopScores(HIGH_SCORE_LIMIT);

        // Stream: run the repository's rows through a pipeline instead of
        // returning them as-is. Comparator: highest score first, ties broken
        // alphabetically by name so the displayed order is always deterministic.
        // (No distinct() here on purpose: two different games can legitimately
        // produce the same player name + score, and de-duping on those two
        // fields would silently drop real high scores.)
        return scores.stream()
                .sorted(Comparator.comparingInt(ScoreEntry::score)
                        .reversed()
                        .thenComparing(ScoreEntry::playerName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public void saveScore(String playerName, int score) {
        Objects.requireNonNull(playerName, "playerName");
        scoreRepository.save(new ScoreEntry(playerName.trim(), score));
    }
}
