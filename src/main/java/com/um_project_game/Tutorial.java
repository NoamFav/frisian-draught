package com.um_project_game;

import com.um_project_game.board.GameInfo;
import com.um_project_game.board.MainBoard;
import com.um_project_game.util.Buttons;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.joml.Vector2i;

import java.net.URL;
import java.util.Optional;

public class Tutorial {

    private MainBoard mainBoard = new MainBoard();

    public MainBoard getMainBoard() {
        return mainBoard;
    }

    private GridPane board;
    private GameInfo gameInfo = new GameInfo();

    private PauseTransition resizePause;

    private Pane gameRoot;
    private Launcher launcher;
    private Stage gameStage;

    // Dimensions
    private int mainBoardSize = 614;
    private int mainBoardX = 376;
    private int mainBoardY = 77;

    private int playerUIHeight = 60;
    private int playerInfoSpacing = 100;

    private int chatUIHeight = 570;
    private int chatUIWidth = 252;
    private int chatUIX = 32;
    private int chatUIY = 99;

    private int buttonWidth = 269;
    private int buttonHeight = 50;
    private int buttonSpacing = 12;

    private int controlButtonsX = 1069;
    private int controlButtonsY = 99;

    private GridPane movesListGridPane = new GridPane();

    private int currentTutorialStep = 0;
    private int totalTutorialSteps = 10;

    /* --------------------------------------------------------------------------------
     *                               CONSTRUCTORS
     * -------------------------------------------------------------------------------- */

    public Tutorial(Launcher launcher) {
        this.launcher = launcher;
        this.gameStage = new Stage();
        this.gameStage.setTitle("Frisian Draughts - Game");

        this.gameRoot = new Pane();
        Scene scene = new Scene(gameRoot, Launcher.REF_WIDTH, Launcher.REF_HEIGHT);
        Launcher.registerScene(scene);

        // Load CSS
        URL cssUrl =
                getClass().getResource(Launcher.DARK_MODE ? "/dark-theme.css" : "/light-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Stylesheet not found");
        }

        this.gameStage.setScene(scene);

        mainGameBoard(gameRoot, scene);

        playerUI(gameRoot, scene, true);
        playerUI(gameRoot, scene, false);
        chatUI(gameRoot, scene);
        buttonGameLogic(gameRoot, scene);

        // Fade in effect on first load
        animateFadeIn(gameRoot, 300);

        // Debounced resizing
        resizePause = new PauseTransition(Duration.millis(50));
        resizePause.setOnFinished(_ -> onResize(gameRoot, scene));

        scene.widthProperty().addListener((_, _, _) -> resizePause.playFromStart());
        scene.heightProperty().addListener((_, _, _) -> resizePause.playFromStart());

        // Handle close event
        this.gameStage.setOnCloseRequest(
                e -> {
                    e.consume();
                    showExitConfirmation();
                });
    }

    public void showTutorialWindow() {
        this.gameStage.show();
    }

    public Pane getTutorialRoot() {
        return gameRoot;
    }

    /* --------------------------------------------------------------------------------
     *                               BOARD METHODS
     * -------------------------------------------------------------------------------- */
    private void mainGameBoard(Pane root, Scene scene) {
        board =
                mainBoard.getMainBoard(
                        root,
                        mainBoardSize,
                        new Vector2i(mainBoardX, mainBoardY),
                        gameInfo,
                        movesListGridPane,
                        false,
                        false);
        board.getStyleClass().add("mainboard");
        root.getChildren().add(board);
    }

    private void resizeBoard(Pane root) {
        board = mainBoard.resizeBoard(mainBoardSize);
        board.setLayoutX(mainBoardX);
        board.setLayoutY(mainBoardY);
        root.getChildren().add(board);
    }

    /* --------------------------------------------------------------------------------
     *                               PLAYER UI
     * -------------------------------------------------------------------------------- */
    private void playerUI(Pane root, Scene scene, boolean isPlayerOne) {
        StackPane playerUI = new StackPane();
        playerUI.setPrefSize(scene.getWidth(), playerUIHeight);

        // If isPlayerOne == false => we position it at the top, else at the bottom
        // In your code, it's reversed, so let's keep your original logic:
        playerUI.setLayoutX(0);
        playerUI.setLayoutY(!isPlayerOne ? 0 : scene.getHeight() - playerUIHeight);

        playerUI.getStyleClass().add("playerUI");
        playerUI.setId(isPlayerOne ? "playerOne" : "Teacher");

        Text playerText = new Text(isPlayerOne ? "Player 1" : "Teacher");
        playerText.getStyleClass().add("label");
        playerText.setId(isPlayerOne ? "playerOneText" : "teacherText");

        Text playerScore = new Text();
        playerScore.getStyleClass().add("label");
        playerScore.setId(isPlayerOne ? "playerOneScore" : "teacherScore");
        if (isPlayerOne) {
            playerScore
                    .textProperty()
                    .bind(Bindings.concat("Score: ", gameInfo.scorePlayerOneProperty()));
        } else {
            playerScore
                    .textProperty()
                    .bind(Bindings.concat("Score: ", gameInfo.scorePlayerTwoProperty()));
        }

        HBox playerInfo = new HBox(playerText, playerScore);
        playerInfo.getStyleClass().add("playerInfo");
        playerInfo.setSpacing(playerInfoSpacing);
        playerInfo.setAlignment(javafx.geometry.Pos.CENTER);

        playerUI.getChildren().add(playerInfo);

        root.getChildren().add(playerUI);
    }

    /* --------------------------------------------------------------------------------
     *                                CHAT UI -- INSTRUCTIONS
     * -------------------------------------------------------------------------------- */
    private void chatUI(Pane root, Scene scene) {
        StackPane chatUI = new StackPane();
        chatUI.setPrefSize(chatUIWidth, chatUIHeight);
        chatUI.setLayoutX(chatUIX);
        chatUI.setLayoutY(chatUIY);
        chatUI.getStyleClass().add("chatUI");

        Text chatText = new Text("Chat");
        chatText.getStyleClass().add("label");
        chatUI.getChildren().add(chatText);

        root.getChildren().add(chatUI);
    }

    /* --------------------------------------------------------------------------------
     *                            EXIT CONFIRMATION
     * -------------------------------------------------------------------------------- */
    private void showExitConfirmation() {
        ExitGameConfirmation exitConfirmation = new ExitGameConfirmation();
        if (exitConfirmation.showAndWait()) { // If user confirmed exit

            if (Launcher.menuStage == null) {
                launcher.showMenu();
            }

            fadeOutAndClose(gameStage, 300);

            gameStage.close();
        }
    }

    /* --------------------------------------------------------------------------------
     *                           CONTROL BUTTONS
     * -------------------------------------------------------------------------------- */
    private void buttonGameLogic(Pane root, Scene scene) {
        VBox controlButtons = new VBox();
        controlButtons.setSpacing(buttonSpacing);
        controlButtons.setLayoutX(controlButtonsX);
        controlButtons.setLayoutY(controlButtonsY);

        Buttons undoButton =
                new Buttons(
                        "Undo",
                        buttonWidth,
                        buttonHeight,
                        () -> {
                            mainBoard.moveManager.undoLastMove();
                        });

        Buttons nextButton =
                new Buttons("Next", buttonWidth, buttonHeight, () -> tutorialNext()); // TODO - next

        Buttons previousButton =
                new Buttons(
                        "Previous",
                        buttonWidth,
                        buttonHeight,
                        () -> tutorialPrevious()); // TODO - previous

        Buttons settingsButton =
                new Buttons("Settings", buttonWidth, buttonHeight, Launcher.settings::show);
        Buttons exitButton =
                new Buttons("Exit", buttonWidth, buttonHeight, this::showExitConfirmation);

        controlButtons
                .getChildren()
                .addAll(
                        undoButton.getButton(),
                        nextButton.getButton(),
                        previousButton.getButton(),
                        settingsButton.getButton(),
                        exitButton.getButton());
        root.getChildren().addAll(controlButtons);
    }

    /* --------------------------------------------------------------------------------
     *                            TUTORIAL LOGIC
     * -------------------------------------------------------------------------------- */

    public void tutorialSteps() {
        // TODO - implement tutorial steps
        switch (currentTutorialStep) {
            case 1:
                tutorialLesson1();
                break;
            case 2:
                tutorialLesson2();
                break;
            case 3:
                tutorialLesson3();
                break;
            case 4:
                tutorialLesson4();
                break;
            case 5:
                tutorialLesson5();
                break;
            case 6:
                tutorialLesson6();
                break;
            case 7:
                tutorialLesson7();
                break;
            case 8:
                tutorialLesson8();
                break;
            case 9:
                tutorialLesson9();
                break;
            case 10:
                tutorialLesson10();
                break;
            default:
                break;
        }
    }

    public void tutorialStart() {
        // TODO - implement tutorial start
        if (currentTutorialStep == 0) {
            currentTutorialStep = 1;
            tutorialLesson1();
        } else {
            tutorialReset();
        }
    }

    public void tutorialEnd() {
        currentTutorialStep = 0;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tutorial Complete");
        alert.setHeaderText("Congratulations! You have completed the tutorial.");
        alert.setContentText("You can now play the game or restart the tutorial.");

        ButtonType buttonTypePlay = new ButtonType("Play Game");
        ButtonType buttonTypeRestart = new ButtonType("Restart Tutorial");

        alert.getButtonTypes().setAll(buttonTypePlay, buttonTypeRestart);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypePlay) {
            gameStage.close(); // Close tutorial and start the game
        } else {
            currentTutorialStep = 1;
            tutorialLesson1();
        }
    }

    public void tutorialReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Tutorial");
        alert.setHeaderText("You have previously started the tutorial. Do you want to reset it?");
        alert.setContentText("This will reset your progress.");

        ButtonType buttonTypeYes = new ButtonType("Yes - Reset Tutorial");
        ButtonType buttonTypeNo = new ButtonType("No - Continue Tutorial");

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            currentTutorialStep = 1;
            tutorialLesson1();
        } else {
            tutorialSteps();
        }
    }

    public void tutorialNext() {
        if (currentTutorialStep < totalTutorialSteps) { // Ensure it doesn't go above total steps
            currentTutorialStep++;
        }
        tutorialSteps();
    }

    public void tutorialPrevious() {
        if (currentTutorialStep > 1) { // Ensure it doesn't go below step 1
            currentTutorialStep--;
        }
        tutorialSteps();
    }

    public void tutorialLesson1() {
        // TODO - implement tutorial lesson 1
    }

    public void tutorialLesson2() {
        // TODO - implement tutorial lesson 2
    }

    public void tutorialLesson3() {
        // TODO - implement tutorial lesson 3
    }

    public void tutorialLesson4() {
        // TODO - implement tutorial lesson 4
    }

    public void tutorialLesson5() {
        // TODO - implement tutorial lesson 5
    }

    public void tutorialLesson6() {
        // TODO - implement tutorial lesson 6
    }

    public void tutorialLesson7() {
        // TODO - implement tutorial lesson 7
    }

    public void tutorialLesson8() {
        // TODO - implement tutorial lesson 8
    }

    public void tutorialLesson9() {
        // TODO - implement tutorial lesson 9
    }

    public void tutorialLesson10() {
        // TODO - implement tutorial lesson 10
    }

    /* --------------------------------------------------------------------------------
     *                            RESIZING LOGIC
     * -------------------------------------------------------------------------------- */
    public void onResize(Pane root, Scene scene) {
        root.getChildren().clear();
        newDimension(scene);

        // Rebuild everything
        resizeBoard(root);
        playerUI(root, scene, true);
        playerUI(root, scene, false);
        chatUI(root, scene);
        buttonGameLogic(root, scene);

        // Fade in after resizing
        animateFadeIn(root, 300);
    }

    private void newDimension(Scene scene) {
        int newSceneWidth = (int) scene.getWidth();
        int newSceneHeight = (int) scene.getHeight();

        int referenceWidth = Launcher.REF_WIDTH;
        int referenceHeight = Launcher.REF_HEIGHT;

        mainBoardSize = convertDimensions(614, newSceneWidth, referenceWidth);
        mainBoardX = convertDimensions(376, newSceneWidth, referenceWidth);
        mainBoardY = convertDimensions(77, newSceneHeight, referenceHeight);

        playerUIHeight = convertDimensions(60, newSceneHeight, referenceHeight);
        playerInfoSpacing = convertDimensions(100, newSceneHeight, referenceHeight);

        chatUIHeight = convertDimensions(570, newSceneHeight, referenceHeight);
        chatUIWidth = convertDimensions(252, newSceneWidth, referenceWidth);
        chatUIX = convertDimensions(32, newSceneWidth, referenceWidth);
        chatUIY = convertDimensions(99, newSceneHeight, referenceHeight);

        buttonWidth = convertDimensions(269, newSceneWidth, referenceWidth);
        buttonHeight = convertDimensions(50, newSceneHeight, referenceHeight);
        buttonSpacing = convertDimensions(12, newSceneHeight, referenceHeight);

        controlButtonsX = convertDimensions(1069, newSceneWidth, referenceWidth);
        controlButtonsY = convertDimensions(99, newSceneHeight, referenceHeight);
    }

    private int convertDimensions(int oldDimension, int newDimension, int oldReferenceDimension) {
        return (int)
                ((double) oldDimension * ((double) newDimension / (double) oldReferenceDimension));
    }

    /* --------------------------------------------------------------------------------
     *                           ANIMATION HELPERS
     * -------------------------------------------------------------------------------- */

    /** Smooth fade-in of the given parent node over a specified duration (in ms). */
    private void animateFadeIn(Pane parent, int durationMs) {
        parent.setOpacity(0); // Start fully transparent
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), parent);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * Scale/pulse effect for a node. For example, call this on a player's UI if it's their turn.
     * The node scales up and then back down once.
     */
    private void animatePulse(Pane node, double scaleTo, int durationMs) {
        ScaleTransition st = new ScaleTransition(Duration.millis(durationMs), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(scaleTo);
        st.setToY(scaleTo);
        st.setAutoReverse(true);
        st.setCycleCount(2); // Go up, then back down
        st.play();
    }

    /**
     * Optional: fade out the window before closing. If you want to use it, call
     * fadeOutAndClose(gameStage, 300) in showExitConfirmation().
     */
    private void fadeOutAndClose(Stage stage, int durationMs) {
        FadeTransition ft =
                new FadeTransition(Duration.millis(durationMs), stage.getScene().getRoot());
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(_ -> stage.close());
        ft.play();
    }
}
