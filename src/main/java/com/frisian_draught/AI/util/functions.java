package com.frisian_draught.AI.util;

import com.frisian_draught.AI.DQNModel;
import com.frisian_draught.AI.Experience;
import com.frisian_draught.AI.ReplayBuffer;
import com.frisian_draught.board.GameState;
import com.frisian_draught.board.Move;

import org.joml.Vector2i;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class functions {

    public void qLearningUpdate(
            GameState state,
            Move action,
            double reward,
            GameState nextState,
            DQNModel model,
            DQNModel targetModel,
            double alpha,
            double gamma) {
        Map<Vector2i, Double> currentQValues = model.predict(state);
        double currentQ = currentQValues.getOrDefault(action.getStartPosition(), 0.0);

        Map<Vector2i, Double> nextQValues = targetModel.predict(nextState);
        double maxNextQ =
                nextQValues.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        double target = reward + gamma * maxNextQ;
        double loss = Math.pow(target - currentQ, 2);

        model.updateWeights(state, action.getStartPosition(), loss);
    }

    public double computeLoss(
            List<Experience> miniBatch, DQNModel model, DQNModel targetModel, double gamma) {
        double totalLoss = 0.0;

        for (Experience exp : miniBatch) {
            double target =
                    exp.isTerminal
                            ? exp.reward
                            : exp.reward
                                    + gamma
                                            * targetModel.predict(exp.nextState).values().stream()
                                                    .mapToDouble(Double::doubleValue)
                                                    .max()
                                                    .orElse(0.0);
            double prediction = model.predict(exp.state).getOrDefault(exp.action, 0.0);
            totalLoss += Math.pow(target - prediction, 2);
        }
        return totalLoss / miniBatch.size();
    }

    public double minimax(
            GameState state,
            int depth,
            double alpha,
            double beta,
            boolean maximizingPlayer,
            DQNModel dqnModel) {
        if (depth == 0 || state.isTerminal()) {
            return dqnModel.evaluate(state);
        }

        if (maximizingPlayer) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (Move move : state.generateMoves()) {
                GameState newState = state.applyMove(move).getNextState();
                double eval = minimax(newState, depth - 1, alpha, beta, false, dqnModel);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Prune the branch
                }
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (Move move : state.generateMoves()) {
                GameState newState = state.applyMove(move).getNextState();
                double eval = minimax(newState, depth - 1, alpha, beta, true, dqnModel);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Prune the branch
                }
            }
            return minEval;
        }
    }

    public Map<Vector2i, Double> predictQValues(GameState state, DQNModel model) {
        return model.predict(state);
    }

    public Move chooseActionEpsilonGreedy(GameState state, DQNModel model, double epsilon) {
        if (Math.random() < epsilon) {
            // Random move for exploration
            List<Move> moves = state.generateMoves();
            return moves.get(new Random().nextInt(moves.size()));
        } else {
            // Greedy action for exploitation
            return state.generateMoves().stream()
                    .max(
                            (move1, move2) ->
                                    Double.compare(
                                            model.predict(state)
                                                    .getOrDefault(move1.getStartPosition(), 0.0),
                                            model.predict(state)
                                                    .getOrDefault(move2.getStartPosition(), 0.0)))
                    .orElse(null);
        }
    }

    public void storeExperience(
            ReplayBuffer replayBuffer,
            GameState state,
            Move action,
            double reward,
            GameState nextState,
            boolean done) {
        replayBuffer.addExperience(
                new Experience(state, action.getStartPosition(), reward, nextState, done));
    }

    public Move adversarialSearch(GameState state, int depth, DQNModel model) {
        Move bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Move move : state.generateMoves()) {
            GameState newState = state.applyMove(move).getNextState();
            double score =
                    minimax(
                            newState,
                            depth - 1,
                            Double.NEGATIVE_INFINITY,
                            Double.POSITIVE_INFINITY,
                            false,
                            model);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    public double evaluateStateWithDQN(GameState state, DQNModel dqnModel) {
        return dqnModel.predict(state).values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);
    }
}
