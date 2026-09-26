package tetris.controller;

import tetris.audio.AudioManager;
import tetris.model.Board;
import tetris.model.GameState;
import tetris.model.Tetromino;
import tetris.model.TetrominoFactory;
import tetris.scoring.ScoringStrategy;
import tetris.scoring.StandardScoringStrategy;
import tetris.ui.GameScreen;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import tetris.network.ExternalPlayerClient;
import java.io.IOException;

import java.util.List;

/* -------------------------------------------------------------------- */
/*  owns the gameplay state and rules - GameScreen only draws the       */
/*  values it receives, it never decides anything itself                */
/* -------------------------------------------------------------------- */

public class GameController {

    /* -------------------------------------------------------------------- */
    /*  settings never changed while the game is running                    */
    /* -------------------------------------------------------------------- */

    private static final double NANO_TO_MS = 1_000_000.0;

    private static final double DROP_SPEED = 300.0;
    private static final double MAX_FRAME_GAP = 50.0;

    private static final Color lockedColour = Color.web("#007aff");

    /* -------------------------------------------------------------------- */
    /*  game state - changes constantly while playing                       */
    /* -------------------------------------------------------------------- */
    private final Board board = new Board();
    private final GameScreen gamesScreen;
    private final Color[][] lockedColours = new Color[Board.HEIGHT][Board.WIDTH];
    private final AnimationTimer gravityTimer;
    private final AudioManager audioManager;
    private final ScoringStrategy scoringStrategy;
    private final ExternalPlayerClient externalPlayerClient = new ExternalPlayerClient();
    private Tetromino nextPiece;
    private volatile long pieceNumber;

    private Tetromino currentPiece;
    private GameState gameState = GameState.RUNNING;
    private int score;
    private double lastFrameTimeMs;
    private double accumulatedFallMs;

    /* -------------------------------------------------------------------- */
    /*  starting the game                                                    */
    /* -------------------------------------------------------------------- */

    public void startGame() {
        audioManager.applyMusicSetting();
        gamesScreen.setKeyHandler(this::handleKeyPress);
        spawnPiece();
        render();
        if (gameState == GameState.RUNNING) {
            gravityTimer.start();
        }
        gamesScreen.requestKeyboardFocus();
    }

    /* -------------------------------------------------------------------- */
    /*  gravity - moves the piece down automatically over time              */
    /* -------------------------------------------------------------------- */

    private void updateGravity(double currentTimeMs) {
        if (gameState != GameState.RUNNING || currentPiece == null) {
            return;
        }

        if (lastFrameTimeMs == 0) {
            lastFrameTimeMs = currentTimeMs;
            return;
        }

        double frameDelta = Math.min(currentTimeMs - lastFrameTimeMs, MAX_FRAME_GAP);
        lastFrameTimeMs = currentTimeMs;

        if (!canCurrentPieceFall()) {
            lockClearAndSpawn();
            render();
            return;
        }

        accumulatedFallMs += frameDelta;
        if (accumulatedFallMs >= DROP_SPEED) {
            currentPiece.moveDown();
            accumulatedFallMs -= DROP_SPEED;

            if (!canCurrentPieceFall()) {
                accumulatedFallMs = 0;
                lockClearAndSpawn();
                render();
                return;
            }

            // re-anchor the javafx piece at its new logical row - per-frame
            // updates remain a single translate operation
            render();
        }

        double fallProgress = accumulatedFallMs / DROP_SPEED;
        gamesScreen.setActivePieceVerticalOffset(fallProgress * GameScreen.CELL_SIZE);
    }

    private boolean canCurrentPieceFall() {
        return board.canPlace(currentPiece, currentPiece.getX(), currentPiece.getY() + 1);
    }

    /* -------------------------------------------------------------------- */
    /*  keyboard controls                                                   */
    /* -------------------------------------------------------------------- */

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.M) {
            boolean musicEnabled = audioManager.toggleMusic();
            gamesScreen.showStatus("Music " + (musicEnabled ? "on" : "off"));
            return;
        }

        if (event.getCode() == KeyCode.S) {
            boolean soundEffectsEnabled = audioManager.toggleSoundEffects();
            gamesScreen.showStatus("Sound effects " + (soundEffectsEnabled ? "on" : "off"));
            return;
        }

        if (event.getCode() == KeyCode.P && gameState != GameState.GAME_OVER) {
            togglePause();
            return;
        }

        if (event.getCode() == KeyCode.E && gameState == GameState.PAUSED) {
            backToMenu.run();
        }


        if (gameState != GameState.RUNNING) {
            return;
        }

        switch (event.getCode()) {
            case LEFT -> moveHorizontally(-1);
            case RIGHT -> moveHorizontally(1);
            case DOWN -> softDrop();
            case UP -> tryRotate();
            case SPACE -> hardDrop();
            default -> {
                return;
            }
        }

        resetFallProgressIfBlocked();
        render();
    }

    private Runnable backToMenu;

    public GameController(GameScreen gameScreen, Runnable backToMenu, AudioManager audioManager) {
        this(gameScreen, backToMenu, audioManager, new StandardScoringStrategy());
    }

    /**
     * @param scoringStrategy the Strategy used to turn a line clear into points;
     *                        pass a different implementation to change the scoring
     *                        rules without changing any other game logic.
     */
    public GameController(GameScreen gameScreen, Runnable backToMenu,
                          AudioManager audioManager, ScoringStrategy scoringStrategy) {
        this.gamesScreen = gameScreen;
        this.backToMenu = backToMenu;
        this.audioManager = audioManager;
        this.scoringStrategy = scoringStrategy;
        gravityTimer = new AnimationTimer() {
            @Override
            public void handle(long currentTimeNanos) {
                double currentTimeMs = currentTimeNanos / NANO_TO_MS;
                updateGravity(currentTimeMs);
            }
        };
    }

    private void moveHorizontally(int direction) {
        if (board.canPlace(currentPiece, currentPiece.getX() + direction, currentPiece.getY())) {
            if (direction < 0) {
                currentPiece.moveLeft();
            } else {
                currentPiece.moveRight();
            }
        }
    }

    private void softDrop() {
        if (canCurrentPieceFall()) {
            currentPiece.moveDown();
            accumulatedFallMs = 0;
        } else {
            lockClearAndSpawn();
        }
    }

    private void hardDrop() {
        while (board.canPlace(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
            currentPiece.moveDown();
        }
        accumulatedFallMs = 0;
        lockClearAndSpawn();
    }

    private void resetFallProgressIfBlocked() {
        if (!canCurrentPieceFall()) {
            accumulatedFallMs = 0;
        }
    }

    private void tryRotate() {
        currentPiece.rotate();
        if (!board.canPlace(currentPiece, currentPiece.getX(), currentPiece.getY())) {
            // every supplied shape uses four rotations, so three more undo an invalid turn
            currentPiece.rotate();
            currentPiece.rotate();
            currentPiece.rotate();
        }
    }

    /* -------------------------------------------------------------------- */
    /*  locking pieces and clearing rows                                    */
    /* -------------------------------------------------------------------- */

    private void saveCurrentPieceColours() {
        board.eachCellFilled(currentPiece, currentPiece.getX(), currentPiece.getY(), (boardRow, boardCol) -> {
            if (boardRow >= 0 && boardRow < Board.HEIGHT && boardCol >= 0 && boardCol < Board.WIDTH) {
                lockedColours[boardRow][boardCol] = lockedColour;
            }
        });
    }

    private void lockClearAndSpawn() {
        saveCurrentPieceColours();
        board.lockPiece(currentPiece);

        List<Integer> clearedRows = board.clearFullRows();
        for (int row : clearedRows) {
            shiftColoursDown(row);
        }

        if (!clearedRows.isEmpty()) {
            score += scoringStrategy.scoreForLines(clearedRows.size());
            gamesScreen.updateScore(score);
            audioManager.playLineClear();
        }
        spawnPiece();
    }

    private void shiftColoursDown(int clearedRow) {
        for (int row = clearedRow; row > 0; row--) {
            lockedColours[row] = lockedColours[row - 1].clone();
        }
        lockedColours[0] = new Color[Board.WIDTH];
    }

    /* -------------------------------------------------------------------- */
    /*  spawning, pause, and game over                                      */
    /* -------------------------------------------------------------------- */

    private void spawnPiece() {
    if (nextPiece == null) {
        nextPiece = TetrominoFactory.createRandomPiece();
    }

    currentPiece = nextPiece;
    nextPiece = TetrominoFactory.createRandomPiece();

    accumulatedFallMs = 0;
    lastFrameTimeMs = 0;

    long currentPieceNumber = ++pieceNumber;

    if (!board.canPlace(currentPiece, currentPiece.getX(), currentPiece.getY())) {
        gameState = GameState.GAME_OVER;
        gravityTimer.stop();
        gamesScreen.showGameOver(score);
        return;
    }

    requestExternalMove(currentPiece, currentPieceNumber);
}

private void requestExternalMove(Tetromino piece, long currentPieceNumber) {
    int[][] cells = copyBoard();
    int[][] currentShape = copyMatrix(piece.getShape());
    int[][] followingShape = copyMatrix(nextPiece.getShape());

    Thread connectionThread = new Thread(() -> {
        boolean warningShown = false;

        while (pieceNumber == currentPieceNumber) {
            try {
                ExternalPlayerClient.ExternalMove move =
                        externalPlayerClient.requestMove(
                                cells,
                                currentShape,
                                followingShape);

                Platform.runLater(() ->
                        applyExternalMove(move, piece, currentPieceNumber));
                return;

            } catch (IOException exception) {
                if (!warningShown) {
                    Platform.runLater(() ->
                            gamesScreen.showStatus(
                                    "External player unavailable - retrying..."));
                    warningShown = true;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    });

    connectionThread.setDaemon(true);
    connectionThread.start();
}

private void applyExternalMove(
        ExternalPlayerClient.ExternalMove move,
        Tetromino piece,
        long currentPieceNumber) {

    if (pieceNumber != currentPieceNumber ||
            currentPiece != piece ||
            gameState != GameState.RUNNING) {
        return;
    }

    int rotations = Math.floorMod(move.opRotate(), 4);

    for (int i = 0; i < rotations; i++) {
        tryRotate();
    }

    int targetX = move.opX();

    while (currentPiece.getX() < targetX) {
        int previousX = currentPiece.getX();
        moveHorizontally(1);

        if (currentPiece.getX() == previousX) {
            break;
        }
    }

    while (currentPiece.getX() > targetX) {
        int previousX = currentPiece.getX();
        moveHorizontally(-1);

        if (currentPiece.getX() == previousX) {
            break;
        }
    }

    gamesScreen.showStatus("External player connected");
    render();
}

private int[][] copyBoard() {
    int[][] cells = new int[Board.HEIGHT][Board.WIDTH];

    for (int row = 0; row < Board.HEIGHT; row++) {
        for (int col = 0; col < Board.WIDTH; col++) {
            cells[row][col] = board.getCell(row, col);
        }
    }

    return cells;
}

private int[][] copyMatrix(int[][] matrix) {
    int[][] copy = new int[matrix.length][];

    for (int row = 0; row < matrix.length; row++) {
        copy[row] = matrix[row].clone();
    }

    return copy;
}

    private void togglePause() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED;
            gravityTimer.stop();
            lastFrameTimeMs = 0;
            gamesScreen.showPauseOverlay(true);
            gamesScreen.showStatus("Game paused");
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING;
            gravityTimer.start();
            gamesScreen.showPauseOverlay(false);
            gamesScreen.showStatus("");
        }
    }

    /* -------------------------------------------------------------------- */
    /*  drawing the board and piece on screen                               */
    /* -------------------------------------------------------------------- */

    private void render() {
        gamesScreen.render(board, lockedColours, currentPiece);
        double fallProgress = accumulatedFallMs / DROP_SPEED;
        gamesScreen.setActivePieceVerticalOffset(fallProgress * GameScreen.CELL_SIZE);
    }
}
