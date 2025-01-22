package com.um_project_game;

import com.um_project_game.board.MainBoard;
import com.um_project_game.board.MovesListManager;
import com.um_project_game.util.Buttons;
import com.um_project_game.util.PawnImages;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class Menu {

    private final Pane menuRoot;
    private final Launcher launcher;

    // Dimensions
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

        // Optional: fade in the entire menu on load
        animateFadeIn(root, 300);
    }

    /**
     * @return The root pane containing the menu.
     */
    public Pane getMenuRoot() {
        return menuRoot;
    }

    /**
     * Called when the window is resized. Rebuilds the entire layout using updated dimensions, then
     * animates in.
     */
    public void onResize(Pane root, Scene scene) {
        rebuildLayout(scene, root);
        animateFadeIn(root, 300);
    }

    /** Rebuilds the entire menu layout (useful for first load and resizing). */
    private void rebuildLayout(Scene scene, Pane root) {
        removeExistingNodes(root);
        updateDimensions(scene);

        initTopBar(scene, root);
        initBottomBar(scene, root);
        initMenuButtons(scene, root);
        initRecentGames(scene, root);
        initLiveGame(scene, root);

        initPlayerStatus(scene, root, Launcher.user);
    }

    /** Initializes top bar. */
    private void initTopBar(Scene scene, Pane root) {
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
                        "© 2024 UM Project - Version 3.5.2",
                        "bottom-bar");
        root.getChildren().add(bottomBarPane);
    }

    /** Initializes main menu buttons. */
    private void initMenuButtons(Scene scene, Pane root) {
        VBox controlButtons = new VBox(controlButtonsSpacing);
        controlButtons.setLayoutX(controlButtonsX);
        controlButtons.setLayoutY(controlButtonsY);
        controlButtons.setId("control-buttons");

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
        Buttons tutorialButton =
                new Buttons("Tutorial", buttonWidth, buttonHeight, () -> launcher.launchTutorial());
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

        // Optional: If you want to animate the menu button container itself on hover
        animateHoverScale(controlButtons, 1.02);
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

        // Optional: Animate new local options container
        animateFadeIn(playLocalOptions, 250);
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
        Path exportPath =
                Paths.get(
                        System.getProperty("user.home"),
                        ".frisian-draught",
                        "FrisianDraughtsExports");
        File directory = exportPath.toFile();

        if (directory.exists() && directory.isDirectory()) {
            File[] pdnFiles = directory.listFiles((_, name) -> name.endsWith(".pdn"));
            if (pdnFiles != null) {
                Arrays.sort(pdnFiles, Comparator.comparing(File::getName).reversed());

                // Load up to 3 recent boards
                for (int i = 0; i < Math.min(3, pdnFiles.length); i++) {
                    File pdnFile = pdnFiles[i];

                    MainBoard mainBoard = new MainBoard();
                    mainBoard.setMovesListManager(new MovesListManager(new GridPane()));

                    // Render the board
                    Node boardNode =
                            mainBoard.getRandomBoard(root, recentBoardsSize, pdnFile.getPath());
                    recentGames.getChildren().add(boardNode);

                    mainBoard
                            .getBoard()
                            .setOnMouseClicked(
                                    _ -> {
                                        launcher.startNewGame(mainBoard);
                                        mainBoard.boardState.setActive(true);
                                    });
                    // Click to load game if toggle is ready

                    animateHoverScale(boardNode, 1.03);
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

        animateHoverScale(liveGame, 1.03);
    }

    private void initPlayerStatus(Scene scene, Pane root, UserInfo user) {
        // Create a VBox to hold player info
        VBox playerInfoBox = new VBox(15);
        playerInfoBox.setId("player-info-box");
        playerInfoBox.setAlignment(Pos.CENTER);
        playerInfoBox.setPadding(new Insets(20));
        playerInfoBox.setSpacing(10);

        // Profile Avatar
        ImageView avatarImage = new ImageView(PawnImages.getPawnImage().blackKing());
        avatarImage.setFitWidth(100);
        avatarImage.setFitHeight(100);
        avatarImage.getStyleClass().add("profile-avatar");

        // Profile Labels
        TextField nameField = new TextField(user.getName());
        nameField.getStyleClass().add("profile-name");
        nameField.setPromptText("Enter your name");
        nameField.setOnAction(
                _ -> {
                    user.setName(nameField.getText());
                });

        Label levelLabel = new Label("Level: " + user.getLevel());
        levelLabel.getStyleClass().add("profile-info-label");

        Label experienceLabel = new Label("Experience: " + user.getExperience() + " XP");
        experienceLabel.getStyleClass().add("profile-info-label");

        // Progress Bar for Experience
        ProgressBar experienceProgress = new ProgressBar();

        double progress =
                (double) user.getExperience() / user.getLevelThresholds().get(user.getLevel() + 1);

        experienceProgress.setProgress(Math.min(progress, 1.0));
        experienceProgress.getStyleClass().add("progress-bar");

        Label rankLabel = new Label("Rank: " + user.getRank());
        rankLabel.getStyleClass().add("profile-info-label");

        // Add statistics
        Label gamesPlayedLabel = new Label("Games Played: " + user.getGamesPlayed());
        Label gamesWonLabel = new Label("Games Won: " + user.getGamesWon());
        Label gamesLostLabel = new Label("Games Lost: " + user.getGamesLost());
        Label winStreakLabel = new Label("Win Streak: " + user.getWinStreak());
        Label highestWinStreakLabel =
                new Label("Highest Win Streak: " + user.getHighestWinStreak());

        Label piecesCapturedLabel = new Label("Pieces Captured: " + user.getPiecesCaptured());
        Label kingPromotionsLabel = new Label("King Promotions: " + user.getKingsPromoted());

        //
        gamesPlayedLabel.getStyleClass().add("profile-info-label");
        gamesWonLabel.getStyleClass().add("profile-info-label");
        gamesLostLabel.getStyleClass().add("profile-info-label");
        winStreakLabel.getStyleClass().add("profile-info-label");
        highestWinStreakLabel.getStyleClass().add("profile-info-label");
        piecesCapturedLabel.getStyleClass().add("profile-info-label");
        kingPromotionsLabel.getStyleClass().add("profile-info-label");

        HBox badgesBox = new HBox(10);
        badgesBox.setAlignment(Pos.CENTER);
        badgesBox.setPadding(new Insets(10));

        Label trophiesBadge = new Label("🏆");
        trophiesBadge.getStyleClass().add("badge");

        badgesBox.getChildren().addAll(trophiesBadge);

        // Add all components to the VBox
        playerInfoBox
                .getChildren()
                .addAll(
                        avatarImage,
                        nameField,
                        levelLabel,
                        experienceLabel,
                        experienceProgress,
                        rankLabel,
                        gamesPlayedLabel,
                        gamesWonLabel,
                        gamesLostLabel,
                        winStreakLabel,
                        highestWinStreakLabel,
                        piecesCapturedLabel,
                        kingPromotionsLabel,
                        badgesBox);

        // ScrollPane to handle overflow
        ScrollPane scrollPane = new ScrollPane(playerInfoBox);
        scrollPane.setPrefSize(versionStatusWidth, versionStatusHeight);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("player-scroll-pane");

        // StackPane for the profile card
        StackPane playerStatusPane = new StackPane(scrollPane);
        playerStatusPane.setPrefSize(versionStatusWidth, versionStatusHeight);
        playerStatusPane.setLayoutX(versionStatusX);
        playerStatusPane.setLayoutY(versionStatusY);
        playerStatusPane.getStyleClass().add("player-status-card");
        playerStatusPane.setId("player-status-card");

        // Add the profile card to the root
        root.getChildren().add(playerStatusPane);

        // Animate hover effect
        animateHoverScale(playerStatusPane, 1.02);
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
            "#player-info-box",
            "#player-status-card"
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

    /* --------------------------------------------------------------------------------
     *                          ANIMATION HELPERS
     * -------------------------------------------------------------------------------- */

    /** Fades in the given node over the specified duration (ms). */
    private void animateFadeIn(Node node, int durationMs) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * Scales up the node slightly on hover, returning to normal when the mouse leaves.
     *
     * @param node - Node to add hover effect
     * @param scaleFactor - e.g., 1.02 for a subtle effect, 1.1 for more noticeable
     */
    private void animateHoverScale(Node node, double scaleFactor) {
        node.setOnMouseEntered(
                _ -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
                    st.setToX(scaleFactor);
                    st.setToY(scaleFactor);
                    st.play();
                });
        node.setOnMouseExited(
                _ -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });
    }
}
