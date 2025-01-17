package com.um_project_game;

import com.um_project_game.board.Bot.BotType;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

public class BotPicker {

    private BotType playerBot;
    private BotType botWhite;
    private BotType botBlack;

    public BotType selectPlayerBot() {
        createBotPickerWithDialog("Choose your bot:", bot -> playerBot = bot);
        return playerBot;
    }

    public Pair<BotType, BotType> selectBotvsBot() {
        createDualBotPickerWithDialog(
                "Choose bots for bot vs bot:",
                white -> botWhite = white,
                black -> botBlack = black);
        return new Pair<>(botWhite, botBlack);
    }

    private void createBotPickerWithDialog(String title, BotSelectionHandler handler) {
        Dialog<BotType> dialog = new Dialog<>();
        dialog.setTitle(title);

        // Add stylesheets for dark or light themes
        dialog.getDialogPane()
                .getScene()
                .getStylesheets()
                .add(
                        getClass()
                                .getResource(
                                        Launcher.DARK_MODE ? "/dark-theme.css" : "/light-theme.css")
                                .toExternalForm());
        dialog.getDialogPane().getStyleClass().add("alert-root");

        // Create components
        Label label = new Label(title);
        label.getStyleClass().add("header-label");

        ChoiceBox<BotType> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(BotType.values());
        choiceBox.setValue(BotType.DQN_BOT); // Default selection
        choiceBox.getStyleClass().add("choice-box");

        Button confirmButton = new Button("Select");
        confirmButton.setOnAction(
                _ -> {
                    if (choiceBox.getValue() != null) {
                        handler.handle(choiceBox.getValue());
                        dialog.setResult(choiceBox.getValue());
                        dialog.close();
                    } else {
                        label.setText("Please select a bot."); // Error message
                        label.getStyleClass().add("error-label"); // Error message style
                    }
                });
        confirmButton.getStyleClass().add("button");

        VBox layout = createLayout(label, choiceBox, confirmButton);
        dialog.getDialogPane().setContent(layout);
        dialog.showAndWait();
    }

    private void createDualBotPickerWithDialog(
            String title, BotSelectionHandler whiteHandler, BotSelectionHandler blackHandler) {
        Dialog<Pair<BotType, BotType>> dialog = new Dialog<>();
        dialog.setTitle(title);

        // Add stylesheets for dark or light themes
        dialog.getDialogPane()
                .getScene()
                .getStylesheets()
                .add(
                        getClass()
                                .getResource(
                                        Launcher.DARK_MODE ? "/dark-theme.css" : "/light-theme.css")
                                .toExternalForm());
        dialog.getDialogPane().getStyleClass().add("alert-root");

        // Create components
        Label label = new Label(title);
        label.getStyleClass().add("header-label");

        Label whiteLabel = new Label("White bot:");
        whiteLabel.getStyleClass().add("message-label");

        ChoiceBox<BotType> whiteChoiceBox = new ChoiceBox<>();
        whiteChoiceBox.getItems().addAll(BotType.values());
        whiteChoiceBox.setValue(BotType.DQN_BOT); // Default selection
        whiteChoiceBox.getStyleClass().add("choice-box");

        Label blackLabel = new Label("Black bot:");
        blackLabel.getStyleClass().add("message-label");

        ChoiceBox<BotType> blackChoiceBox = new ChoiceBox<>();
        blackChoiceBox.getItems().addAll(BotType.values());
        blackChoiceBox.setValue(BotType.MINIMAX_BOT); // Default selection
        blackChoiceBox.getStyleClass().add("choice-box");

        Button confirmButton = new Button("Select");
        confirmButton.setOnAction(
                _ -> {
                    if (whiteChoiceBox.getValue() != null && blackChoiceBox.getValue() != null) {
                        whiteHandler.handle(whiteChoiceBox.getValue());
                        blackHandler.handle(blackChoiceBox.getValue());
                        dialog.setResult(
                                new Pair<>(whiteChoiceBox.getValue(), blackChoiceBox.getValue()));
                        dialog.close();
                    } else {
                        label.setText("Please select bots for both sides."); // Error message
                        label.getStyleClass().add("error-label");
                    }
                });
        confirmButton.getStyleClass().add("button");

        VBox layout =
                createLayout(
                        label,
                        whiteLabel,
                        whiteChoiceBox,
                        blackLabel,
                        blackChoiceBox,
                        confirmButton);
        dialog.getDialogPane().setContent(layout);
        dialog.showAndWait();
    }

    private VBox createLayout(Node... components) {
        VBox layout = new VBox(15); // VBox with consistent spacing
        layout.getChildren().addAll(components);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("dialog-content");
        return layout;
    }

    public String getPlayerBotSelection() {
        return "Player selected: " + (playerBot != null ? playerBot.name() : "No bot selected");
    }

    public String getBotvsBotSelection() {
        return "White bot: "
                + (botWhite != null ? botWhite.name() : "No bot selected")
                + ", Black bot: "
                + (botBlack != null ? botBlack.name() : "No bot selected");
    }

    @FunctionalInterface
    private interface BotSelectionHandler {
        void handle(BotType botType);
    }
}
