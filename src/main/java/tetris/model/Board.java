package tetris.model;

public class Board {

    public static final int WIDTH = 10;
    public static final int HEIGHT = 20;

    /* -------------------------------------------------------------------- */
    /*  the grid itself - 0 = empty, 1 = occupied                          */
    /* -------------------------------------------------------------------- */
    private final int[][] grid;
    private final int width;
    private final int height;

    public Board() {
        this(WIDTH, HEIGHT);
    }

    public Board(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    /* -------------------------------------------------------------------- */
    /*  basic cell access                                                  */
    /* -------------------------------------------------------------------- */

    // treats out-of-bounds as "occupied" too, so walls/floor act like
    // collisions without needing separate boundary checks everywhere else
    public boolean isCellOccupied(int row, int col) {
        if (row < 0 || row >= height || col < 0 || col >= width) {
            return true;
        }
        return grid[row][col] != 0;
    }

    public void setCell(int row, int col, int value) {
        if (row >= 0 && row < height && col >= 0 && col < width) {
            grid[row][col] = value;
        }
    }

    public int getCell(int row, int col) {
        return grid[row][col];
    }

    /* -------------------------------------------------------------------- */
    /*  collision checking and locking pieces in                           */
    /* -------------------------------------------------------------------- */

    public void eachCellFilled(Tetromino piece, int atX, int atY, java.util.function.BiConsumer<Integer, Integer> action) {
        int[][] shape = piece.getShape();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    action.accept(atY + row, atX + col);
                }
            }
        }
    }

    // remove repeated logic in canPlace, lockPiece, GameController.saveCurrentPieceColours

    public boolean canPlace(Tetromino piece, int newX, int newY) {
        boolean[] fits = { true };
        eachCellFilled(piece, newX, newY, (boardRow, boardCol) -> {
            if (isCellOccupied(boardRow, boardCol)) {
                fits[0] = false;
            }
        });
        return fits[0];
    }

    public void lockPiece(Tetromino piece) {
        eachCellFilled(piece, piece.getX(), piece.getY(), (boardRow, boardCol) ->
                setCell(boardRow, boardCol, 1));
    }

    /* -------------------------------------------------------------------- */
    /*  clearing full rows                                                 */
    /* -------------------------------------------------------------------- */

    // scans from the bottom up, removing any fully filled rows
    // returns how many rows were cleared, used for scoring
    // returns the row indices that were cleared, so callers can keep other
    // per-cell data (like colours) in sync without re-scanning independently

    public java.util.List<Integer> clearFullRows() {
        java.util.List<Integer> clearedRows = new java.util.ArrayList<>();

        for (int row = height - 1; row >= 0; row--) {
            if (isRowFull(row)) {
                removeRow(row);
                clearedRows.add(row);
                row++;
            }
        }
        return clearedRows;
    }

    // enhanced for-loop: walk every value in this row directly, no need
    // for a column index since we're only ever reading, not writing
    private boolean isRowFull(int row) {
        for (int cell : grid[row]) {
            if (cell == 0) {
                return false;
            }
        }
        return true;
    }

    // shifts every row above the cleared one down by one, then clears the top row
    private void removeRow(int rowToRemove) {
        for (int row = rowToRemove; row > 0; row--) {
            grid[row] = grid[row - 1].clone();
        }
        grid[0] = new int[width];
    }
}