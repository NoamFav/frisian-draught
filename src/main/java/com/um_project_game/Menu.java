package com.um_project_game;

import com.um_project_game.board.MainBoard;
import com.um_project_game.board.MovesListManager;
import com.um_project_game.util.Buttons;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class Menu {

    private final Pane menuRoot;
    private final Launcher launcher;

    // Dimensions (initialized with defaults)
    private int topBarHeight = 75;
    private int bottomBarHeight = 55;
    private int buttonWidth = 510;
    private int buttonHeight = 60;

    private int controlButtonsX = 75;
    private int controlButtonsY = 115;
    private int controlButtonsSpacing = 15;

    private int recentBoardsX = 75;
    private int recentBoardsY = 422;
    private int recentBoardsSpacingY = 15;
    private int recentBoardsSize = 225;
    private int recentBoardsSpacingX = 105;

    private int liveGameX = 695;
    private int liveGameY = 120;
    private int liveGameSpacing = 15;
    private int liveGameSize = 225;

    private int versionStatusX = 1030;
    private int versionStatusY = 99;
    private int versionStatusWidth = 300;
    private int versionStatusHeight = 570;

    /** Constructor */
    public Menu(Pane root, Scene scene, Launcher launcher) {
        this.menuRoot = root;
        this.launcher = launcher;
        rebuildLayout(scene, root);
    }

    /**
     * @return The root pane containing the menu.
     */
    public Pane getMenuRoot() {
        return menuRoot;
    }

    /** Rebuilds the entire menu layout (useful for first load and resizing). */
    private void rebuildLayout(Scene scene, Pane root) {
        // Remove old nodes
        removeExistingNodes(root);

        // Update dimension fields based on the new window size
        updateDimensions(scene);

        // Build everything fresh
        initTopBar(scene, root);
        initBottomBar(scene, root);
        initMenuButtons(scene, root);
        initRecentGames(scene, root);
        initLiveGame(scene, root);
        initVersionStatus(scene, root);
    }

    /** Initializes top bar. */
    private void initTopBar(Scene scene, Pane root) {
        // Use a helper to create a stacked bar with text
        StackPane topBarPane =
                createStackedBar(
                        0, 0, (int) scene.getWidth(), topBarHeight, "Frisian draughts", "top-bar");
        root.getChildren().add(topBarPane);
    }

    /** Initializes bottom bar. */
    private void initBottomBar(Scene scene, Pane root) {
        StackPane bottomBarPane =
                createStackedBar(
                        0,
                        (int) (scene.getHeight() - bottomBarHeight),
                        (int) scene.getWidth(),
                        bottomBarHeight,
                        "© 2024 UM Project - Version 2.1.4",
                        "bottom-bar");
        root.getChildren().add(bottomBarPane);
    }

    /** Initializes main menu buttons. */
    private void initMenuButtons(Scene scene, Pane root) {
        VBox controlButtons = new VBox(controlButtonsSpacing);
        controlButtons.setLayoutX(controlButtonsX);
        controlButtons.setLayoutY(controlButtonsY);
        controlButtons.setId("control-buttons");

        // Example no-op
        Runnable noAction = () -> {};

        Buttons playLocalButton =
                new Buttons(
                        "Play Local",
                        buttonWidth,
                        buttonHeight,
                        () -> showLocalOptions(scene, root));
        Buttons multiplayerButton =
                new Buttons(
                        "Multiplayer",
                        buttonWidth,
                        buttonHeight,
                        () -> launcher.startNewGame(true, false, false));
        Buttons tutorialButton = new Buttons("Tutorial", buttonWidth, buttonHeight, noAction);
        Buttons settingsButton =
                new Buttons("Settings", buttonWidth, buttonHeight, Launcher.settings::show);

        controlButtons
                .getChildren()
                .addAll(
                        playLocalButton.getButton(),
                        multiplayerButton.getButton(),
                        tutorialButton.getButton(),
                        settingsButton.getButton());

        root.getChildren().add(controlButtons);
    }

    /** Shows the "play local" options, replacing the main menu buttons. */
    private void showLocalOptions(Scene scene, Pane root) {
        removeNodeById(root, "#control-buttons");

        VBox playLocalOptions = new VBox(controlButtonsSpacing);
        playLocalOptions.setLayoutX(controlButtonsX);
        playLocalOptions.setLayoutY(controlButtonsY);
        playLocalOptions.setId("play-local-options");

        Buttons pvpButton =
                new Buttons(
                        "Player against Player",
                        buttonWidth,
                        buttonHeight,
                        () -> launcher.startNewGame(false, false, false));
        Buttons pvBotButton =
                new Buttons(
                        "Player against Bot",
                        buttonWidth,
                        buttonHeight,
                        () -> launcher.startNewGame(false, true, false));
        Buttons botvBotButton =
                new Buttons(
                        "Random Bot against Bot",
                        buttonWidth,
                        buttonHeight,
                        () -> launcher.startNewGame(false, true, true));
        Buttons backButton =
                new Buttons("Back", buttonWidth, buttonHeight, () -> returnToMainMenu(scene, root));

        playLocalOptions
                .getChildren()
                .addAll(
                        pvpButton.getButton(),
                        pvBotButton.getButton(),
                        botvBotButton.getButton(),
                        backButton.getButton());

        root.getChildren().add(playLocalOptions);
    }

    /** Returns to main menu by removing the local options and re-adding main menu buttons. */
    private void returnToMainMenu(Scene scene, Pane root) {
        removeNodeById(root, "#play-local-options");
        initMenuButtons(scene, root);
    }

    /** Initializes the "Recent Games" boards. */
    private void initRecentGames(Scene scene, Pane root) {
        VBox recentContainer = new VBox(recentBoardsSpacingY);
        recentContainer.setLayoutX(recentBoardsX);
        recentContainer.setLayoutY(recentBoardsY);
        recentContainer.setId("recent-boards");

        Text title = new Text("Recent Boards");
        title.getStyleClass().add("label");

        HBox recentGames = new HBox(recentBoardsSpacingX);

        // Find PDN files in user's FrisianDraughtsExports folder
        Path exportPath = Paths.get(System.getProperty("user.home"), "FrisianDraughtsExports");
        File directory = exportPath.toFile();

        if (directory.exists() && directory.isDirectory()) {
            File[] pdnFiles = directory.listFiles((_, name) -> name.endsWith(".pdn"));
            if (pdnFiles != null) {
                // Sort in descending order by filename
                Arrays.sort(pdnFiles, Comparator.comparing(File::getName).reversed());

                // Load up to 3 recent boards
                for (int i = 0; i < Math.min(3, pdnFiles.length); i++) {
                    File pdnFile = pdnFiles[i];

                    MainBoard mainBoard = new MainBoard();
                    mainBoard.setMovesListManager(new MovesListManager(new GridPane()));

                    // Render the board
                    recentGames
                            .getChildren()
                            .add(
                                    mainBoard.getRandomBoard(
                                            root, recentBoardsSize, pdnFile.getPath()));

                    // Click to load game if toggle is ready
                    if (Launcher.isRecentGameToggleReady) {
                        mainBoard
                                .getBoard()
                                .setOnMouseClicked(e -> launcher.startNewGame(mainBoard));
                    } else {
                        mainBoard
                                .getBoard()
                                .setOnMouseClicked(
                                        _ ->
                                                System.out.println(
                                                        "Currently unavailable, will be soon"));
                    }
                }
            }
        }

        recentContainer.getChildren().addAll(title, recentGames);
        root.getChildren().add(recentContainer);
    }

    /** Initializes the "Live Game" board preview. */
    private void initLiveGame(Scene scene, Pane root) {
        VBox liveGame = new VBox(liveGameSpacing);
        liveGame.setLayoutX(liveGameX);
        liveGame.setLayoutY(liveGameY);
        liveGame.setAlignment(Pos.CENTER);
        liveGame.setId("live-game");

        Text liveGameTitle = new Text("Live Game");
        liveGameTitle.getStyleClass().add("label");

        MainBoard mainBoard = new MainBoard();
        GridPane liveBoard = mainBoard.getRandomBoard(root, liveGameSize);

        liveGame.getChildren().addAll(liveGameTitle, liveBoard);
        root.getChildren().add(liveGame);
    }

    /** Initializes the "Version Status" section (rectangle + text). */
    private void initVersionStatus(Scene scene, Pane root) {
        Rectangle versionRect =
                new Rectangle(
                        versionStatusX, versionStatusY, versionStatusWidth, versionStatusHeight);
        versionRect.setFill(Color.TRANSPARENT);

        Text versionText = new Text("Version Status");
        versionText.getStyleClass().add("label");

        StackPane versionPane = new StackPane(versionRect, versionText);
        versionPane.setLayoutX(versionStatusX);
        versionPane.setLayoutY(versionStatusY);
        versionPane.getStyleClass().add("version-status");
        versionPane.setId("version-status");

        root.getChildren().add(versionPane);
    }

    /** Called when the window is resized. Rebuilds the entire layout using updated dimensions. */
    public void onResize(Pane root, Scene scene) {
        rebuildLayout(scene, root);
    }

    /** Updates dimension fields based on the current Scene width/height. */
    private void updateDimensions(Scene scene) {
        int newWidth = (int) scene.getWidth();
        int newHeight = (int) scene.getHeight();

        int refWidth = Launcher.REF_WIDTH;
        int refHeight = Launcher.REF_HEIGHT;

        topBarHeight = scaleDimension(75, newHeight, refHeight);
        bottomBarHeight = scaleDimension(55, newHeight, refHeight);
        buttonWidth = scaleDimension(510, newWidth, refWidth);
        buttonHeight = scaleDimension(60, newHeight, refHeight);

        controlButtonsX = scaleDimension(75, newWidth, refWidth);
        controlButtonsY = scaleDimension(115, newHeight, refHeight);
        controlButtonsSpacing = scaleDimension(15, newHeight, refHeight);

        recentBoardsX = scaleDimension(75, newWidth, refWidth);
        recentBoardsY = scaleDimension(422, newHeight, refHeight);
        recentBoardsSpacingY = scaleDimension(15, newHeight, refHeight);
        recentBoardsSize = scaleDimension(225, newHeight, refHeight);
        recentBoardsSpacingX = scaleDimension(105, newWidth, refWidth);

        liveGameX = scaleDimension(695, newWidth, refWidth);
        liveGameY = scaleDimension(120, newHeight, refHeight);
        liveGameSpacing = scaleDimension(15, newHeight, refHeight);
        liveGameSize = scaleDimension(225, newHeight, refHeight);

        versionStatusX = scaleDimension(1030, newWidth, refWidth);
        versionStatusY = scaleDimension(99, newHeight, refHeight);
        versionStatusWidth = scaleDimension(300, newWidth, refWidth);
        versionStatusHeight = scaleDimension(570, newHeight, refHeight);
    }

    /** Helper to scale a single dimension. */
    private int scaleDimension(int original, int newVal, int refVal) {
        return (int) (original * (double) newVal / (double) refVal);
    }

    /** Removes all major nodes (bars, buttons, boards, etc.) from the root. */
    private void removeExistingNodes(Pane root) {
        String[] ids = {
            "#top-bar",
            "#bottom-bar",
            "#control-buttons",
            "#recent-boards",
            "#live-game",
            "#version-status",
            "#play-local-options"
        };
        for (String id : ids) {
            removeNodeById(root, id);
        }
    }

    /** Utility to remove a single node by its CSS ID. */
    private void removeNodeById(Pane root, String cssId) {
        Node node = root.lookup(cssId);
        if (node != null) {
            root.getChildren().remove(node);
        }
    }

    /** Helper to create a stack pane bar with background rectangle + centered text. */
    private StackPane createStackedBar(
            int x, int y, int width, int height, String textContent, String barId) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect.setId(barId);

        Text text = new Text(textContent);
        text.getStyleClass().add("label");

        StackPane stack = new StackPane(rect, text);
        stack.setId(barId);
        stack.setLayoutX(x);
        stack.setLayoutY(y);
        return stack;
    }
}
