package com.frisian_draught;

import com.frisian_draught.board.GameInfo;
import com.frisian_draught.board.MainBoard;
import com.frisian_draught.util.Buttons;
import com.frisian_draught.util.ExitChoice;
import com.frisian_draught.util.TileConversion;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * The Tutorial class is responsible for managing the tutorial of the game. It provides a
 * step-by-step guide to the user on how to play the game.
 */
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
    private int totalTutorialSteps = 5;

    /* --------------------------------------------------------------------------------
     *                               CONSTRUCTORS
     * -------------------------------------------------------------------------------- */

    /**
     * Constructor for the Tutorial class
     *
     * @param launcher The launcher object
     */
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

    /**
     * Create the main game board
     *
     * @param root The root pane
     * @param scene The scene
     */
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

    /**
     * Resize the game board to fit the new dimensions
     *
     * @param root The root pane
     */
    private void resizeBoard(Pane root) {
        board = mainBoard.resizeBoard(mainBoardSize);
        board.setLayoutX(mainBoardX);
        board.setLayoutY(mainBoardY);
        root.getChildren().add(board);
    }

    /* --------------------------------------------------------------------------------
     *                               PLAYER UI
     * -------------------------------------------------------------------------------- */

    /**
     * Create the player UI If isPlayerOne == false => we position it at the top, else at the bottom
     *
     * @param root The root pane
     * @param scene The scene
     * @param isPlayerOne Whether the player is player one
     */
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

    /**
     * Create the chat UI Not used in the tutorial, but will be in the TODO list
     *
     * @param root The root pane
     * @param scene The scene
     */
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

    /** Show the exit confirmation dialog */
    private void showExitConfirmation() {

        ExitChoice choice = ExitGameConfirmation.showSaveConfirmation(false);

        switch (choice) {
            case EXIT_WITH_SAVE:
            case EXIT_WITHOUT_SAVE:
                if (Launcher.menuStage == null) {
                    launcher.showMenu();
                }

                fadeOutAndClose(gameStage, 300);
                gameStage.close();
                break;

            case NOT_EXIT:
                break;
        }
    }

    /* --------------------------------------------------------------------------------
     *                           CONTROL BUTTONS
     * -------------------------------------------------------------------------------- */

    /**
     * Create the control buttons for the Game Allowing the user to reset the tutorial, go to the
     * next or previous step, open settings
     *
     * @param root The root pane
     * @param scene The scene
     */
    private void buttonGameLogic(Pane root, Scene scene) {
        VBox controlButtons = new VBox();
        controlButtons.setSpacing(buttonSpacing);
        controlButtons.setLayoutX(controlButtonsX);
        controlButtons.setLayoutY(controlButtonsY);

        Buttons resetButton =
                new Buttons(
                        "Reset Tutorial",
                        buttonWidth,
                        buttonHeight,
                        () -> {
                            tutorialReset();
                        });

        Buttons nextButton = new Buttons("Next", buttonWidth, buttonHeight, () -> tutorialNext());

        Buttons previousButton =
                new Buttons("Previous", buttonWidth, buttonHeight, () -> tutorialPrevious());

        Buttons settingsButton =
                new Buttons("Settings", buttonWidth, buttonHeight, Launcher.settings::show);
        Buttons exitButton =
                new Buttons("Exit", buttonWidth, buttonHeight, this::showExitConfirmation);

        controlButtons
                .getChildren()
                .addAll(
                        resetButton.getButton(),
                        nextButton.getButton(),
                        previousButton.getButton(),
                        settingsButton.getButton(),
                        exitButton.getButton());
        root.getChildren().addAll(controlButtons);
    }

    /* --------------------------------------------------------------------------------
     *                            TUTORIAL LOGIC
     * -------------------------------------------------------------------------------- */

    /** Tutorial steps Not all implemented yet */
    public void tutorialSteps() {

        System.out.println("Tutorial begins");
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

    /**
     * Start the tutorial If the tutorial has already been started, ask the user if they want to
     * reset it
     */
    public void tutorialStart() {
        if (currentTutorialStep == 0) {
            currentTutorialStep = 1;
            tutorialLesson1();
        } else {
            tutorialReset();
        }
    }

    /** End the tutorial Ask the user if they want to play the game or restart the tutorial */
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

    /** Reset the tutorial Ask the user if they want to reset their progress */
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

    /** Proceed to the next tutorial step */
    public void tutorialNext() {
        if (currentTutorialStep < totalTutorialSteps) { // Ensure it doesn't go above total steps
            currentTutorialStep++;
        } else {
            tutorialEnd();
        }
        tutorialSteps();
    }

    /** Go back to the previous tutorial step */
    public void tutorialPrevious() {
        if (currentTutorialStep > 1) { // Ensure it doesn't go below step 1
            currentTutorialStep--;
        }
        tutorialSteps();
    }

    /**
     * Initialize the board for the given tutorial step
     *
     * @param step The tutorial step
     */
    private void initializeBoardForTutorialStep(int step) {
        try {
            // Define the home directory path for the tutorial files
            String homeDir = System.getProperty("user.home");
            Path tutorialDir = Paths.get(homeDir, ".frisian-draught", "tutorial");

            // Define the PDN file name and target path
            String pdnFileName = String.format("tutorial%d.pdn", step);
            Path pdnFilePath = tutorialDir.resolve(pdnFileName);

            if (!Files.exists(pdnFilePath)) {
                throw new IllegalArgumentException("PDN file not found: " + pdnFilePath);
            }

            // Load the board state from the PDN file
            mainBoard.loadGameFromPDN(pdnFilePath.toString());
            mainBoard.boardRendered.renderBoard();
            mainBoard.boardRendered.renderPawns(true);
            mainBoard.moveManager.switchTurn(true);

        } catch (Exception e) {
            System.err.println(
                    "Error loading tutorial PDN file for step " + step + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Alert the user that they have passed the tutorial step */
    public void tutorialPassedAlert() {
        Platform.runLater(
                () -> {
                    // Inform the user that they have passed the tutorial
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Tutorial Lesson Passed");
                    alert.setHeaderText("Congratulations!");
                    alert.setContentText("You have completed this step of the tutorial.");
                    alert.showAndWait();
                    tutorialNext(); // Proceed to the next tutorial step
                });
    }

    /** Tutorial Lesson 1: Introducing the game board */
    public void tutorialLesson1() {
        System.out.println("Tutorial Lesson 1: Introducing the game board.");

        // Initialize the board for tutorial step 1
        initializeBoardForTutorialStep(1);

        mainBoard.moveManager.setOnPawnMovedCallback(
                (_, _, _) -> {
                    Launcher.user.completedTutorialStage();
                    System.out.println("Tutorial passed");
                    // Pawn moved. Automatic pass
                    tutorialPassedAlert();
                });

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tutorial Lesson 1");
        alert.setHeaderText("Welcome to the Tutorial!");
        alert.setContentText("Please try to move the white piece from the highlighted square.");
        alert.showAndWait();
    }

    /** Tutorial Lesson 2: Teaching captures */
    public void tutorialLesson2() {
        System.out.println("Tutorial Lesson 2: Teaching captures.");

        // Initialize the board for tutorial step 2
        initializeBoardForTutorialStep(2);

        // Define callback to check for a successful capture
        mainBoard.moveManager.setOnPawnMovedCallback(
                (_, _, isCapture) -> {
                    if (isCapture) {
                        Launcher.user.completedTutorialStage();
                        tutorialPassedAlert();
                    }
                });

        // Prompt the user
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tutorial Lesson 2");
        alert.setHeaderText("Learning Captures!");
        alert.setContentText("Move the white piece to capture the black piece.");
        alert.showAndWait();
    }

    /** Tutorial Lesson 3: Teaching multiple captures */
    public void tutorialLesson3() {
        System.out.println("Tutorial Lesson 3: Teaching multiple captures.");

        // Initialize the board for tutorial step 3
        initializeBoardForTutorialStep(3);

        // Define callback to check for successful multiple captures
        mainBoard.moveManager.setOnPawnMovedCallback(
                (_, _, isCapture) -> {
                    Launcher.user.completedTutorialStage();
                    if (isCapture) {
                        tutorialPassedAlert();
                    }
                });

        // Prompt the user
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tutorial Lesson 3");
        alert.setHeaderText("Learning Multiple Captures!");
        alert.setContentText("Capture multiple pieces by making consecutive jumps.");
        alert.showAndWait();
    }

    /** Tutorial Lesson 4: Promoting to King */
    public void tutorialLesson4() {
        System.out.println("Tutorial Lesson 4: Promoting to King.");

        // Initialize the board for tutorial step 4
        initializeBoardForTutorialStep(4);

        // Define callback to check for promotion
        mainBoard.moveManager.setOnPawnMovedCallback(
                (_, newPosition, _) -> {
                    Launcher.user.completedTutorialStage();
                    if (TileConversion.getTileNotation(newPosition)
                            <= 5) { // If moved to last field
                        tutorialPassedAlert();
                    }
                });

        // Prompt the user
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tutorial Lesson 4");
        alert.setHeaderText("Learning Promotion to King!");
        alert.setContentText(
                "Move your piece to the opposite end of the board to promote it to a King.");
        alert.showAndWait();
    }

    /** Tutorial Lesson 5: Capturing with a King */
    public void tutorialLesson5() {
        System.out.println("Tutorial Lesson 5: Capturing with a King.");

        // Initialize the board for tutorial step 5
        initializeBoardForTutorialStep(5);

        // Define callback to check for a successful capture with a king
        mainBoard.moveManager.setOnPawnMovedCallback(
                (pawn, _, isCapture) -> {
                    Launcher.user.completedTutorialStage();
                    if (isCapture && pawn.isKing()) { // Ensures the capture is performed by a king
                        tutorialPassedAlert();
                    }
                });

        // Prompt the user
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tutorial Lesson 5");
        alert.setHeaderText("Capturing with a King!");
        alert.setContentText("Use your King to capture an opponent's piece.");
        alert.showAndWait();
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

    /**
     * Resize the game elements when the window is resized
     *
     * @param root The root pane
     * @param scene The scene
     */
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

    /**
     * Calculate the new dimensions for the given scene
     *
     * @param scene The scene
     */
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

    /**
     * Convert dimensions from the reference dimensions to the new dimensions
     *
     * @param oldDimension The old dimension
     * @param newDimension The new dimension
     * @param oldReferenceDimension The old reference dimension
     * @return The converted dimension
     */
    private int convertDimensions(int oldDimension, int newDimension, int oldReferenceDimension) {
        return (int)
                ((double) oldDimension * ((double) newDimension / (double) oldReferenceDimension));
    }

    /* --------------------------------------------------------------------------------
     *                           ANIMATION HELPERS
     * -------------------------------------------------------------------------------- */

    /**
     * Smooth fade-in of the given parent node over a specified duration (in ms).
     *
     * @param parent The parent node to fade in
     * @param durationMs The duration of the fade-in animation in milliseconds
     */
    private void animateFadeIn(Pane parent, int durationMs) {
        parent.setOpacity(0); // Start fully transparent
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), parent);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * Smooth fade-out of the given stage over a specified duration (in ms), followed by closing the
     * stage.
     *
     * @param stage The stage to fade out and close
     * @param durationMs The duration of the fade-out animation in milliseconds
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
