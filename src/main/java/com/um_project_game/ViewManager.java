package com.um_project_game;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * The ViewManager class is responsible for managing the views of the application. Handles the
 * gameswitch from one window to the other
 */
public class ViewManager {

    private Pane root;

    public Pane getRoot() {
        return root;
    }

    private Launcher launcher;
    private Menu menu;
    private Scene scene;

    private List<Game> activeGames = new ArrayList<>();
    private int GAME_STATE = 0;

    /**
     * Constructor for the ViewManager class
     *
     * @param root The root pane of the application
     * @param launcher The launcher object
     * @param scene The scene object
     */
    public ViewManager(Pane root, Launcher launcher, Scene scene) {

        this.root = root;
        this.launcher = launcher;
        this.scene = scene;
    }

    /**
     * @param state Equals: 0 - Close Server Instance 1 - Online Multiplayer 2 - Offline Player
     *     against Bot 3 - Offline Player against Player 4 - Bot vs Bot
     */
    public void gameStateSwitch(int state) {
        GAME_STATE = state;
        switch (GAME_STATE) {
            case 0:
                root.getChildren().clear();
                root.getChildren().add(menu.getMenuRoot());
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                // Start a new game
                // Case 1, 2, 3, 4 are all game just with different settings bot wise or multiplayer
                boolean isMultiplayer = (GAME_STATE == 1);
                boolean isAgainstBot = (GAME_STATE == 2);
                boolean isBotvsBot = (GAME_STATE == 4);
                System.out.println("Starting game");
                System.out.println("Multiplayer: " + isMultiplayer);
                System.out.println("Against Bot: " + isAgainstBot);

                Game game = new Game(isMultiplayer, isAgainstBot, isBotvsBot, launcher);

                activeGames.add(game);
                game.showGameWindow();
                break;
            case 5:
                // Start tutorial
                System.out.println("Starting tutorial");
                Tutorial tutorial = new Tutorial(launcher);
                tutorial.showTutorialWindow();
                tutorial.tutorialStart();

                break;
            default:
                break;
        }
    }

    /**
     * Changes the game state to load a game from a file
     *
     * @param filePath The file path of the game to load
     */
    public void gameStateSwitch(String filePath) {
        Game game = new Game(false, false, false, launcher, filePath);

        activeGames.add(game);
        game.showGameWindow();
    }

    /**
     * Closes the game window
     *
     * @param gameToClose The game to close
     */
    public void closeGame(Game gameToClose) {
        activeGames.remove(gameToClose);
        root.getChildren().remove(gameToClose.getGameRoot());
        if (activeGames.isEmpty()) {}
    }

    public void setRoot(Pane root) {
        this.root = root;
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public void setLauncher(Launcher launcher) {
        this.launcher = launcher;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public List<Game> getActiveGames() {
        return activeGames;
    }

    public void setActiveGames(List<Game> activeGames) {
        this.activeGames = activeGames;
    }

    public Game getGame(Game game) {
        return activeGames.get(activeGames.indexOf(game));
    }

    public int getGAME_STATE() {
        return GAME_STATE;
    }

    public void setGAME_STATE(int gAME_STATE) {
        GAME_STATE = gAME_STATE;
    }
}
