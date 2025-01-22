package com.um_project_game.AI.MiniMax;

import com.um_project_game.board.GameState;
import com.um_project_game.board.Move;
import com.um_project_game.AI.DQNModel;

import java.util.ArrayList;
import java.util.List;

/**
 * The HybridAgent class combines the MiniMax algorithm with a Deep Q-Network (DQN) model
 * to determine the best move in a game state.
 */
public class HybridAgent {

    private final GameState rootState;
    private final DQNModel dqnModel;

    /**
     * Constructs a HybridAgent with the given root state and DQN model.
     *
     * @param rootState the initial game state
     * @param dqnModel the DQN model for evaluating game states
     */
    public HybridAgent(GameState rootState, DQNModel dqnModel) {
        this.rootState = rootState;
        this.dqnModel = dqnModel;
    }

    /**
     * Gets the best move for the given game state using the MiniMax algorithm.
     *
     * @param state the current game state
     * @param depth the depth to search in the game tree
     * @param maximizingPlayer true if the current player is maximizing, false otherwise
     * @return the best move found
     */
    public Move getBestMove(GameState state, int depth, boolean maximizingPlayer) {
        MMResult result = minimax(state.clone(), depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, maximizingPlayer, new ArrayList<>());
        return result.getMoves().isEmpty() ? null : result.getMoves().get(0);
    }

    /**
     * Implements the MiniMax algorithm with alpha-beta pruning.
     *
     * @param state the current game state
     * @param depth the depth to search in the game tree
     * @param alpha the alpha value for alpha-beta pruning
     * @param beta the beta value for alpha-beta pruning
     * @param maximizingPlayer true if the current player is maximizing, false otherwise
     * @param moveSequence the sequence of moves leading to the current state
     * @return the result of the MiniMax evaluation
     */
    private MMResult minimax(GameState state, int depth, double alpha, double beta, boolean maximizingPlayer, List<Move> moveSequence) {
        if (depth == 0 || state.isTerminal()) {
            double score = dqnModel.evaluate(state);
            return new MMResult(score, moveSequence);
        }

        if (maximizingPlayer) {
            double maxEval = Double.NEGATIVE_INFINITY;
            List<Move> bestMoves = new ArrayList<>();

            List<Move> possibleMoves = state.generateMoves();
            possibleMoves.sort((m1, m2) -> Double.compare(dqnModel.predict(state).getOrDefault(m2.getEndPosition(), 0.0),
                    dqnModel.predict(state).getOrDefault(m1.getEndPosition(), 0.0)));

            for (Move move : possibleMoves) {
                GameState newState = state.simulateMove(move).getNextState();
                List<Move> newMoveSequence = new ArrayList<>(moveSequence);
                newMoveSequence.add(move);

                double eval = minimax(newState.clone(), depth - 1, alpha, beta, false, newMoveSequence).getScore();
                if (eval > maxEval) {
                    maxEval = eval;
                    bestMoves = newMoveSequence;
                }
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return new MMResult(maxEval, bestMoves);

        } else {
            double minEval = Double.POSITIVE_INFINITY;
            List<Move> bestMoves = new ArrayList<>();

            List<Move> possibleMoves = state.generateMoves();
            possibleMoves.sort((m1, m2) -> Double.compare(dqnModel.predict(state).getOrDefault(m1.getEndPosition(), 0.0),
                    dqnModel.predict(state).getOrDefault(m2.getEndPosition(), 0.0)));

            for (Move move : possibleMoves) {
                GameState newState = state.simulateMove(move).getNextState();
                List<Move> newMoveSequence = new ArrayList<>(moveSequence);
                newMoveSequence.add(move);

                double eval = minimax(newState.clone(), depth - 1, alpha, beta, true, newMoveSequence).getScore();
                if (eval < minEval) {
                    minEval = eval;
                    bestMoves = newMoveSequence;
                }
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return new MMResult(minEval, bestMoves);
        }
    }
}