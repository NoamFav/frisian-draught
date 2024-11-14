package com.um_project_game.AI.util;

import com.um_project_game.AI.DQNModel;
import com.um_project_game.AI.Experience;
import com.um_project_game.AI.ReplayBuffer;
import com.um_project_game.board.GameState;
import com.um_project_game.board.Move;
import com.um_project_game.board.MoveResult;

import org.joml.Vector2i;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class functions {

    private static final int TARGET_UPDATE_FREQUENCY = 100;
    private static final int BATCH_SIZE = 32;
    private static final int NUM_EPOCHS = 1;

    public void qLearningUpdate(
            GameState state,
            Vector2i action,
            double reward,
            GameState nextState,
            DQNModel model,
            DQNModel targetModel,
            double alpha,
            double gamma) {
        Map<Vector2i, Double> currentQValues = model.predict(state);
        double currentQ = currentQValues.getOrDefault(action, 0.0);

        Map<Vector2i, Double> nextQValues = targetModel.predict(nextState);
        double maxNextQ =
                nextQValues.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        double target = reward + gamma * maxNextQ;
        double loss = Math.pow(target - currentQ, 2);

        model.updateWeights(state, action, loss);
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
                GameState newState =
                        state.applyMove(move.getStartPosition(), move.getEndPosition())
                                .getNextState();
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
                GameState newState =
                        state.applyMove(move.getStartPosition(), move.getEndPosition())
                                .getNextState();
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

    public Vector2i chooseActionEpsilonGreedy(GameState state, DQNModel model, double epsilon) {
        if (Math.random() < epsilon) {
            // Random move for exploration
            List<Move> moves = state.generateMoves();
            return moves.get(new Random().nextInt(moves.size()))
                    .getStartPosition(); // Choose a random start position as the action
        } else {
            // Greedy action for exploitation
            return model.predict(state).entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null); // Return the action with the highest Q-value
        }
    }

    public void trainDRLModel(
            DQNModel model,
            DQNModel targetModel,
            ReplayBuffer replayBuffer,
            int batchSize,
            double gamma,
            double alpha,
            int numEpochs) {
        for (int epoch = 0; epoch < numEpochs; epoch++) {
            List<Experience> miniBatch = replayBuffer.sample(batchSize);
            double totalLoss = computeLoss(miniBatch, model, targetModel, gamma);
            model.updateWeights(miniBatch, totalLoss);

            if (epoch % TARGET_UPDATE_FREQUENCY == 0) {
                targetModel.copyWeightsFrom(model);
            }
        }
    }

    public void storeExperience(
            ReplayBuffer replayBuffer,
            GameState state,
            Vector2i action,
            double reward,
            GameState nextState,
            boolean done) {
        replayBuffer.addExperience(new Experience(state, action, reward, nextState, done));
    }

    public void mainDRLLoop(
            GameState initialState,
            DQNModel model,
            DQNModel targetModel,
            ReplayBuffer replayBuffer,
            double epsilon,
            double gamma,
            double alpha) {
        GameState state = initialState;
        while (!state.isTerminal()) {
            Vector2i action = chooseActionEpsilonGreedy(state, model, epsilon);
            MoveResult result =
                    state.applyMove(
                            action,
                            new Vector2i(
                                    action.x + 1, action.y + 1)); // Adjust move logic as needed
            GameState nextState = result.getNextState();
            double reward = result.getReward();
            boolean done = result.isGameOver();

            storeExperience(replayBuffer, state, action, reward, nextState, done);
            trainDRLModel(model, targetModel, replayBuffer, BATCH_SIZE, gamma, alpha, NUM_EPOCHS);

            state = nextState;
            if (done) break;
        }
    }

    public Vector2i adversarialSearch(GameState state, int depth, DQNModel model) {
        Vector2i bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Move move : state.generateMoves()) {
            GameState newState =
                    state.applyMove(move.getStartPosition(), move.getEndPosition()).getNextState();
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
                bestMove = move.getStartPosition();
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
