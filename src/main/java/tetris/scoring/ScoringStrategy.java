package tetris.scoring;

/**
 * Strategy pattern: decides how many points a line clear is worth.
 * Swapping the strategy passed to {@code GameController} changes the
 * scoring rules without touching any game logic.
 */
public interface ScoringStrategy {

    /**
     * @param lineCount number of rows cleared at once (1-4)
     * @return points earned for clearing that many lines in one move
     */
    int scoreForLines(int lineCount);
}
