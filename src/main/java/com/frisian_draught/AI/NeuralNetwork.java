package com.frisian_draught.AI;

/**
 * The NeuralNetwork class represents a simple feedforward neural network with one hidden layer.
 * It supports training using backpropagation and predicting outputs based on inputs.
 */
public class NeuralNetwork {
    private double[][] weightsInputHidden; // Weights between input and hidden layers
    private double[][] weightsHiddenOutput; // Weights between hidden and output layers
    private double learningRate;

    /**
     * Constructs a NeuralNetwork with the specified layer sizes and learning rate.
     *
     * @param inputSize the size of the input layer
     * @param hiddenSize the size of the hidden layer
     * @param outputSize the size of the output layer
     * @param learningRate the learning rate for weight updates
     */
    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize, double learningRate) {
        this.learningRate = learningRate;

        // Initialize weights with small random values
        weightsInputHidden = new double[inputSize][hiddenSize];
        weightsHiddenOutput = new double[hiddenSize][outputSize];
        initializeWeights(weightsInputHidden);
        initializeWeights(weightsHiddenOutput);
    }

    /**
     * Initializes the weights of the network with small random values.
     *
     * @param weights the weights to initialize
     */
    private void initializeWeights(double[][] weights) {
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                weights[i][j] = Math.random() * 0.01;
            }
        }
    }

    /**
     * Returns the weights of the network.
     *
     * @return a 3D array containing the weights
     */
    public double[][][] getWeights() {
        return new double[][][] {weightsInputHidden, weightsHiddenOutput};
    }

    /**
     * Sets the weights of the network.
     *
     * @param weights a 3D array containing the weights to set
     */
    public void setWeights(double[][][] weights) {
        weightsInputHidden = weights[0];
        weightsHiddenOutput = weights[1];
    }

    /**
     * Predicts the output based on the given input using a forward pass.
     *
     * @param input the input array
     * @return the predicted output array
     */
    public double[] predict(double[] input) {
        double[] hiddenLayer = activate(multiply(input, weightsInputHidden));
        double[] outputLayer = activate(multiply(hiddenLayer, weightsHiddenOutput));
        return outputLayer;
    }

    /**
     * Trains the network using the given input and target output.
     *
     * @param input the input array
     * @param target the target output array
     */
    public void train(double[] input, double[] target) {
        double[] hiddenLayer = activate(multiply(input, weightsInputHidden));
        double[] outputLayer = activate(multiply(hiddenLayer, weightsHiddenOutput));

        // Calculate output layer error
        double[] outputError = new double[outputLayer.length];
        for (int i = 0; i < outputError.length; i++) {
            outputError[i] = target[i] - outputLayer[i];
        }

        // Backpropagate to hidden layer
        double[] hiddenError = backpropagate(outputError, weightsHiddenOutput);

        // Update weights between hidden and output layers
        updateWeights(weightsHiddenOutput, hiddenLayer, outputError);

        // Update weights between input and hidden layers
        updateWeights(weightsInputHidden, input, hiddenError);
    }

    /**
     * Applies the sigmoid activation function to the given layer.
     *
     * @param layer the layer to activate
     * @return the activated layer
     */
    private double[] activate(double[] layer) {
        double[] activated = new double[layer.length];
        for (int i = 0; i < layer.length; i++) {
            activated[i] = sigmoid(layer[i]);
        }
        return activated;
    }

    /**
     * Applies the sigmoid function to the given value.
     *
     * @param x the value to apply the sigmoid function to
     * @return the result of the sigmoid function
     */
    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    /**
     * Multiplies the input array by the weights matrix.
     *
     * @param input the input array
     * @param weights the weights matrix
     * @return the result of the multiplication
     */
    private double[] multiply(double[] input, double[][] weights) {
        if (input.length != weights.length) {
            throw new IllegalArgumentException("Input length does not match weights dimensions!");
        }

        double[] output = new double[weights[0].length];
        for (int i = 0; i < weights[0].length; i++) {
            for (int j = 0; j < input.length; j++) {
                output[i] += input[j] * weights[j][i];
            }
        }
        return output;
    }

    /**
     * Backpropagates the error through the network.
     *
     * @param error the error array
     * @param weights the weights matrix
     * @return the propagated error array
     */
    private double[] backpropagate(double[] error, double[][] weights) {
        double[] propagatedError = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                propagatedError[i] += error[j] * weights[i][j];
            }
        }
        return propagatedError;
    }

    /**
     * Updates the weights of the network using the given inputs and errors.
     *
     * @param weights the weights matrix to update
     * @param inputs the input array
     * @param errors the error array
     */
    private void updateWeights(double[][] weights, double[] inputs, double[] errors) {
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                weights[i][j] += learningRate * errors[j] * inputs[i];
            }
        }
    }
}