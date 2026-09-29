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
        Board board = new Board();

        for (int row = 0; row < Board.HEIGHT; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                assertFalse(board.isCellOccupied(row, col));
            }
        }
    }

    @Test
    void setCellMarksThatCellOccupied() {
        Board board = new Board();

        board.setCell(5, 5, 1);

        assertTrue(board.isCellOccupied(5, 5));
        assertEquals(1, board.getCell(5, 5));
    }

    @Test
    void setCellIgnoresOutOfBoundsWritesInsteadOfThrowing() {
        Board board = new Board();

        assertDoesNotThrow(() -> {
            board.setCell(-1, 0, 1);
            board.setCell(0, Board.WIDTH, 1);
        });

        assertFalse(board.isCellOccupied(0, 0));
    }

    @Test
    void canPlaceIsFalseWhenOverlappingLockedCells() {
        Board board = new Board();
        Tetromino piece = new OPiece(0, 0, TEST_COLOUR);

        assertTrue(board.canPlace(piece, 4, 0));
    }

    @Test
    void canPlaceIsFalseBeyondTheFloor() {
        Board board = new Board();
        Tetromino piece = new OPiece(0, 0, TEST_COLOUR);

        assertFalse(board.canPlace(piece, 4, - 1));
    }

    @Test
    void canPlaceIsFalseBeyondTheSideWalls() {
        Board board = new Board();
        Tetromino piece = new OPiece(0, 0, TEST_COLOUR);

        assertFalse(board.canPlace(piece, -1, 0));
        assertFalse(board.canPlace(piece, Board.WIDTH - 1, 0));
    }

    @Test
    void lockPieceFillsEveryCellTheShapeOccupies() {
        Board board = new Board();
        Tetromino piece = new OPiece(0, 0, TEST_COLOUR);

        board.lockPiece(piece);

        assertTrue(board.isCellOccupied(0, 0));
        assertTrue(board.isCellOccupied(0, 1));
        assertTrue(board.isCellOccupied(1, 0));
        assertTrue(board.isCellOccupied(1, 1));
    }

    @Test
    void eachCellFilledVisitsOnlyTheFilledShapeCells() {
        Board board = new Board();
        Tetromino piece = new OPiece(0, 0, TEST_COLOUR);
        List<int[]> visited = new java.util.ArrayList<>();

        board.eachCellFilled(piece, 2, 3, (row, col) -> visited.add(new int[] {row, col}));

        assertEquals(4, visited.size());
    }

    @Test
    void clearFullRowsRemovesASingleCompletedRow() {
        Board board = new Board();
        board.setCell(Board.HEIGHT - 1, 0, 1);

        List<Integer> clearedRows = board.clearFullRows();

        assertTrue(board.isCellOccupied(Board.HEIGHT -1, 0));
        assertFalse(board.isCellOccupied(Board.HEIGHT - 2, 0));
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