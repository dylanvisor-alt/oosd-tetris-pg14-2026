package tetris.controller;

import tetris.model.GameConfig;

import java.util.Objects;

public final class ConfigController {

    private final GameConfig gameConfig;

    public ConfigController() {
        this(new GameConfig());
    }

    public ConfigController(GameConfig gameConfig) {
        this.gameConfig = Objects.requireNonNull(gameConfig, "gameConfig");
    }

    public String getFieldSize() {
        return gameConfig.getFieldSize();
    }

    public void setFieldSize(String fieldSize) {
        gameConfig.setFieldSize(fieldSize);
    }

    public int getLevel() {
        return gameConfig.getLevel();
    }

    public void setLevel(int level) {
        gameConfig.setLevel(level);
    }

    public boolean isMusicEnabled() {
        return gameConfig.isMusicEnabled();
    }

    public void setMusicEnabled(boolean enabled) {
        gameConfig.setMusicEnabled(enabled);
    }

    public boolean isSoundEffectsEnabled() {
        return gameConfig.isSoundEffectsEnabled();
    }

    public void setSoundEffectsEnabled(boolean enabled) {
        gameConfig.setSoundEffectsEnabled(enabled);
    }

    public boolean isAiPlayEnabled() {
        return gameConfig.isAiPlayEnabled();
    }

    public void setAiPlayEnabled(boolean enabled) {
        gameConfig.setAiPlayEnabled(enabled);
    }

    public boolean isExtendedModeEnabled() {
        return gameConfig.isExtendedModeEnabled();
    }

    public void setExtendedModeEnabled(boolean enabled) {
        gameConfig.setExtendedModeEnabled(enabled);
    }

    public int getBoardWidth() {
        return gameConfig.getBoardWidth();
    }

    public int getBoardHeight() {
        return gameConfig.getBoardHeight();
    }

    public boolean isExternalPlayerEnabled() {
        return gameConfig.isExternalPlayerEnabled();
    }

    public void setExternalPlayerEnabled(boolean enabled) {
        gameConfig.setExternalPlayerEnabled(enabled);
    }

}
