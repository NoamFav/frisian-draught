package com.um_project_game;

/**
 * The Player class is responsible for managing the players of the game. It stores the name of the
 * player and whether they are white or black. Only used in the server on the multiplayer mode
 */
public class Player {
    private String name;

    private boolean isWhite;
    private boolean isSpectator;

    /**
     * Constructor for the Player class
     *
     * @param name The name of the player
     * @param isWhite Whether the player is white or black
     */
    public Player(String name, boolean isWhite) {
        this.name = name;
        this.isWhite = isWhite;
        this.isSpectator = false;
    }

    /**
     * Constructor for the Player class
     *
     * @param name The name of the player
     * @param isWhite Whether the player is white or black
     * @param isSpectator Whether the player is a spectator
     */
    public Player(String name, boolean isWhite, boolean isSpectator) {
        this.name = name;
        this.isWhite = isWhite;
        this.isSpectator = isSpectator;
    }

    public boolean isSpectator() {
        return isSpectator;
    }

    public void setSpectator(boolean spectator) {
        isSpectator = spectator;
    }

    public String getName() {
        return name;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setWhite(boolean white) {
        isWhite = white;
    }
}
