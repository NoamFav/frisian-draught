package com.um_project_game.board;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class GameInfo {
    public IntegerProperty scorePlayerOne = new SimpleIntegerProperty(0);
    public IntegerProperty scorePlayerTwo = new SimpleIntegerProperty(0);
    public IntegerProperty playerTurn = new SimpleIntegerProperty(1);

    public int getScorePlayerOne() {
        return scorePlayerOne.get();
    }

    public IntegerProperty scorePlayerOneProperty() {
        return scorePlayerOne;
    }

    public int getScorePlayerTwo() {
        return scorePlayerTwo.get();
    }

    public IntegerProperty scorePlayerTwoProperty() {
        return scorePlayerTwo;
    }

    public int getPlayerTurn() {
        return playerTurn.get();
    }

    public IntegerProperty playerTurnProperty() {
        return playerTurn;
    }
}
