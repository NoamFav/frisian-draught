package com.um_project_game;

import com.um_project_game.board.MainBoard;
import com.um_project_game.util.SoundPlayer;
import com.um_project_game.util.TomlLoader;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The Launcher class is responsible for launching the application. It is the entry point of the
 * application and is responsible for setting up the initial scene and stage.
 */
public class Launcher extends Application {
    private PauseTransition resizePause;
    public static SoundPlayer soundPlayer = new SoundPlayer();
    public static Settings settings;
    public static final int REF_WIDTH = 1366;
    public static final int REF_HEIGHT = 768;
    public static boolean DARK_MODE = true;
    public static boolean dqnbot = true;
    public static final ViewManager viewManager =
            new ViewManager(
                    new Pane(), new Launcher(), new Scene(new Pane(), REF_WIDTH, REF_HEIGHT));
    public static Scene menuScene;
    private static final List<Scene> scenes = new ArrayList<>();

    public static UserInfo user;

    /**
     * Register a scene with the launcher. This allows the launcher to apply the theme to the scene
     * when the theme is switched.
     *
     * @param scene The scene to register
     */
    public static void registerScene(Scene scene) {
        scenes.add(scene);
        applyTheme(scene);
    }

    /**
     * Switch the theme of the application. This will apply the new theme to all registered scenes.
     */
    public static void switchTheme() {
        Launcher.DARK_MODE = !Launcher.DARK_MODE;
        scenes.forEach(Launcher::applyTheme); // Apply the new theme to all scenes
    }

    public static Stage menuStage;

    /**
     * The start method is called when the application is launched. It is responsible for setting up
     * the initial stage and scene.
     *
     * @param stage The primary stage for the application
     */
    @Override
    public void start(Stage stage) {
        menuStage = stage;
        copyPDNFilesToHomeDirectory();
        try {
            TomlLoader.ensureExternalConfigExists();
        } catch (IOException e) {

            e.printStackTrace();
        }
        user = TomlLoader.loadPlayerInfo();
        settings = new Settings(soundPlayer, viewManager.getRoot(), viewManager.getScene());
        showLoadingScreen(stage);
    }

    /**
     * Show the loading screen while the application is loading. This is a simple loading screen
     * with a rotating circle of dots and a loading text. Technically, this is not a loading screen,
     * but a splash screen.
     *
     * @param stage The stage to show the loading screen on
     */
    private void showLoadingScreen(Stage stage) {
        // Create the root pane and apply the CSS ID
        Pane loadingRoot = new Pane();
        loadingRoot.setId("loading-root");

        // Create the scene and add the CSS stylesheet
        Scene loadingScene = new Scene(loadingRoot, REF_WIDTH, REF_HEIGHT);
        loadingScene
                .getStylesheets()
                .add(
                        getClass()
                                .getResource(DARK_MODE ? "/dark-theme.css" : "/light-theme.css")
                                .toExternalForm());

        // Create a group of dots (dotted circle) and apply a style class
        Group dotCircle = createDottedCircle(REF_WIDTH / 2.0, REF_HEIGHT / 2.0, 50, 12);
        dotCircle.getStyleClass().add("dot-circle");

        // Rotate the dot circle
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(3), dotCircle);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
        rotateTransition.play();

        // Loading Text
        Text loadingText = new Text("Loading...");
        loadingText.setId("loading-text");
        loadingText.setX(REF_WIDTH / 2.0 - 70);
        loadingText.setY(REF_HEIGHT / 2.0 + 100);

        ScaleTransition textPulse = new ScaleTransition(Duration.seconds(1), loadingText);
        textPulse.setFromX(1.0);
        textPulse.setToX(1.2);
        textPulse.setFromY(1.0);
        textPulse.setToY(1.2);
        textPulse.setCycleCount(Animation.INDEFINITE);
        textPulse.setAutoReverse(true);
        textPulse.play();

        // Add everything to the root
        loadingRoot.getChildren().addAll(dotCircle, loadingText);

        // Handle close event on the UI thread
        stage.setOnCloseRequest(
                _ -> {
                    System.out.println("Window is closing...");
                    saveDataToTOML();
                });

        // Global shutdown hook to catch other exits
        // Useful for force quitting the application with CTRL+C
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    System.out.println("Shutdown hook triggered...");
                                    saveDataToTOML();
                                }));

        // Set the scene and show the stage
        stage.setScene(loadingScene);
        stage.show();

        // Simulate loading delay
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(_ -> setupMenuStage(stage));
        pause.play();
    }

    /**
     * Create a dotted circle with a specified center, radius, and number of dots. Trigonometry is
     * used to calculate the positions of the dots around the circle.
     *
     * @param centerX The x-coordinate of the center of the circle
     * @param centerY The y-coordinate of the center of the circle
     * @param radius The radius of the circle
     * @param dotCount The number of dots to create
     * @return A group containing the dots
     */
    private Group createDottedCircle(double centerX, double centerY, double radius, int dotCount) {
        Group group = new Group();
        double angleStep = 360.0 / dotCount;

        for (int i = 0; i < dotCount; i++) {
            double angle = Math.toRadians(i * angleStep);
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            Circle dot = new Circle(5, Color.WHITE); // Small dots
            dot.setCenterX(x);
            dot.setCenterY(y);
            dot.getStyleClass().add("dot");

            // Scale animation to make dots pulse
            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.6), dot);
            scaleTransition.setFromX(1.0);
            scaleTransition.setToX(1.3);
            scaleTransition.setFromY(1.0);
            scaleTransition.setToY(1.3);
            scaleTransition.setCycleCount(Animation.INDEFINITE);
            scaleTransition.setAutoReverse(true);
            scaleTransition.setDelay(Duration.millis(i * 100)); // Stagger the pulse animation
            scaleTransition.play();

            group.getChildren().add(dot);
        }

        return group;
    }

    /**
     * Setup the menu stage. This method is called after the loading screen has finished. It creates
     * the menu and sets up the stage.
     *
     * @param stage The stage to set up
     */
    private void setupMenuStage(Stage stage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, REF_WIDTH, REF_HEIGHT);
        Launcher.registerScene(scene);
        menuScene = scene;
        stage.setTitle("Frisian Draughts - Menu");

        URL cssUrl = getClass().getResource(DARK_MODE ? "/dark-theme.css" : "/light-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        scene.setOnKeyPressed(
                event -> {
                    switch (event.getCode()) {
                        case ESCAPE:
                            ExitGameConfirmation.showExitConfirmation();
                            break;
                        default:
                            break;
                    }
                });

        stage.setScene(scene);

        // Create and display the menu
        Menu menu = new Menu(root, scene, this);
        viewManager.setMenu(menu);
        resizePause = new PauseTransition(Duration.millis(50));
        resizePause.setOnFinished(_ -> menu.onResize(root, scene));
        // Add resize listeners
        scene.widthProperty().addListener((_, _, _) -> resizePause.playFromStart());
        scene.heightProperty().addListener((_, _, _) -> resizePause.playFromStart());

        // Handle close event
        stage.setOnCloseRequest(
                _ -> {
                    // If there are no other windows open, exit the application
                    if (Stage.getWindows().size() <= 1) {
                        // This is the last window, so exit
                        Platform.exit();
                    } else {
                        // Just hide the menu window
                        menuStage = null;
                        stage.hide();
                    }
                });

        stage.show();
    }

    /**
     * Start a new game with the specified parameters.
     *
     * @param isOnline Is Online-Game?
     * @param againstBot Is Against Bot?
     */
    public void startNewGame(boolean isOnline, boolean againstBot, boolean isBotVsBot) {

        startNewGame(isOnline, againstBot, isBotVsBot, null);
    }

    /**
     * Start a new game with the specified parameters.
     *
     * @param isOnline Is Online-Game?
     * @param againstBot Is Against Bot?
     * @param isBotVsBot Is Bot vs Bot?
     * @param mainBoard The main board to load
     */
    public void startNewGame(
            boolean isOnline, boolean againstBot, boolean isBotVsBot, MainBoard mainBoard) {

        // for loaded games
        if (mainBoard != null) {
            // viewManager.gameStateSwitch(2, mainBoard);
        } else if (isOnline) {

            viewManager.gameStateSwitch(1);
        } else if (againstBot) {
            viewManager.gameStateSwitch(isBotVsBot ? 4 : 2);
        } else {
            viewManager.gameStateSwitch(3);
        }
    }

    /** Launch the tutorial. */
    public void launchTutorial() {
        viewManager.gameStateSwitch(5);
    }

    /**
     * Start a new game with the specified file path.
     *
     * @param filePath The file path of the game to load
     */
    public void startNewGame(String filePath) {
        viewManager.gameStateSwitch(filePath);
    }

    /** Close the menu. */
    public void closeMenu() {
        if (menuStage != null) {
            menuStage.close();
            menuStage = null;
        }
    }

    /** Show the menu. */
    public void showMenu() {
        viewManager.gameStateSwitch(0);
        if (menuStage == null) {
            Stage newMenuStage = new Stage();
            menuStage = newMenuStage;
            setupMenuStage(menuStage);
        } else {
            menuStage.show();
        }
    }

    /**
     * Apply the theme to the specified scene. This method will apply the dark theme if the
     * DARK_MODE is true, otherwise it will apply the light theme.
     *
     * @param scene The scene to apply the theme to
     */
    public static void applyTheme(Scene scene) {
        URL cssUrl =
                Launcher.DARK_MODE
                        ? Launcher.class.getResource("/dark-theme.css")
                        : Launcher.class.getResource("/light-theme.css");

        if (cssUrl != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("Applied theme: " + (Launcher.DARK_MODE ? "Dark" : "Light"));
        } else {
            System.err.println("Stylesheet not found.");
        }
    }

    private void copyPDNFilesToHomeDirectory() {
        try {
            // Define the home directory path for storing tutorial files
            String homeDir = System.getProperty("user.home");
            Path tutorialDir = Paths.get(homeDir, ".frisian-draught", "tutorial");

            // Ensure the directory exists
            if (!Files.exists(tutorialDir)) {
                Files.createDirectories(tutorialDir);
            }

            // Iterate through tutorial steps
            for (int step = 1; step <= 10; step++) { // Assuming 10 tutorial files
                String pdnFileName = String.format("tutorial%d.pdn", step);
                Path targetFilePath = tutorialDir.resolve(pdnFileName);

                if (!Files.exists(targetFilePath)) {
                    String pdnFilePath = String.format("/tutorial/%s", pdnFileName);
                    URL pdnFileUrl = getClass().getResource(pdnFilePath);

                    if (pdnFileUrl == null) {
                        System.err.println("PDN file not found: " + pdnFilePath);
                        continue;
                    }

                    Files.copy(Paths.get(pdnFileUrl.toURI()), targetFilePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Error copying tutorial PDN files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Save the user data to the TOML file. */
    public static void saveDataToTOML() {
        TomlLoader.savePlayerInfo(user);
    }

    /**
     * The main method is the entry point of the application. It launches the application.
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
