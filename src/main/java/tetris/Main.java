package tetris;

import tetris.controller.ConfigController;
import tetris.controller.GameController;
import tetris.controller.HighScoreController;
import tetris.controller.TwoPlayerGameController;
import tetris.stats.MatchHistoryService;
import tetris.audio.AudioManager;
import tetris.ui.*;
import tetris.util.SceneManager;
import javafx.application.Application;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

/* Starts the application. Game rules live in GameController and JavaFX drawing lives in GameScreen. */

public class Main extends Application {

    private SceneManager sceneManager;
    private MainMenuScreen mainMenuScreen;
    private final HighScoreController highScoreController = new HighScoreController();
    private final ConfigController configController = new ConfigController();
    private AudioManager audioManager;
    private final MatchHistoryService matchHistoryService = new MatchHistoryService();
    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        audioManager = new AudioManager(configController);

        sceneManager = SceneManager.init(primaryStage);
        primaryStage.setTitle("Tetris - 2006ICT");
        primaryStage.setResizable(false);

        mainMenuScreen = new MainMenuScreen(
                this::showGame,
                this::showConfiguration,
                this::showHighScores,
                this::confirmExit,
                this::showTwoPlayerGame
        );

        SplashScreen splashScreen = new SplashScreen();
        sceneManager.show(splashScreen.getRoot());
        splashScreen.start(this::showMainMenu);
    }

    private void showMainMenu() {
        sceneManager.show(mainMenuScreen.getRoot());
    }

    private void showGame() {
        GameScreen gameScreen = new GameScreen(
                configController.getBoardWidth(), configController.getBoardHeight(), this::showGame, this::saveHighScore, this::showMainMenu);

        GameController gameController = new GameController(
                gameScreen, this::showMainMenu, audioManager, configController, matchHistoryService);

        sceneManager.show(gameScreen.getRoot());
        gameController.startGame();
    }

    private void showTwoPlayerGame() {
        TwoPlayerGameScreen twoPlayerGameScreen = new TwoPlayerGameScreen(this::showMainMenu);
        TwoPlayerGameController twoPlayerGameController =
                new TwoPlayerGameController(twoPlayerGameScreen, this::showMainMenu);
        sceneManager.show(twoPlayerGameScreen.getRoot());
        twoPlayerGameController.startGame();
    }


    private void showConfiguration() {
        ConfigurationScreen configurationScreen = new ConfigurationScreen(
                configController,
                this::showMainMenu
        );
        sceneManager.show(configurationScreen.getRoot());
    }

    private void saveHighScore(int finalScore) {
        TextInputDialog nameDialog = new TextInputDialog("Player");
        nameDialog.setTitle("Save High Score");
        nameDialog.setHeaderText("Game over - Score: " + finalScore);
        nameDialog.setContentText("Player name:");
        nameDialog.getEditor().setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= 20 ? change : null
        ));

        nameDialog.showAndWait()
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .ifPresent(name -> {
                    highScoreController.saveScore(name, finalScore);
                    showHighScores();
                });
    }

    private void showHighScores() {
        HighScoreScreen highScoreScreen = new HighScoreScreen(
                highScoreController.getTopScores(),
                this::showMainMenu
        );
        sceneManager.show(highScoreScreen.getRoot());
    }

    private Stage primaryStage;

    private void confirmExit() {
        ExitDialog exitDialogue = new ExitDialog(primaryStage);
        if (exitDialogue.showAndWait()) {
            primaryStage.close();
        }
    }

    @Override
    public void stop() {
        if (audioManager != null) {
            audioManager.dispose();
        }
        matchHistoryService.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
