package tetris.audio;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import tetris.controller.ConfigController;

import java.net.URL;
import java.util.Objects;

/** Plays the game's background music and short sound effects. */
public final class AudioManager {

    private static final String MUSIC_RESOURCE = "/tetris/sounds/music.mp3";
    private static final String LINE_CLEAR_RESOURCE = "/tetris/sounds/clear-line.wav";

    private final ConfigController configController;
    private final MediaPlayer backgroundMusic;
    private final AudioClip lineClearSound;

    public AudioManager(ConfigController configController) {
        this.configController = Objects.requireNonNull(configController, "configController");
        backgroundMusic = new MediaPlayer(new Media(resourceUrl(MUSIC_RESOURCE).toExternalForm()));
        backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
        backgroundMusic.setVolume(0.45);
        lineClearSound = new AudioClip(resourceUrl(LINE_CLEAR_RESOURCE).toExternalForm());
        lineClearSound.setVolume(0.75);
    }

    /** Starts or pauses music to match the selected configuration. */
    public void applyMusicSetting() {
        if (configController.isMusicEnabled()) {
            if (backgroundMusic.getStatus() != MediaPlayer.Status.PLAYING) {
                backgroundMusic.play();
            }
        } else {
            backgroundMusic.pause();
        }
    }

    public boolean toggleMusic() {
        boolean enabled = !configController.isMusicEnabled();
        configController.setMusicEnabled(enabled);
        applyMusicSetting();
        return enabled;
    }

    public boolean toggleSoundEffects() {
        boolean enabled = !configController.isSoundEffectsEnabled();
        configController.setSoundEffectsEnabled(enabled);
        return enabled;
    }

    public void playLineClear() {
        if (configController.isSoundEffectsEnabled()) {
            lineClearSound.play();
        }
    }

    /** Releases native media resources when JavaFX closes. */
    public void dispose() {
        backgroundMusic.stop();
        backgroundMusic.dispose();
    }

    private static URL resourceUrl(String resourcePath) {
        URL resource = AudioManager.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Audio resource not found: " + resourcePath);
        }
        return resource;
    }
}
