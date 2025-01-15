package com.um_project_game.AI.MiniMax;

import com.um_project_game.board.GameState;
import com.um_project_game.board.Move;
import com.um_project_game.AI.DQNModel;

import java.util.ArrayList;
import java.util.List;

public class HybridAgent {

    private final GameState rootState;
    private final DQNModel dqnModel;

    public HybridAgent(GameState rootState, DQNModel dqnModel) {
        this.rootState = rootState;
        this.dqnModel = dqnModel;
    }

    public Move getBestMove(GameState state, int depth, boolean maximizingPlayer) {
        MMResult result = minimax(state.clone(), depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, maximizingPlayer, new ArrayList<>());
        return result.getMoves().isEmpty() ? null : result.getMoves().get(0);
    }

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
                if (beta <= alpha) break; // Prune the branch
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
                if (beta <= alpha) break; // Prune the branch
            }
            return new MMResult(minEval, bestMoves);
        }
    }
}