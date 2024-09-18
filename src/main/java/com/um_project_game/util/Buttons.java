package com.um_project_game.util;

import javafx.scene.control.Button;

public class Buttons {

    private Button button;

    /**
     * @param text The text to be displayed on the button
     * @param x The x-coordinate of the button
     * @param y The y-coordinate of the button
     * @param width The width of the button
     * @param height The height of the button
     */
    public Buttons(String text, int x, int y, int width, int height) {
        button = new Button(text);
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefWidth(width);
        button.setPrefHeight(height);
    }

    public Button getButton() {
        return button;
    }
}
