package com.um_project_game.AI;

import com.um_project_game.board.GameState;

import org.joml.Vector2i;

/**
 * The Experience class represents a single experience in the reinforcement learning process.
 * It contains the current state, the action taken, the reward received, the next state, and whether the next state is terminal.
 */
public class Experience {
    public GameState state;
    public Vector2i action;
    public double reward;
    public GameState nextState;
    public boolean isTerminal;

    /**
     * Constructs an Experience with the given parameters.
     *
     * @param state the current game state
     * @param action the action taken
     * @param reward the reward received
     * @param nextState the next game state
     * @param isTerminal true if the next state is terminal, false otherwise
     */
    public Experience(
            GameState state,
            Vector2i action,
            double reward,
            GameState nextState,
            boolean isTerminal) {
        this.state = state;
        this.action = action;
        this.reward = reward;
        this.nextState = nextState;
        this.isTerminal = isTerminal;
    }
}