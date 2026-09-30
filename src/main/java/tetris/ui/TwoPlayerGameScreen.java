package tetris.ui;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tetris.model.Board;

public class TwoPlayerGameScreen {

    public static final int BOARD_WIDTH = Board.WIDTH;
    public static final int BOARD_HEIGHT = Board.HEIGHT;
    public static final int CELL_SIZE = 24;

    private final BorderPane root = new BorderPane();
    private final PlayerBoardView player1View;
    private final PlayerBoardView player2View;
    private final Label statusLabel = new Label();
    private final Button exitButton = new Button("Exit to Menu");

    public TwoPlayerGameScreen(Runnable onExit) {
        player1View = new PlayerBoardView("Player 1", BOARD_WIDTH, BOARD_HEIGHT, CELL_SIZE);
        player2View = new PlayerBoardView("Player 2", BOARD_WIDTH, BOARD_HEIGHT, CELL_SIZE);

        HBox boards = new HBox(24, player1View.getNode(), player2View.getNode());
        boards.setAlignment(Pos.CENTER);
        boards.setPadding(new Insets(20));

        statusLabel.getStyleClass().add("game-status");

        exitButton.setOnAction(event -> onExit.run());
        exitButton.getStyleClass().add("game-action-button");

        Label controlsLabel = new Label("P1: Arrow keys + Space | P2: WASD + Q | P - Pause | E - Exit whilst paused");
        controlsLabel.getStyleClass().add("game-controls");

        VBox bottomBar = new VBox(6, statusLabel, controlsLabel, exitButton);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(10));

        root.setCenter(boards);
        root.setBottom(bottomBar);
        root.getStyleClass().add("game-background");
        root.setFocusTraversable(true);
    }

    public BorderPane getRoot() {
        return root;
    }

    public PlayerBoardView getPlayer1View() {
        return player1View;
    }

    public PlayerBoardView getPlayer2View() {
        return player2View;
    }

    public void setKeyHandler(EventHandler<KeyEvent> keyHandler) {
        root.setOnKeyPressed(keyHandler);
    }

    public void requestKeyboardFocus() {
        root.requestFocus();
    }

    public void showStatus(String status) {
        statusLabel.setText(status);
    }

    public void showMatchOver(String message) {
        statusLabel.setText(message);
    }
}
