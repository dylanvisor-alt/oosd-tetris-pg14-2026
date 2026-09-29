package tetris.ai;

import tetris.model.Board;
import tetris.model.Tetromino;

import java.util.ArrayList;
import java.util.List;

// ai simulates dropping piece in every possible spot on board, scores the reuslt, and returns the placement with the highest score
// simulation experimentation model (heuristic)

public final class AiPlayer {

    // note negative weight punishes a trait, positive rewards it
    private static final double AGGREGATE_HEIGHT_WEIGHT = -0.51;
    private static final double LINES_CLEARED_WEIGHT = 0.76;
    private static final double HOLES_WEIGHT = -0.36;
    private static final double BUMPINESS_WEIGHT = -0.18;

    private static final int NO_LANDING = -1;

    private AiPlayer() {
    }

    public static AiMove chooseBestPlacement(Board board, Tetromino piece) {
        int[][] snapshot = snapshotOf(board);
        List<int[][]> shapesByRotation = shapesForEveryRotation(piece);

        double bestScore = Double.NEGATIVE_INFINITY;
        AiMove bestMove = new AiMove(0, piece.getX());

        for (int rotation = 0; rotation < shapesByRotation.size(); rotation++) {
            int[][] shape = shapesByRotation.get(rotation);
            int shapeWidth = shape[0].length;

            for (int x = -shapeWidth; x <= board.getWidth(); x++) {
                int landingRow = landingRowFor(snapshot, shape, x);
                if (landingRow == NO_LANDING) {
                    continue;
                }

                int[][] resultingGrid = deepCopy(snapshot);
                place(resultingGrid, shape, x, landingRow);
                double score = score(resultingGrid);

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = new AiMove(rotation, x);
                }
            }
        }
        return bestMove;
    }

    private static List<int[][]> shapesForEveryRotation(Tetromino piece) {
        List<int[][]> shapes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            shapes.add(piece.getShape());
            piece.rotate();
        }
        return shapes;
    }

    private static int[][] snapshotOf(Board board) {
        int[][] snapshot = new int[board.getHeight()][board.getWidth()];
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
            snapshot[row][col] = board.getCell(row, col);
            }
        }
        return snapshot;
    }

    private static int landingRowFor(int[][] snapshot, int[][] shape, int x) {
        int height = snapshot.length;
        int width = snapshot[0].length;

        if (!fits(snapshot, shape, x, 0, width, height)) {
            return NO_LANDING;
        }

        int row = 0;
        while (fits(snapshot, shape, x, row + 1, width, height)) {
            row++;
        }
        return row;
    }

    private static boolean fits(int[][] snapshot, int[][] shape, int x, int y, int width, int height) {
        for (int shapeRow = 0; shapeRow < shape.length; shapeRow++) {
            for (int shapeCol = 0; shapeCol < shape[shapeRow].length; shapeCol++) {
                if (shape[shapeRow][shapeCol] == 0) {
                    continue;
                }
                int boardRow = y + shapeRow;
                int boardCol = x + shapeCol;
                if (boardRow < 0 || boardRow >= height || boardCol < 0 || boardCol >= width) {
                    return false;
                }
                if (snapshot[boardRow][boardCol] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void place(int[][] grid, int[][] shape, int x, int y) {
        for (int shapeRow = 0; shapeRow < shape.length; shapeRow++) {
            for (int shapeCol = 0; shapeCol < shape[shapeRow].length; shapeCol++) {
                if (shape[shapeRow][shapeCol] == 1) {
                    grid[y + shapeRow][x + shapeCol] = 1;
                }
            }
        }
    }

    private static double score(int[][] grid) {
        int width = grid[0].length;
        int height = grid.length;

        int[] columnHeights = new int[width];
        int holes = 0;

        for (int col = 0; col < width; col++) {
            boolean seenBlock = false;
            for (int row = 0; row < height; row++) {
                if (grid[row][col] != 0) {
                    if (!seenBlock) {
                        columnHeights[col] = height - row;
                        seenBlock = true;
                    }
                } else if (seenBlock) {
                    holes++;
                }
            }
        }

        int aggregateHeight = 0;
        int bumpiness = 0;
        for (int col = 0; col < width; col++) {
            aggregateHeight += columnHeights[col];
            if (col > 0) {
                bumpiness += Math.abs(columnHeights[col] - columnHeights[col -1]);
            }
        }

        int linesCleared = 0;
        for (int row = 0; row < height; row++) {
            boolean full = true;
            for (int col = 0; col < width; col++) {
                if (grid[row][col] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                linesCleared++;
            }
        }

        return AGGREGATE_HEIGHT_WEIGHT * aggregateHeight
                + LINES_CLEARED_WEIGHT * linesCleared
                + HOLES_WEIGHT * holes
                + BUMPINESS_WEIGHT * bumpiness;
    }

    private static int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int row = 0; row < original.length; row++) {
            copy[row] = original[row].clone();
        }
        return copy;
    }
}

