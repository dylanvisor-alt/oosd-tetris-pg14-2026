package tetris.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tetris.util.Constants;

public class MainMenuScreen {

    private final StackPane root = new StackPane();

    public MainMenuScreen(
            Runnable onPlay,
            Runnable onConfiguration,
            Runnable onHighScores,
            Runnable onExit,
            Runnable onTwoPlayer
    ) {
        Label title = new Label("TETRIS");
        title.getStyleClass().add("game-title");

        Label subtitle = new Label("2006ICT - Final Submission");
        subtitle.getStyleClass().add("subtitle");

        Button playButton = createMenuButton("Play", onPlay);
        playButton.setDefaultButton(true);
        Button configurationButton = createMenuButton("Configuration", onConfiguration);
        Button highScoresButton = createMenuButton("High Scores", onHighScores);
        Button exitButton = createMenuButton("Exit", onExit);
        Button twoPlayerButton = createMenuButton("2 Player", onTwoPlayer);


        VBox menu = new VBox(
                14,
                title,
                subtitle,
                playButton,
                twoPlayerButton,
                configurationButton,
                highScoresButton,
                exitButton

        );
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(48));
        menu.setMaxWidth(360);
        menu.getStyleClass().add("menu-panel");

        root.getChildren().add(menu);
        root.setAlignment(Pos.CENTER);
        root.setMinSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
        root.setPrefSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
        root.getStyleClass().add("app-background");
    }

    private Button createMenuButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("menu-button");

        if (action == null) {
            button.setDisable(true);
        } else {
            button.setOnAction(event -> action.run());
        }
        return button;
    }

    public Parent getRoot() {
        return root;
    }
}
