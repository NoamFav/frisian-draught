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

/**
 * The BotPicker class provides a user interface for selecting bots for the game.
 * It allows the user to select a bot for the player and bots for a bot vs bot match.
 */
public class BotPicker {

    private BotType playerBot;
    private BotType botWhite;
    private BotType botBlack;

    /**
     * Displays a dialog for selecting the player's bot.
     *
     * @return the selected bot type for the player
     */
    public BotType selectPlayerBot() {
        createBotPickerWithDialog("Choose your bot:", bot -> playerBot = bot);
        return playerBot;
    }

    /**
     * Displays a dialog for selecting bots for a bot vs bot match.
     *
     * @return a pair of selected bot types for white and black
     */
    public Pair<BotType, BotType> selectBotvsBot() {
        createDualBotPickerWithDialog(
                "Choose bots for bot vs bot:",
                white -> botWhite = white,
                black -> botBlack = black);
        return new Pair<>(botWhite, botBlack);
    }

    /**
     * Creates a dialog for selecting a single bot.
     *
     * @param title the title of the dialog
     * @param handler the handler for the selected bot type
     */
    private void createBotPickerWithDialog(String title, BotSelectionHandler handler) {
        Dialog<BotType> dialog = new Dialog<>();
        dialog.setTitle(title);

        dialog.getDialogPane()
                .getScene()
                .getStylesheets()
                .add(
                        getClass()
                                .getResource(
                                        Launcher.DARK_MODE ? "/dark-theme.css" : "/light-theme.css")
                                .toExternalForm());
        dialog.getDialogPane().getStyleClass().add("alert-root");

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

    /**
     * Creates a dialog for selecting two bots for a bot vs bot match.
     *
     * @param title the title of the dialog
     * @param whiteHandler the handler for the selected white bot type
     * @param blackHandler the handler for the selected black bot type
     */
    private void createDualBotPickerWithDialog(
            String title, BotSelectionHandler whiteHandler, BotSelectionHandler blackHandler) {
        Dialog<Pair<BotType, BotType>> dialog = new Dialog<>();
        dialog.setTitle(title);

        dialog.getDialogPane()
                .getScene()
                .getStylesheets()
                .add(
                        getClass()
                                .getResource(
                                        Launcher.DARK_MODE ? "/dark-theme.css" : "/light-theme.css")
                                .toExternalForm());
        dialog.getDialogPane().getStyleClass().add("alert-root");

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

    /**
     * Creates a VBox layout with the specified components.
     *
     * @param components the components to add to the layout
     * @return the created VBox layout
     */
    private VBox createLayout(Node... components) {
        VBox layout = new VBox(15); // VBox with consistent spacing
        layout.getChildren().addAll(components);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("dialog-content");
        return layout;
    }

    /**
     * Returns a string representation of the player's bot selection.
     *
     * @return a string representing the player's bot selection
     */
    public String getPlayerBotSelection() {
        return "Player selected: " + (playerBot != null ? playerBot.name() : "No bot selected");
    }

    /**
     * Returns a string representation of the bot vs bot selection.
     *
     * @return a string representing the bot vs bot selection
     */
    public String getBotvsBotSelection() {
        return "White bot: "
                + (botWhite != null ? botWhite.name() : "No bot selected")
                + ", Black bot: "
                + (botBlack != null ? botBlack.name() : "No bot selected");
    }

    /**
     * Functional interface for handling bot selection.
     */
    @FunctionalInterface
    private interface BotSelectionHandler {
        void handle(BotType botType);
    }
}