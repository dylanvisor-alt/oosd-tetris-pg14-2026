package tetris.ui;

import tetris.model.Board;
import tetris.model.Tetromino;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import tetris.util.Constants;

import java.util.Objects;
import java.util.function.IntConsumer;

/* -------------------------------------------------------------------- */
/*  javafx view for gameplay - draws state given by GameController      */
/*  but does not decide whether a move is legal or change the Board     */
/* -------------------------------------------------------------------- */

public class GameScreen {

    public static final int DEFAULT_CELL_SIZE = 28;
    public static final int MAX_BOARD_PIXEL_WIDTH = 280;
    public static final int MAX_BOARD_PIXEL_HEIGHT = 560;
    private static final Color EMPTY_COLOUR = Color.web("#090d21");
    private static final Color GRID_COLOUR = Color.web("#293462");

    private final BorderPane root = new BorderPane();
    private final Rectangle[][] lockedCellViews;
    private final int boardWidth;
    private final int boardHeight;
    private final int cellSize;
    private final Pane activePieceLayer = new Pane();
    private final PauseOverlay pauseOverlay = new PauseOverlay();
    private final Label scoreLabel = new Label("Score: 0");
    private final Label statusLabel = new Label();
    private final Button restartButton = new Button("Restart");
    private final Button exitButton = new Button("Exit to Menu");
    private final Button saveScoreButton = new Button("Save Score");

    private Group activePieceView;
    private int finalScore;

    public GameScreen(int boardWidth, int boardHeight, Runnable onRestart, IntConsumer onSaveScore, Runnable menuExitButton) {


        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.cellSize = Math.min(DEFAULT_CELL_SIZE, Math.min(MAX_BOARD_PIXEL_WIDTH / boardWidth, MAX_BOARD_PIXEL_HEIGHT / boardHeight));
        this.lockedCellViews = new Rectangle[boardHeight][boardWidth];
        Objects.requireNonNull(onRestart, "onRestart");
        Objects.requireNonNull(onSaveScore, "onSaveScore");

        /* -------------------------------------------------------------------- */
        /*  build the board itself - locked grid, active piece layer, pause     */
        /* -------------------------------------------------------------------- */
        GridPane lockedGrid = createLockedGrid();
        int boardPixelWidth = boardWidth * cellSize;
        int boardPixelHeight = boardHeight * cellSize;

        activePieceLayer.setMinSize(boardPixelWidth, boardPixelHeight);
        activePieceLayer.setPrefSize(boardPixelWidth, boardPixelHeight);
        activePieceLayer.setMaxSize(boardPixelWidth, boardPixelHeight);
        activePieceLayer.setMouseTransparent(true);

        pauseOverlay.setMinSize(boardPixelWidth, boardPixelHeight);
        pauseOverlay.setPrefSize(boardPixelWidth, boardPixelHeight);
        pauseOverlay.setMaxSize(boardPixelWidth, boardHeight);

        // stacked on top of each other: locked blocks at the bottom, the
        // falling piece above that, the pause overlay above everything

        StackPane boardStack = new StackPane(lockedGrid, activePieceLayer, pauseOverlay);
        boardStack.setAlignment(Pos.TOP_LEFT);
        boardStack.setMinSize(boardPixelWidth, boardPixelHeight);
        boardStack.setPrefSize(boardPixelWidth, boardPixelHeight);
        boardStack.setMaxSize(boardPixelWidth, boardPixelHeight);
        boardStack.getStyleClass().add("game-board-frame");

        /* -------------------------------------------------------------------- */
        /*  bottom bar - score/status on top, controls centered below           */
        /* -------------------------------------------------------------------- */

        VBox bottomBar = createBottomBar();

        /* -------------------------------------------------------------------- */
        /*  put the board and bottom bar into one window                        */
        /* -------------------------------------------------------------------- */

        StackPane boardArea = new StackPane(boardStack);
        boardArea.setAlignment(Pos.CENTER);
        boardArea.setPadding(new Insets(10, 130, 10, 130));
        boardArea.setMinWidth(Constants.APP_WIDTH);
        boardArea.setPrefWidth(Constants.APP_WIDTH);
        boardArea.getStyleClass().add("game-board-area");

        root.setCenter(boardArea);
        root.setBottom(bottomBar);
        root.setMinSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
        root.setPrefSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
        root.getStyleClass().add("game-background");
        root.setFocusTraversable(true);

        restartButton.setOnAction(event -> onRestart.run());
        restartButton.setVisible(false);
        restartButton.setManaged(false);
        restartButton.getStyleClass().add("game-action-button");

        exitButton.setOnAction(event -> menuExitButton.run());
        exitButton.setVisible(false);
        exitButton.setManaged(false);
        exitButton.getStyleClass().addAll("game-action-button", "exit-menu-button");

        saveScoreButton.setOnAction(event -> onSaveScore.accept(finalScore));
        saveScoreButton.setVisible(false);
        saveScoreButton.setManaged(false);
        saveScoreButton.getStyleClass().add("game-action-button");
    }

    // score/status sit on their own row, with the controls hint centered
    // underneath on a second row - keeps the whole bar narrow instead of
    // stretching everything out sideways

    private VBox createBottomBar() {
        scoreLabel.getStyleClass().add("game-score");

        statusLabel.getStyleClass().add("game-status");
        statusLabel.setWrapText(true);

        HBox scoreRow = new HBox(16, scoreLabel, statusLabel, restartButton, saveScoreButton, exitButton);
        scoreRow.setAlignment(Pos.CENTER);

        Label controlsLabel = new Label(
                "\u2190 \u2192 Move  |  \u2191 Rotate  \n \u2193 Soft Drop  |  Space - Hard Drop  |  P - Pause  |  M - Music  |  S - Sound");
        controlsLabel.getStyleClass().add("game-controls");
        controlsLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        controlsLabel.setAlignment(Pos.CENTER);
        controlsLabel.setMaxWidth(Double.MAX_VALUE);

        VBox bottomBar = new VBox(6, scoreRow, controlsLabel);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(10));
        bottomBar.getStyleClass().add("game-bottom-bar");
        return bottomBar;
    }

    private GridPane createLockedGrid() {
        GridPane grid = new GridPane();
        for (int row = 0; row < boardHeight; row++) {
            grid.getRowConstraints().add(new RowConstraints(cellSize));
        }
        for (int col = 0; col < boardWidth; col++) {
            grid.getColumnConstraints().add(new ColumnConstraints(cellSize));
        }

        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                Rectangle cell = new Rectangle(cellSize, cellSize, EMPTY_COLOUR);
                cell.setStroke(GRID_COLOUR);
                lockedCellViews[row][col] = cell;
                grid.add(cell, col, row);
            }
        }

        int boardPixelWidth = boardWidth * cellSize;
        int boardPixelHeight = boardHeight * cellSize;
        grid.setMinSize(boardPixelWidth, boardPixelHeight);
        grid.setPrefSize(boardPixelWidth, boardPixelHeight);
        grid.setMaxSize(boardPixelWidth, boardPixelHeight);
        return grid;
    }

    /* -------------------------------------------------------------------- */
    /*  hooks that GameController uses to talk to this screen               */
    /* -------------------------------------------------------------------- */

    public BorderPane getRoot() {
        return root;
    }

    public int getCellSize() {
        return cellSize;
    }

    public void setKeyHandler(EventHandler<KeyEvent> keyHandler) {
        root.setOnKeyPressed(keyHandler);
    }

    public void requestKeyboardFocus() {
        root.requestFocus();
    }

    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    public void showStatus(String status) {
        statusLabel.setText(status);
    }

    public void showGameOver(int finalScore) {
        this.finalScore = finalScore;
        statusLabel.setText("GAME OVER");
        restartButton.setManaged(true);
        restartButton.setVisible(true);
        saveScoreButton.setManaged(true);
        saveScoreButton.setVisible(true);
        exitButton.setManaged(true);
        exitButton.setVisible(true);
    }

    public void showPauseOverlay(boolean paused) {
        pauseOverlay.setPaused(paused);
        exitButton.setVisible(paused);
        exitButton.setManaged(paused);
    }

    /* -------------------------------------------------------------------- */
    /*  drawing the board and the falling piece                             */
    /* -------------------------------------------------------------------- */

    // repaints the fixed blocks and recreates the currently falling piece
    public void render(Board board, Color[][] lockedColors, Tetromino currentPiece) {
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                lockedCellViews[row][col].setFill(
                        board.isCellOccupied(row, col) ? lockedColors[row][col] : EMPTY_COLOUR);
            }
        }

        activePieceLayer.getChildren().clear();
        activePieceView = null;
        if (currentPiece == null) {
            return;
        }

        activePieceView = makePieceView(currentPiece);
        activePieceLayer.getChildren().add(activePieceView);
    }

    private Group makePieceView(Tetromino piece) {
        Group pieceView = new Group();
        int[][] shape = piece.getShape();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    Rectangle cell = new Rectangle(cellSize, cellSize, piece.getColor());
                    cell.setStroke(GRID_COLOUR);
                    cell.setX(col * cellSize);
                    cell.setY(row * cellSize);
                    pieceView.getChildren().add(cell);
                }
            }
        }

        pieceView.setLayoutX(piece.getX() * cellSize);
        pieceView.setLayoutY(piece.getY() * cellSize);
        return pieceView;
    }

    // moves only the falling piece between its two logical board rows
    public void setActivePieceVerticalOffset(double pixelOffset) {
        if (activePieceView != null) {
            activePieceView.setTranslateY(pixelOffset);
        }
    }
}
