package tetris;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tetris.model.Movable;
import tetris.model.Tetromino;
import tetris.model.TetrominoFactory;
import tetris.model.shapes.IPiece;
import tetris.model.shapes.JPiece;
import tetris.model.shapes.LPiece;
import tetris.model.shapes.OPiece;
import tetris.model.shapes.SPiece;
import tetris.model.shapes.TPiece;
import tetris.model.shapes.ZPiece;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TetrominoTest {
    private static final Color TEST_COLOUR = Color.web("#007aff");

    // --- individual, per-piece checks ---

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

    // --- parameterized, table-driven checks across every piece type ---

    @ParameterizedTest(name = "{0} contains four cells and returns after {2} rotations")
    @MethodSource("tetrominoes")
    void preservesFourBlocksAndCompletesItsRotationCycle(String name, Tetromino piece, int rotationCycle) {
        int[][] originalShape = copyOf(piece.getShape());

        assertEquals(4, filledCellCount(originalShape));

        for (int rotation = 0; rotation < rotationCycle; rotation++) {
            piece.rotate();
            assertEquals(4, filledCellCount(piece.getShape()));
        }

        assertTrue(Arrays.deepEquals(originalShape, piece.getShape()));
    }

    @ParameterizedTest(name = "{0} moves left, right, and down")
    @MethodSource("tetrominoes")
    void movesInEachSupportedDirection(String name, Tetromino piece, int ignoredRotationCycle) {
        int startX = piece.getX();
        int startY = piece.getY();

        piece.moveLeft();
        piece.moveRight();
        piece.moveDown();

        assertEquals(startX, piece.getX());
        assertEquals(startY + 1, piece.getY());
    }

    @ParameterizedTest(name = "factory piece {index} starts at the standard spawn point")
    @MethodSource("factoryPieces")
    void factoryCreatesAValidPieceAtTheSpawnPoint(Tetromino piece) {
        assertEquals(4, piece.getX());
        assertEquals(0, piece.getY());
        assertEquals(4, filledCellCount(piece.getShape()));
    }

    private static Stream<Arguments> tetrominoes() {
        return Stream.of(
                Arguments.of("I", new IPiece(4, 0, Color.CYAN), 2),
                Arguments.of("O", new OPiece(4, 0, Color.YELLOW), 1),
                Arguments.of("T", new TPiece(4, 0, Color.PURPLE), 4),
                Arguments.of("S", new SPiece(4, 0, Color.GREEN), 2),
                Arguments.of("Z", new ZPiece(4, 0, Color.RED), 2),
                Arguments.of("J", new JPiece(4, 0, Color.BLUE), 4),
                Arguments.of("L", new LPiece(4, 0, Color.ORANGE), 4)
        );
    }

    private static Stream<Tetromino> factoryPieces() {
        return Stream.generate(TetrominoFactory::createRandomPiece).limit(20);
    }

    private static int[][] copyOf(int[][] shape) {
        return Arrays.stream(shape).map(int[]::clone).toArray(int[][]::new);
    }

    private static int filledCellCount(int[][] shape) {
        return Arrays.stream(shape)
                .flatMapToInt(Arrays::stream)
                .sum();
    }
}