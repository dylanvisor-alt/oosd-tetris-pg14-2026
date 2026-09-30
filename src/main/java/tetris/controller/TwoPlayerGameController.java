package tetris.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import tetris.model.Board;
import tetris.model.Tetromino;
import tetris.model.TetrominoFactory;
import tetris.ui.PlayerBoardView;
import tetris.ui.TwoPlayerGameScreen;

import java.util.List;

public class TwoPlayerGameController {

    private static final double NANO_TO_MS = 1_000_000;
    private static final double DROP_SPEED = 300.0;
    private static final double MAX_FRAME_GAP = 50.0;
    private static final Color LOCKED_COLOUR = Color.web("#007aff");

    private final TwoPlayerGameScreen screen;
    private final Runnable backToMenu;
    private final AnimationTimer gravityTimer;

    private final PlayerSession player1;
    private final PlayerSession player2;

    private double lastFrameTimeMs;
    private boolean paused;

    public TwoPlayerGameController(TwoPlayerGameScreen screen, Runnable backToMenu) {
        this.screen = screen;
        this.backToMenu = backToMenu;

        this.player1 = new PlayerSession(screen.getPlayer1View());
        this.player2 = new PlayerSession(screen.getPlayer2View());

        gravityTimer = new AnimationTimer() {
            @Override
            public void handle(long currentTimeNanos) {
                updateGravity(currentTimeNanos / NANO_TO_MS);
            }
        };
    }

    public void startGame() {
        screen.setKeyHandler(this::handleKeyPress);
        player1.spawnPiece();
        player2.spawnPiece();
        player1.render();
        player2.render();
        gravityTimer.start();
        screen.requestKeyboardFocus();
    }

    private void updateGravity(double currentTimeMs) {
        if (paused) {
            return;
        }
        if (lastFrameTimeMs == 0) {
            lastFrameTimeMs = currentTimeMs;
            return;
        }

        double frameDelta = Math.min(currentTimeMs - lastFrameTimeMs, MAX_FRAME_GAP);
        lastFrameTimeMs = currentTimeMs;

        player1.tick(frameDelta);
        player2.tick(frameDelta);
        player1.render();
        player2.render();
        checkForMatchOver();
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code == KeyCode.P) {
            togglePause();
            return;
        }
        if (code == KeyCode.E && paused) {
            backToMenu.run();
            return;
        }
        if (paused) {
            return;
        }

        switch (code) {
            case LEFT -> player1.moveHorizontally(-1);
            case RIGHT -> player1.moveHorizontally(1);
            case DOWN -> player1.softDrop();
            case UP -> player1.tryRotate();
            case SPACE -> player1.hardDrop();
            case A -> player2.moveHorizontally(-1);
            case D -> player2.moveHorizontally(1);
            case S -> player2.softDrop();
            case W -> player2.tryRotate();
            case Q -> player2.hardDrop();
            default -> {
                return;
            }
        }
        player1.render();
        player2.render();
    }

    private void togglePause() {
        paused = !paused;
        lastFrameTimeMs = 0;
        screen.showStatus(paused ? "Paused | Press E to Exit" : "");
    }

    private void checkForMatchOver() {
        if (!player1.gameOver || !player2.gameOver) {
            return;
        }

        gravityTimer.stop();
        if (player1.score > player2.score) {
            screen.showMatchOver("Player 1 Wins! " + player1.score + " - " + player2.score);
        } else if (player2.score > player1.score) {
            screen.showMatchOver("Player 2 wins! " + player2.score + " - " + player1.score);
        } else {
            screen.showMatchOver("Draw! " + player1.score + " - " + player2.score);
        }
    }

    private final class PlayerSession {
        private final Board board = new Board(TwoPlayerGameScreen.BOARD_WIDTH, TwoPlayerGameScreen.BOARD_HEIGHT);
        private final Color [][] lockedColours =
                new Color[TwoPlayerGameScreen.BOARD_HEIGHT][TwoPlayerGameScreen.BOARD_WIDTH];

        private final PlayerBoardView view;

        private Tetromino currentPiece;
        private int score;
        private double accumulatedFallMs;
        private boolean gameOver;

        private PlayerSession(PlayerBoardView view) {
            this.view = view;
        }

        private void spawnPiece() {
            currentPiece = TetrominoFactory.createRandomPiece();
            accumulatedFallMs = 0;
            if (!board.canPlace(currentPiece, currentPiece.getX(), currentPiece.getY())) {
                gameOver = true;
                view.showOut();
            }
        }

        private boolean canFall() {
            return board.canPlace(currentPiece, currentPiece.getX(), currentPiece.getY() +1);
        }

        private void tick(double frameDelta) {
            if (gameOver || currentPiece == null) {
                return;
            }
            if (!canFall()) {
                lockClearAndSpawn();
                return;
            }
            accumulatedFallMs += frameDelta;
            if (accumulatedFallMs >= DROP_SPEED) {
                currentPiece.moveDown();
                accumulatedFallMs -= DROP_SPEED;
                if (!canFall()) {
                    lockClearAndSpawn();
                }
            }
        }

        private void moveHorizontally(int direction) {
            if (gameOver || currentPiece == null) {
                return;
            }
            if (board.canPlace(currentPiece, currentPiece.getX() + direction, currentPiece.getY())) {
                if (direction < 0) {
                    currentPiece.moveLeft();
                } else {
                    currentPiece.moveRight();
                }
            }
        }

        private void softDrop() {
            if (gameOver || currentPiece == null) {
                return;
            }
            if (canFall()) {
                currentPiece.moveDown();
                accumulatedFallMs = 0;
            } else {
                lockClearAndSpawn();
            }
        }

        private void hardDrop() {
            if (gameOver || currentPiece == null) {
                return;
            }
            while (board.canPlace(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
                currentPiece.moveDown();
            }
            lockClearAndSpawn();
        }

        private void tryRotate() {
            if (gameOver || currentPiece == null) {
                return;
            }
            currentPiece.rotate();
            if (!board.canPlace(currentPiece, currentPiece.getX(), currentPiece.getY())) {
                currentPiece.rotate();
                currentPiece.rotate();
                currentPiece.rotate();
            }
        }

        private void lockClearAndSpawn() {
            board.eachCellFilled(currentPiece, currentPiece.getX(), currentPiece.getY(), (row, col) -> {
                if (row >= 0 && row < board.getHeight() && col >= 0 && col < board.getWidth()) {
                    lockedColours[row][col] = LOCKED_COLOUR;
                }
            });
            board.lockPiece(currentPiece);

            List<Integer> clearedRows = board.clearFullRows();
            if (!clearedRows.isEmpty()) {
                for (int row : clearedRows) {
                    for (int r = row; r > 0; r--) {
                        lockedColours[r] = lockedColours[r - 1].clone();
                    }
                    lockedColours[0] = new Color[board.getWidth()];
                }

                score += switch (clearedRows.size()) {
                    case 1 -> 100;
                    case 2 -> 300;
                    case 3 -> 500;
                    case 4 -> 800;
                    default -> 0;
                };
                view.updateScore(score);
            }
            spawnPiece();
        }

        private void render() {
            view.render(board, lockedColours, currentPiece);
        }

    }
}
