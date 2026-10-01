package tetris.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tetris.controller.ConfigController;
import tetris.model.GameConfig;
import tetris.util.Constants;

import java.util.Objects;
import java.util.function.Consumer;

public final class ConfigurationScreen {

    private final StackPane root = new StackPane();

    public ConfigurationScreen(ConfigController configController, Runnable onBack) {
        Objects.requireNonNull(configController, "configController");
        Objects.requireNonNull(onBack, "onBack");

        Label title = new Label("Configuration");
        title.getStyleClass().add("screen-title");

        GridPane settings = new GridPane();
        settings.setHgap(22);
        settings.setVgap(18);
        settings.setAlignment(Pos.CENTER);
        settings.setPadding(new Insets(16));
        settings.getStyleClass().add("config-grid");

        ComboBox<String> fieldSize = new ComboBox<>();
        fieldSize.getItems().addAll(GameConfig.FIELD_SIZES);
        fieldSize.setValue(configController.getFieldSize());
        fieldSize.setMaxWidth(Double.MAX_VALUE);
        fieldSize.setOnAction(event -> configController.setFieldSize(fieldSize.getValue()));
        addSetting(settings, "Field size", fieldSize, 0);

        Label levelValue = new Label(Integer.toString(configController.getLevel()));
        levelValue.getStyleClass().add("config-value");

        Slider level = new Slider(GameConfig.MIN_LEVEL, GameConfig.MAX_LEVEL, configController.getLevel());
        level.setMajorTickUnit(1);
        level.setMinorTickCount(0);
        level.setSnapToTicks(true);
        level.setShowTickMarks(true);
        level.setShowTickLabels(true);
        level.valueProperty().addListener((observable, oldValue, newValue) -> {
            int selectedLevel = newValue.intValue();
            levelValue.setText(Integer.toString(selectedLevel));
            configController.setLevel(selectedLevel);
        });

        HBox levelControl = new HBox(12, level, levelValue);
        levelControl.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(level, Priority.ALWAYS);
        addSetting(settings, "Level", levelControl, 1);

        addSetting(settings, "Music", createCheckBox(
                configController.isMusicEnabled(), configController::setMusicEnabled), 2);
        addSetting(settings, "Sound effects", createCheckBox(
                configController.isSoundEffectsEnabled(), configController::setSoundEffectsEnabled), 3);
        addSetting(settings, "AI play", createCheckBox(
                configController.isAiPlayEnabled(), configController::setAiPlayEnabled), 4);
        // addSetting(settings, "Extended mode", createCheckBox(
        //        configController.isExtendedModeEnabled(), configController::setExtendedModeEnabled), 5);
        addSetting(settings, "External Player", createCheckBox(
                configController.isExternalPlayerEnabled(), configController::setExternalPlayerEnabled), 5);


        Label note = new Label("Settings are saved for this session.");
        note.getStyleClass().add("config-note");

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.getStyleClass().add("menu-button");
        backButton.setOnAction(event -> onBack.run());

        VBox content = new VBox(24, title, settings, note, backButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(34));
        content.setMaxWidth(440);

        root.getChildren().add(content);
        root.setAlignment(Pos.CENTER);
        root.setMinSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
        root.setPrefSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
        root.getStyleClass().add("app-background");
    }

    private void addSetting(GridPane grid, String text, Node control, int row) {
        Label label = new Label(text);
        label.getStyleClass().add("config-label");
        grid.add(label, 0, row);
        grid.add(control, 1, row);
    }

    private CheckBox createCheckBox(boolean selected, Consumer<Boolean> onChange) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(selected);
        checkBox.setOnAction(event -> onChange.accept(checkBox.isSelected()));
        return checkBox;
    }

    public Parent getRoot() {
        return root;
    }
}
