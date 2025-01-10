package com.um_project_game;

public class Player {
    private String name;

    private boolean isWhite;
    private boolean isSpectator;

    public Player(String name, boolean isWhite) {
        this.name = name;
        this.isWhite = isWhite;
        this.isSpectator = false;
    }

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
