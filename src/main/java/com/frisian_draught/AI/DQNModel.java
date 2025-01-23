package com.frisian_draught.AI;

import com.frisian_draught.board.GameState;
import com.frisian_draught.board.Move;

import org.joml.Vector2i;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The DQNModel class represents a Deep Q-Network model used for evaluating game states
 * and determining the best moves in a game.
 */
public class DQNModel {
    private NeuralNetwork network;

    /**
     * Constructs a DQNModel with the specified parameters.
     *
     * @param inputSize the size of the input layer
     * @param hiddenSize the size of the hidden layer
     * @param outputSize the size of the output layer
     * @param learningRate the learning rate for the neural network
     */
    public DQNModel(int inputSize, int hiddenSize, int outputSize, double learningRate) {
        this.network = new NeuralNetwork(inputSize, hiddenSize, outputSize, learningRate);
    }

    /**
     * Predicts Q-values for all possible moves in the given game state.
     *
     * @param state the current game state
     * @return a map of positions to Q-values
     */
    public Map<Vector2i, Double> predict(GameState state) {
        double[] input = state.toInputArray();
        double[] output = network.predict(input);
        Map<Vector2i, Double> qValues = new HashMap<>();

        int boardSize = 10;
        for (int i = 0; i < output.length; i++) {
            int x = i % boardSize;
            int y = i / boardSize;
            qValues.put(new Vector2i(x, y), output[i]);
        }

        // Filter Q-values based on valid moves from MainBoard
        List<Move> possibleMoves = state.generateMoves();
        Map<Vector2i, Double> filteredQValues = new HashMap<>();
        for (Move move : possibleMoves) {
            Vector2i endPosition = move.getEndPosition();
            if (qValues.containsKey(endPosition)) {
                filteredQValues.put(endPosition, qValues.get(endPosition));
            }
        }

        System.out.println("Filtered Q-values: " + filteredQValues);
        return filteredQValues;
    }

    /**
     * Updates the weights of the neural network using a single experience.
     *
     * @param state the current game state
     * @param action the action taken
     * @param targetQValue the target Q-value for the action
     */
    public void updateWeights(GameState state, Vector2i action, double targetQValue) {
        // 1. Get current network predictions for this state
        double[] input = state.toInputArray();
        double[] predictedQ = network.predict(input);

        // 2. Convert (x, y) action to the 1D index in predictedQ
        int actionIndex = action.y * 10 + action.x;

        // 3. Overwrite the chosen action’s Q-value with the Bellman target
        predictedQ[actionIndex] = targetQValue;

        // 4. Train the network to fit this updated Q vector
        network.train(input, predictedQ);
    }

    /**
     * Updates the weights of the neural network using multiple experiences.
     *
     * @param experiences an iterable of experiences
     * @param gamma the discount factor for future rewards
     */
    public void updateWeights(Iterable<Experience> experiences, double gamma) {
        for (Experience exp : experiences) {
            double[] input = exp.state.toInputArray(); // Convert state to input array
            double[] target = network.predict(input); // Get current predictions

            // Calculate target Q-value using Bellman equation
            double maxNextQ = 0;
            if (!exp.isTerminal) {
                double[] nextQValues = network.predict(exp.nextState.toInputArray());
                for (double q : nextQValues) {
                    maxNextQ = Math.max(maxNextQ, q);
                }
            }

            // Update Q-value for the chosen action
            int actionIndex = exp.action.y * 10 + exp.action.x;
            target[actionIndex] = exp.reward + gamma * maxNextQ;

            network.train(input, target); // Train the network
        }
    }

    /**
     * Copies the weights from another DQN model.
     *
     * @param model the DQN model to copy weights from
     */
    public void copyWeightsFrom(DQNModel model) {
        network.setWeights(model.network.getWeights());
    }

    /**
     * Evaluates the maximum Q-value for a given game state.
     *
     * @param state the current game state
     * @return the maximum Q-value
     */
    public double evaluate(GameState state) {
        return predict(state).values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    /**
     * Saves the model to a file.
     *
     * @param filename the name of the file to save the model to
     */
    public void saveModel(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this.network.getWeights());
            System.out.println("Model saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the model from a file.
     *
     * @param filename the name of the file to load the model from
     */
    public void loadModel(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            double[][][] weights = (double[][][]) in.readObject();
            this.network.setWeights(weights);
            System.out.println("Model loaded from " + filename);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}