package com.um_project_game.util;

import javafx.scene.control.Button;

public class Buttons {

    /**
     * @param text The text to be displayed on the button
     * @param x The x-coordinate of the button
     * @param y The y-coordinate of the button
     * @param width The width of the button
     * @param height The height of the button
     */
    public Buttons(String text, int x, int y, int width, int height) {
        Button button = new Button(text);
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefWidth(width);
        button.setPrefHeight(height);
        button.setStyle("-fx-background-color: " + Colors.colors.get("buttonColor") + "; -fx-text-fill: " + Colors.colors.get("buttonTextColor") + ";");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + Colors.colors.get("buttonHoverColor") + "; -fx-text-fill: " + Colors.colors.get("buttonHoverTextColor") + ";"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + Colors.colors.get("buttonColor") + "; -fx-text-fill: " + Colors.colors.get("buttonTextColor") + ";"));
    }
}
