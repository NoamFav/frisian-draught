package com.um_project_game.util;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.AudioClip;

public class SoundPlayer {
    private AudioClip backgroundMusic;
    private AudioClip moveSound;
    private AudioClip captureSound;

    private DoubleProperty mainVolume = new SimpleDoubleProperty(1.0);
    private DoubleProperty backgroundVolume = new SimpleDoubleProperty(1.0);
    private DoubleProperty moveVolume = new SimpleDoubleProperty(1.0);
    private DoubleProperty captureVolume = new SimpleDoubleProperty(1.0);

    public SoundPlayer() {
        backgroundMusic =
                new AudioClip(
                        getClass()
                                .getResource(
                                        "/sound/important-to-you-pecan-pie-main-version-18025-02-06.mp3")
                                .toString());
        moveSound = new AudioClip(getClass().getResource("/sound/move.mp3").toString());
        captureSound = new AudioClip(getClass().getResource("/sound/move.mp3").toString());

        // Start playing background music
        playBackgroundMusic();

        setMainVolume(0.4);

        // Add listeners to monitor volume changes
        mainVolume.addListener(
                (obs, oldVal, newVal) -> {
                    updateBackgroundMusicVolume();
                    updateMoveSoundVolume();
                    updateCaptureSoundVolume();
                });
        backgroundVolume.addListener((obs, oldVal, newVal) -> updateBackgroundMusicVolume());
        moveVolume.addListener((obs, oldVal, newVal) -> updateMoveSoundVolume());
        captureVolume.addListener((obs, oldVal, newVal) -> updateCaptureSoundVolume());
    }

    private void updateBackgroundMusicVolume() {
        double volume = mainVolume.get() * backgroundVolume.get();
        volumeCheck(backgroundMusic, volume);
    }

    private void updateMoveSoundVolume() {
        double volume = mainVolume.get() * moveVolume.get();
        volumeCheck(moveSound, volume);
    }

    private void updateCaptureSoundVolume() {
        double volume = mainVolume.get() * captureVolume.get();
        volumeCheck(captureSound, volume);
    }

    private void volumeCheck(AudioClip sound, double volume) {
        sound.setVolume(volume);
        if (volume == 0) {
            sound.stop();
        } else {
            sound.stop();
            sound.play();
        }
    }

    public void playBackgroundMusic() {
        backgroundMusic.setCycleCount(AudioClip.INDEFINITE);
        backgroundMusic.setVolume(mainVolume.get() * backgroundVolume.get());
        backgroundMusic.play();
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
