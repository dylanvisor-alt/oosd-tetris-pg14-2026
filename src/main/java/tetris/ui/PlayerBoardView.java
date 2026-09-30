package tetris.ui;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import tetris.model.Board;
import tetris.model.Tetromino;

public class PlayerBoardView {
    private static final Color EMPTY_COLOUR = Color.web("#090d21");
    private static final Color GRID_COLOUR = Color.web("#293462");

    private final int boardWidth;
    private final int boardHeight;
    private final int cellSize;
    private final Rectangle[][] lockedCellViews;
    private final Pane activePieceLayer = new Pane();
    private final Label nameLabel;
    private final Label scoreLabel = new Label("Score: 0");
    private final Label outLabel = new Label("OUT");
    private final VBox root;

    private Group activePieceView;

    public PlayerBoardView(String playerName, int boardWidth, int boardHeight, int cellSize) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.cellSize = cellSize;
        this.lockedCellViews = new Rectangle[boardHeight][boardWidth];
        this.nameLabel = new Label(playerName);

        GridPane lockedGrid = new GridPane();
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                Rectangle cell = new Rectangle(cellSize, cellSize, EMPTY_COLOUR);
                cell.setStroke(GRID_COLOUR);
                lockedCellViews[row][col] = cell;
                lockedGrid.add(cell, col, row);
            }
        }

        int pixelWidth = boardWidth * cellSize;
        int pixelHeight = boardHeight * cellSize;

        activePieceLayer.setMinSize(pixelWidth, pixelHeight);
        activePieceLayer.setPrefSize(pixelWidth, pixelHeight);
        activePieceLayer.setMaxSize(pixelWidth, pixelHeight);
        activePieceLayer.setMouseTransparent(true);

        outLabel.getStyleClass().add("game-status");
        outLabel.setVisible(false);
        outLabel.setManaged(false);

        StackPane boardStack = new StackPane(lockedGrid, activePieceLayer, outLabel);
        boardStack.setMinSize(pixelWidth, pixelHeight);
        boardStack.setPrefSize(pixelWidth, pixelHeight);
        boardStack.setMaxSize(pixelWidth, pixelHeight);
        boardStack.getStyleClass().add("game-board-frame");

        nameLabel.getStyleClass().add("game-score");
        scoreLabel.getStyleClass().add("game-score");

        root = new VBox(8, nameLabel, boardStack, scoreLabel);
        root.setAlignment(Pos.CENTER);
    }

    public Parent getNode() {
        return root;
    }

    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    public void showOut() {
        outLabel.setVisible(true);
        outLabel.setManaged(true);
    }

    public void render(Board board, Color[][] lockedColors, Tetromino currentPiece) {
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
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

    public Group makePieceView(Tetromino piece) {
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
}
