package tetris.scoring;

/**
 * The default scoring rules used by the game today: 100 / 300 / 500 / 800
 * points for clearing 1, 2, 3, or 4 lines in one move.
 */
public final class StandardScoringStrategy implements ScoringStrategy {

    @Override
    public int scoreForLines(int lineCount) {
        return switch (lineCount) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            case 4 -> 800;
            default -> 0;
        };
    }
}
