package com.frisian_draught.util;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundPlayer {
    private MediaPlayer backgroundMusic;
    private AudioClip moveSound;
    private AudioClip captureSound;

    private DoubleProperty mainVolume = new SimpleDoubleProperty(1.0);
    private DoubleProperty backgroundVolume = new SimpleDoubleProperty(1.0);
    private DoubleProperty moveVolume = new SimpleDoubleProperty(1.0);
    private DoubleProperty captureVolume = new SimpleDoubleProperty(1.0);

    public SoundPlayer() {
        Media backgroundMedia =
                new Media(
                        getClass()
                                .getResource(
                                        "/sound/important-to-you-pecan-pie-main-version-18025-02-06.mp3")
                                .toExternalForm());
        backgroundMusic = new MediaPlayer(backgroundMedia);
        moveSound = new AudioClip(getClass().getResource("/sound/move.mp3").toExternalForm());
        captureSound = new AudioClip(getClass().getResource("/sound/move.mp3").toExternalForm());

        // Start playing background music
        playBackgroundMusic();

        // Add listeners to monitor volume changes
        mainVolume.addListener((_, _, _) -> updateVolumes());
        backgroundVolume.addListener((_, _, _) -> updateBackgroundMusicVolume());
        moveVolume.addListener((_, _, _) -> updateMoveSoundVolume());
        captureVolume.addListener((_, _, _) -> updateCaptureSoundVolume());
    }

    private void updateVolumes() {
        updateBackgroundMusicVolume();
        updateMoveSoundVolume();
        updateCaptureSoundVolume();
    }

    private void updateBackgroundMusicVolume() {
        double volume = mainVolume.get() * backgroundVolume.get();
        backgroundMusic.setVolume(volume);
    }

    private void updateMoveSoundVolume() {
        double volume = mainVolume.get() * moveVolume.get();
        moveSound.setVolume(volume);
    }

    private void updateCaptureSoundVolume() {
        double volume = mainVolume.get() * captureVolume.get();
        captureSound.setVolume(volume);
    }

    public void playBackgroundMusic() {
        if (backgroundMusic.getStatus() != MediaPlayer.Status.PLAYING) {
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusic.play();
        }
    }

    public void playMoveSound() {
        moveSound.setVolume(mainVolume.get() * moveVolume.get());
        moveSound.play();
    }

    public void playCaptureSound() {
        captureSound.setVolume(mainVolume.get() * captureVolume.get());
        captureSound.play();
    }

    // Setters and getters for volumes
    public void setMainVolume(double volume) {
        mainVolume.set(volume);
    }

    public void setBackgroundVolume(double volume) {
        backgroundVolume.set(volume);
    }

    public void setMoveVolume(double volume) {
        moveVolume.set(volume);
    }

    public void setCaptureVolume(double volume) {
        captureVolume.set(volume);
    }

    public double getMainVolume() {
        return mainVolume.get();
    }

    public double getBackgroundVolume() {
        return backgroundVolume.get();
    }

    public double getMoveVolume() {
        return moveVolume.get();
    }

    public double getCaptureVolume() {
        return captureVolume.get();
    }

    // Properties for binding
    public DoubleProperty mainVolumeProperty() {
        return mainVolume;
    }

    public DoubleProperty backgroundVolumeProperty() {
        return backgroundVolume;
    }

    public DoubleProperty moveVolumeProperty() {
        return moveVolume;
    }

    public DoubleProperty captureVolumeProperty() {
        return captureVolume;
    }
}
