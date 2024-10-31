package com.um_project_game.AI;

import com.um_project_game.board.GameState;
import org.joml.Vector2i;

public class Experience {
    public GameState state;
    public Vector2i action;
    public double reward;
    public GameState nextState;
    public boolean isTerminal;

    public Experience(GameState state, Vector2i action, double reward, GameState nextState, boolean isTerminal) {
        this.state = state;
        this.action = action;
        this.reward = reward;
        this.nextState = nextState;
        this.isTerminal = isTerminal;
    }
}
