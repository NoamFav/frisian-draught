package com.um_project_game.board;

public class MoveResult {
    private GameState nextState;
    private double reward;
    private boolean isGameOver;

    public MoveResult(GameState nextState, double reward, boolean isGameOver) {
        this.nextState = nextState;
        this.reward = reward;
        this.isGameOver = isGameOver;
    }

    public GameState getNextState() {
        return nextState;
    }

    public double getReward() {
        return reward;
    }

    public boolean isGameOver() {
        return isGameOver;
    }
}
