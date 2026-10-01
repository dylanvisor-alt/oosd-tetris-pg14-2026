package tetris.model;

import java.util.List;
import java.util.Objects;

/** Holds the values selected on the Milestone 1 configuration screen. */
public final class GameConfig {

    public static final List<String> FIELD_SIZES = List.of("10 x 20", "12 x 24", "14 x 28");
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 10;

    private String fieldSize = FIELD_SIZES.get(0);
    private int level = MIN_LEVEL;
    private boolean musicEnabled = true;
    private boolean soundEffectsEnabled = true;
    private boolean aiPlayEnabled;
    private boolean extendedModeEnabled;

    private boolean externalPlayerEnabled;

    public boolean isExternalPlayerEnabled() {
        return externalPlayerEnabled;
    }

    public void setExternalPlayerEnabled(boolean externalPlayerEnabled) {
        this.externalPlayerEnabled = externalPlayerEnabled;
    }

    public String getFieldSize() {
        return fieldSize;
    }

    public void setFieldSize(String fieldSize) {
        String selectedSize = Objects.requireNonNull(fieldSize, "fieldSize");
        if (!FIELD_SIZES.contains(selectedSize)) {
            throw new IllegalArgumentException("Unsupported field size: " + selectedSize);
        }
        this.fieldSize = selectedSize;
    }

    public int getLevel() {
        return level;
    }

    public int getBoardWidth() {
        return Integer.parseInt(fieldSize.split(" x ")[0].trim());
    }

    public int getBoardHeight() {
        return Integer.parseInt(fieldSize.split(" x ")[1].trim());
    }

    public void setLevel(int level) {
        if (level < MIN_LEVEL || level > MAX_LEVEL) {
            throw new IllegalArgumentException("Level must be between " + MIN_LEVEL + " and " + MAX_LEVEL);
        }
        this.level = level;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }

    public boolean isSoundEffectsEnabled() {
        return soundEffectsEnabled;
    }

    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        this.soundEffectsEnabled = soundEffectsEnabled;
    }

    public boolean isAiPlayEnabled() {
        return aiPlayEnabled;
    }

    public void setAiPlayEnabled(boolean aiPlayEnabled) {
        this.aiPlayEnabled = aiPlayEnabled;
    }

    public boolean isExtendedModeEnabled() {
        return extendedModeEnabled;
    }

    public void setExtendedModeEnabled(boolean extendedModeEnabled) {
        this.extendedModeEnabled = extendedModeEnabled;
    }
}
