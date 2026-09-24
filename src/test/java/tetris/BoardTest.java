package tetris;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tetris.model.Board;
import tetris.model.shapes.OPiece;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    @ParameterizedTest(name = "an O-piece at ({0}, {1}) fits: {2}")
    @MethodSource("piecePlacements")
    void checksPiecePlacementAtBoundaries(int x, int y, boolean expectedToFit) {
        Board board = new Board();
        OPiece piece = new OPiece(0, 0, Color.YELLOW);

        assertEquals(expectedToFit, board.canPlace(piece, x, y));
    }

    private static Stream<Arguments> piecePlacements() {
        return Stream.of(
                Arguments.of(-1, 0, false),
                Arguments.of(0, -1, false),
                Arguments.of(0, 0, true),
                Arguments.of(Board.WIDTH - 2, Board.HEIGHT - 2, true),
                Arguments.of(Board.WIDTH - 1, 0, false),
                Arguments.of(0, Board.HEIGHT - 1, false)
        );
    }

    @Test
    void doesNotPlaceAPieceOverALockedCell() {
        Board board = new Board();
        board.setCell(4, 4, 1);

        assertFalse(board.canPlace(new OPiece(0, 0, Color.YELLOW), 4, 4));
    }

    @Test
    void locksEveryFilledCellOfAPiece() {
        Board board = new Board();
        OPiece piece = new OPiece(3, 5, Color.YELLOW);

        board.lockPiece(piece);

        assertTrue(board.isCellOccupied(5, 3));
        assertTrue(board.isCellOccupied(5, 4));
        assertTrue(board.isCellOccupied(6, 3));
        assertTrue(board.isCellOccupied(6, 4));
        assertFalse(board.isCellOccupied(4, 3));
    }

    @Test
    void clearsAdjacentRowsAndShiftsTheRemainingRowsDown() {
        Board board = new Board();
        for (int column = 0; column < Board.WIDTH; column++) {
            board.setCell(Board.HEIGHT - 1, column, 1);
            board.setCell(Board.HEIGHT - 2, column, 1);
        }
        board.setCell(Board.HEIGHT - 3, 4, 1);

        assertEquals(2, board.clearFullRows().size());
        assertEquals(1, board.getCell(Board.HEIGHT - 1, 4));
        assertEquals(0, board.getCell(Board.HEIGHT - 2, 4));
    }
}
