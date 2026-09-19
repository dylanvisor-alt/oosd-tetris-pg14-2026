package tetris;

import org.junit.jupiter.api.Test;
import tetris.model.Board;
import tetris.model.Tetromino;
import tetris.model.shapes.OPiece;
import javafx.scene.paint.Color;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    private static final Color TEST_COLOUR = Color.web("00bcd4");

    @Test
    void outOfBoundsCellsAreTreatedAsOccupied() {
        Board board = new Board();

        assertTrue(board.isCellOccupied(-1, 0));
        assertTrue(board.isCellOccupied(0,-1));
        assertTrue(board.isCellOccupied(Board.HEIGHT, 0));
        assertTrue(board.isCellOccupied(0, Board.WIDTH));
    }

    @Test
    void emptyBoardHasNoOccupiedCells() {

    }

    @Test
    void setCellMarksThatCellOccupied() {

    }

    @Test
    void setCellIgnoresOutOfBoundsWritesInsteadOfThrowing() {

    }

    @Test
    void canPlaceIsFalseWhenOverlappingLockedCells() {

    }

    @Test
    void canPlaceIsFalseBeyondTheFloor() {

    }

    @Test
    void canPlaceIsFalseBeyondTheSideWalls() {

    }

    @Test
    void lockPieceFillsEveryCellTheShapeOccupies() {

    }

    @Test
    void eachCellFilledVisitsOnlyTheFilledShapeCells() {

    }

    @Test
    void clearFullRowsRemovesASingleCompletedRow() {

    }

    @Test
    void clearFullRowsLeavesPartiallyFilledRowsInPlace() {
        Board board = new Board();
        board.setCell(Board.HEIGHT - 1, 0, 1);

        List<Integer> clearedRows = board.clearFullRows();

        assertTrue(clearedRows.isEmpty());
        assertTrue(board.isCellOccupied(Board.HEIGHT - 1, 0));
    }

    @Test
    void clearFullRowsShiftsRemainingBlocksDown() {
        Board board = new Board();
        board.setCell(Board.HEIGHT -2, 0, 1);

        fillRow(board, Board.HEIGHT - 1);

        board.clearFullRows();

        assertTrue(board.isCellOccupied(Board.HEIGHT - 1, 0));
        assertTrue(board.isCellOccupied(Board.HEIGHT - 2, 0));
    }

    @Test
    void clearFullRowsHandlesMultipleCompletedRowsAtOnce() {
        Board board = new Board();
        fillRow(board, Board.HEIGHT - 1);
        fillRow(board, Board.HEIGHT - 2);

        List<Integer> clearedRows = board.clearFullRows();

        assertEquals(2, clearedRows.size());
        for (int col = 0; col < Board.WIDTH; col++) {
            assertFalse(board.isCellOccupied(Board.HEIGHT - 1, col));
            assertFalse(board.isCellOccupied(Board.HEIGHT - 2, col));
        }
    }

    private void fillRow(Board board, int row) {
        for (int col = 0; col < Board.WIDTH; col++) {
            board.setCell(row, col,1);
        }
    }
}