package com.um_project_game.Simulation;

import com.um_project_game.board.*;
import com.um_project_game.AI.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class GameSimulator {

    private final DQNModel model;
    private final DQNModel targetModel;
    private final ReplayBuffer replayBuffer;
    private final int maxGames;
    private final int maxMoves;
    private final double epsilon;
    private final double gamma;
    private final double alpha;
    private final int batchSize;
    private final Path savePath;

    public GameSimulator(DQNModel model, DQNModel targetModel, ReplayBuffer replayBuffer,
                         int maxGames, int maxMoves, double epsilon, double gamma, double alpha, int batchSize, Path savePath) {
        this.model = model;
        this.targetModel = targetModel;
        this.replayBuffer = replayBuffer;
        this.maxGames = maxGames;
        this.maxMoves = maxMoves;
        this.epsilon = epsilon;
        this.gamma = gamma;
        this.alpha = alpha;
        this.batchSize = batchSize;
        this.savePath = savePath;
    }

    /**
     * Runs the simulation for a specified number of games.
     */
    public void runSimulation() {
        for (int game = 0; game < maxGames; game++) {
            System.out.println("Starting game " + (game + 1));
            simulateGame();

            // Save the model every 100 games
            if ((game + 1) % 100 == 0) {
                saveModel("dqn_model_game_" + (game + 1) + ".bin");
            }
        }
    }

    /**
     * Simulates a single game.
     */
    private void simulateGame() {
        MainBoard mainBoard = new MainBoard();
        BoardState boardState = mainBoard.boardState;
        GameState initialState = mainBoard.getBoardState();
        boolean isGameOver = false;
        int moveCount = 0;

        while (!isGameOver && moveCount < maxMoves) {
            GameState currentState = mainBoard.getBoardState();

            // Choose action
            Move action = chooseActionEpsilonGreedy(currentState, model, epsilon);

            if (action == null) {
                System.out.println("No valid moves. Ending game.");
                break;
            }

            // Apply move
            MoveResult result = currentState.applyMove(action);
            GameState nextState = result.getNextState();
            double reward = result.getReward();
            isGameOver = result.isGameOver();

            // Store experience in replay buffer
            replayBuffer.addExperience(new Experience(currentState, action.getStartPosition(), reward, nextState, isGameOver));

            // Train model
            if (replayBuffer.size() >= batchSize) {
                trainModel();
            }

            moveCount++;
        }

        System.out.println("Game finished after " + moveCount + " moves.");
    }

    /**
     * Trains the model using a mini-batch from the replay buffer.
     */
    private void trainModel() {
        var miniBatch = replayBuffer.sample(batchSize);
        double totalLoss = 0.0;

        for (Experience experience : miniBatch) {
            double target = experience.isTerminal ? experience.reward :
                    experience.reward + gamma * targetModel.predict(experience.nextState).values().stream().max(Double::compareTo).orElse(0.0);

            model.updateWeights(experience.state, experience.action, target);
            totalLoss += Math.pow(target - model.predict(experience.state).getOrDefault(experience.action, 0.0), 2);
        }

        System.out.println("Average Loss: " + (totalLoss / miniBatch.size()));
    }

    /**
     * Chooses an action using the epsilon-greedy strategy.
     */
    private Move chooseActionEpsilonGreedy(GameState state, DQNModel model, double epsilon) {
        if (Math.random() < epsilon) {
            // Exploration: Choose a random move
            var moves = state.generateMoves();
            return moves.isEmpty() ? null : moves.get((int) (Math.random() * moves.size()));
        } else {
            // Exploitation: Choose the best move
            return state.generateMoves().stream()
                    .max((move1, move2) -> Double.compare(
                            model.predict(state).getOrDefault(move1.getStartPosition(), 0.0),
                            model.predict(state).getOrDefault(move2.getStartPosition(), 0.0)))
                    .orElse(null);
        }
    }

    /**
     * Saves the current model to a file.
     */
    private void saveModel(String filename) {
        try {
            Path filePath = savePath.resolve(filename);
            model.saveModel(filePath.toString());
            System.out.println("Model saved to " + filePath);
        } catch (Exception e) {
            System.err.println("Error saving model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main method to run the simulation.
     */
    public static void main(String[] args) {
        int inputSize = 101;
        int hiddenSize = 100;
        int outputSize = 100;
        double learningRate = 0.1;

        DQNModel model = new DQNModel(inputSize, hiddenSize, outputSize, learningRate);
        DQNModel targetModel = new DQNModel(inputSize, hiddenSize, outputSize, learningRate);
        targetModel.copyWeightsFrom(model);

        ReplayBuffer replayBuffer = new ReplayBuffer(10000);

        // Simulation parameters
        int maxGames = 1000;
        int maxMoves = 500;
        double epsilon = 0.1;
        double gamma = 0.99;
        double alpha = 0.01;
        int batchSize = 32;

        Path savePath = Path.of(System.getProperty("user.home"), "FrisianDraughtAIBin");
        try {
            if (!Files.exists(savePath)) {
                Files.createDirectories(savePath);
            }
        } catch (IOException e) {
            System.err.println("Error creating save directory: " + e.getMessage());
        }

        GameSimulator simulator = new GameSimulator(model, targetModel, replayBuffer, maxGames, maxMoves, epsilon, gamma, alpha, batchSize, savePath);
        simulator.runSimulation();
    }
}