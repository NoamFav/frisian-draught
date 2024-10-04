package com.um_project_game.util;

import javafx.scene.media.AudioClip;

public class SoundPlayer {
    private AudioClip backgroundMusic;
    private AudioClip moveSound;
    private AudioClip captureSound;

    public SoundPlayer() {
        backgroundMusic = new AudioClip(getClass().getResource("/sound/background.wav").toString());
        moveSound = new AudioClip(getClass().getResource("/sound/move.mp3").toString());
        //captureSound = new AudioClip(getClass().getResource("snap.mp3").toString());
    }

    public void playBackgroundMusic() {
        backgroundMusic.setCycleCount(AudioClip.INDEFINITE);
        backgroundMusic.play();
    }

    public void playMoveSound() {
        moveSound.play();
    }

    public void playCaptureSound() {
        captureSound.play();
    }
}
