package tetris.model;

public record ScoreEntry(String playerName, int score) {

    public ScoreEntry {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name must not be blank");
        }
        if (score < 0) {
            throw new IllegalArgumentException("Score must not be negative");
        }
    }
}
