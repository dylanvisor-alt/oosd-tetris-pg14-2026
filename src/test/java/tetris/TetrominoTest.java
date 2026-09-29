package tetris;

import org.junit.jupiter.api.Test;
import tetris.model.Movable;
import tetris.model.Tetromino;
import tetris.model.shapes.IPiece;
import tetris.model.shapes.OPiece;
import tetris.model.shapes.TPiece;
import javafx.scene.paint.Color;

import static org.junit.jupiter.api.Assertions.*;

class TetrominoTest {
    private static final Color TEST_COLOUR = Color.web("#007aff");

    @Test
    void storesStartingPositionAndColour() {
        Tetromino piece = new TPiece(4, 0, TEST_COLOUR);

        assertEquals(4, piece.getX());
        assertEquals(0, piece.getY());
        assertEquals(TEST_COLOUR, piece.getColor());
    }

    @Test
    void moveLeftDecreasesX() {
        Tetromino piece = new TPiece(4, 0, TEST_COLOUR);

        piece.moveLeft();

        assertEquals(3, piece.getX());
    }

    @Test
    void moveRightIncreasesX() {
        Tetromino piece = new TPiece(4, 0, TEST_COLOUR);

        piece.moveRight();

        assertEquals(5, piece.getX());
    }

    @Test
    void moveDownIsSharedByEveryShapeByAbstractBaseClass() {
        Movable piece = new TPiece(4, 0, TEST_COLOUR);

        piece.moveDown();

        assertEquals(1, ((Tetromino) piece).getY());
    }

    @Test
    void tPieceCyclesThroughAllRotationStates() {
        TPiece piece = new TPiece(0, 0, TEST_COLOUR);

        int[][] state0 = piece.getShape();
        piece.rotate();
        int[][] state1 = piece.getShape();
        piece.rotate();
        int[][] state2 = piece.getShape();
        piece.rotate();
        int[][] state3 = piece.getShape();
        piece.rotate();

        assertArrayEquals(new int[][] {{0, 1, 0}, {1, 1, 1}}, state0);
        assertArrayEquals(new int[][] {{1, 0}, {1, 1}, {1, 0}}, state1);
        assertArrayEquals(new int[][] {{1, 1, 1}, {0, 1, 0}}, state2);
        assertArrayEquals(new int[][] {{0, 1}, {1, 1}, {0, 1}}, state3);
        assertArrayEquals(state0, piece.getShape());
    }

    @Test
    void oPieceShapeNeverChangesOnRotate() {
        OPiece piece = new OPiece(0, 0, TEST_COLOUR);
        int[][] expected = {{1, 1}, {1, 1}};

        assertArrayEquals(expected, piece.getShape());
        piece.rotate();
        assertArrayEquals(expected, piece.getShape());
    }

    @Test
    void iPieceOnlyHasTwoRotationStates() {
        IPiece piece = new IPiece(0, 0, TEST_COLOUR);

        assertArrayEquals(new int[][] {{1, 1, 1, 1}}, piece.getShape());
        piece.rotate();
        assertArrayEquals(new int[][] {{1}, {1}, {1}, {1}}, piece.getShape());
        piece.rotate();
        assertArrayEquals(new int[][] {{1, 1, 1, 1}}, piece.getShape());

    }
}