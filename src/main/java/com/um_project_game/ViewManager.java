package com.um_project_game;

import com.um_project_game.Server.MainServer;
import com.um_project_game.board.MainBoard;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class ViewManager {

    private Pane root;

    public Pane getRoot() {
        return root;
    }

    private Launcher launcher;
    private Menu menu;
    private Scene scene;
    private MainServer server;
    private List<Game> activeGames = new ArrayList<>();
    private int GAME_STATE = 0; // 0 = Menu, 1 = Game, 2 = Multiplayer

    public ViewManager(Pane root, Launcher launcher, Scene scene) {
        this.server = new MainServer();
        this.root = root;
        this.launcher = launcher;
        this.scene = scene;
    }

    public MainServer getServer() {
        return server;
    }

    /**
     * @param state Equals: 0 - Close Server Instance 1 - Online Multiplayer 2 - Offline Player
     *     against Bot 3 - Offline Player against Player 4 - Bot vs Bot
     */
    public void gameStateSwitch(int state) {
        GAME_STATE = state;
        switch (GAME_STATE) {
            case 0:
                if (server.isRunning()) {
                    server.close();
                    System.out.println("Server closed");
                }

                root.getChildren().clear();
                root.getChildren().add(menu.getMenuRoot());
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                // Start a new game
                boolean isMultiplayer = (GAME_STATE == 1);
                boolean isAgainstBot = (GAME_STATE == 2);
                boolean isBotvsBot = (GAME_STATE == 4);
                System.out.println("Starting game");
                System.out.println("Multiplayer: " + isMultiplayer);
                System.out.println("Against Bot: " + isAgainstBot);

                if (isMultiplayer) {
                    System.out.println("Starting multiplayer game");
                    if (server.isRunning()) {
                        server.close();
                        System.out.println("Server closed");
                    }
                    server = new MainServer(); // Create a new server instance
                    System.out.println("Starting server");
                    Thread serverThread = new Thread(server);
                    serverThread.setDaemon(true);
                    serverThread.start();
                    System.out.println("Server started");
                } else {
                    if (server != null && server.isRunning()) {
                        server.close();
                        System.out.println("Server closed");
                    }
                }

                Game game = new Game(isMultiplayer, isAgainstBot, isBotvsBot, launcher);

                activeGames.add(game);
                game.showGameWindow();
                break;
            default:
                break;
        }
    }

    public void gameStateSwitch(int state, MainBoard board) {
        GAME_STATE = state;
        switch (GAME_STATE) {
            case 0:
                if (server.isRunning()) {
                    server.close();
                    System.out.println("Server closed");
                }

                root.getChildren().clear();
                root.getChildren().add(menu.getMenuRoot());
                break;
            case 1:
            case 2:
            case 3:
                // Start a new game
                boolean isMultiplayer = (GAME_STATE == 1);
                boolean isAgainstBot = (GAME_STATE == 2);

                System.out.println("Starting game");
                System.out.println("Multiplayer: " + isMultiplayer);
                System.out.println("Against Bot: " + isAgainstBot);

                if (isMultiplayer) {
                    System.out.println("Starting multiplayer game");
                    if (server.isRunning()) {
                        server.close();
                        System.out.println("Server closed");
                    }
                    server = new MainServer(); // Create a new server instance
                    System.out.println("Starting server");
                    Thread serverThread = new Thread(server);
                    serverThread.setDaemon(true);
                    serverThread.start();
                    System.out.println("Server started");
                } else {
                    if (server != null && server.isRunning()) {
                        server.close();
                        System.out.println("Server closed");
                    }
                }

                Game game = new Game(launcher, board);

                activeGames.add(game);
                game.showGameWindow();
                break;
            default:
                break;
        }
    }

    public void closeGame(Game gameToClose) {
        activeGames.remove(gameToClose);
        root.getChildren().remove(gameToClose.getGameRoot());
        if (activeGames.isEmpty()) {
            gameStateSwitch(0); // Go back to menu
        }
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

    public void setServer(MainServer server) {
        this.server = server;
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
