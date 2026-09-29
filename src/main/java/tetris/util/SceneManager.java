package tetris.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

/**
 * Owns the single JavaFX {@link Scene} used to switch between the application's
 * screens. Enforced as a Singleton: JavaFX only hands us the primary {@link Stage}
 * inside {@code Application.start(Stage)}, so the instance cannot be created via a
 * static field initializer. Instead {@link #init(Stage)} must be called exactly once,
 * from {@code Main.start}, before any other code calls {@link #getInstance()}.
 */
public final class SceneManager {

    private static final String STYLESHEET_PATH = "/tetris/styles/style.css";

    private static SceneManager instance;

    private final Stage primaryStage;
    private Scene scene;

    private SceneManager(Stage primaryStage) {
        this.primaryStage = Objects.requireNonNull(primaryStage, "primaryStage");
    }

    /**
     * Creates the single {@code SceneManager} bound to {@code primaryStage}.
     * Must be called exactly once during application start-up.
     *
     * @throws IllegalStateException if the singleton has already been initialised
     */
    public static synchronized SceneManager init(Stage primaryStage) {
        if (instance != null) {
            throw new IllegalStateException("SceneManager has already been initialised");
        }
        instance = new SceneManager(primaryStage);
        return instance;
    }

    /**
     * Returns the single {@code SceneManager} instance.
     *
     * @throws IllegalStateException if {@link #init(Stage)} has not been called yet
     */
    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SceneManager.init(Stage) must be called before getInstance()");
        }
        return instance;
    }

    /**
     * Test-only hook to reset the singleton between test cases. Not used by
     * application code.
     */
    static synchronized void resetForTesting() {
        instance = null;
    }

    public void show(Parent screenRoot) {
        Objects.requireNonNull(screenRoot, "screenRoot");

        if (scene == null) {
            scene = new Scene(screenRoot);
            addSharedStylesheet();
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(screenRoot);
        }

        primaryStage.sizeToScene();
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
        screenRoot.requestFocus();
    }

    private void addSharedStylesheet() {
        URL stylesheet = SceneManager.class.getResource(STYLESHEET_PATH);
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }
}
