package com.um_project_game.board;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GameOverSummaryScreen {

    private final GameInfo gameInfo;
    private final Stage ownerStage;
    private final Runnable onReset;

    public GameOverSummaryScreen(GameInfo gameInfo, Stage ownerStage, Runnable onReset) {
        this.gameInfo = gameInfo;
        this.ownerStage = ownerStage;
        this.onReset = onReset;
    }

    public void display(String winner) {
        Stage summaryStage = new Stage();
        summaryStage.initModality(Modality.APPLICATION_MODAL);
        summaryStage.initOwner(ownerStage);
        summaryStage.setTitle("Game Over - Summary");

        // Game Over Label
        Label gameOverLabel = new Label("Game Over!");
        gameOverLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Winner Label
        Label winnerLabel = new Label("Winner: " + winner);
        winnerLabel.setStyle("-fx-font-size: 18px;");

        // Player Scores
        Label scorePlayerOne = new Label("Player 1 Score: " + gameInfo.getScorePlayerOne());
        Label scorePlayerTwo = new Label("Player 2 Score: " + gameInfo.getScorePlayerTwo());

        // Reset Button
        Button resetButton = new Button("Reset Game");
        resetButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        resetButton.setOnAction(event -> {
            summaryStage.close();
            onReset.run();
        });

        // Layout
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                gameOverLabel,
                winnerLabel,
                scorePlayerOne,
                scorePlayerTwo,
                resetButton
        );

        Scene scene = new Scene(layout, 300, 250);
        summaryStage.setScene(scene);
        summaryStage.show();
    }
}
