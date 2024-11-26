package com.um_project_game.AI;

public class NeuralNetwork {
    private double[][] weightsInputHidden; // Weights between input and hidden layers
    private double[][] weightsHiddenOutput; // Weights between hidden and output layers
    private double learningRate;

    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize, double learningRate) {
        this.learningRate = learningRate;

        // Initialize weights with small random values
        weightsInputHidden = new double[inputSize][hiddenSize];
        weightsHiddenOutput = new double[hiddenSize][outputSize];
        initializeWeights(weightsInputHidden);
        initializeWeights(weightsHiddenOutput);
    }

    private void initializeWeights(double[][] weights) {
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                weights[i][j] = Math.random() * 0.01;
            }
        }
    }

    // Getter for weights
    public double[][][] getWeights() {
        return new double[][][] {weightsInputHidden, weightsHiddenOutput};
    }

    // Setter for weights
    public void setWeights(double[][][] weights) {
        weightsInputHidden = weights[0];
        weightsHiddenOutput = weights[1];
    }

    // Forward pass: Predict output based on input
    public double[] predict(double[] input) {
        double[] hiddenLayer = activate(multiply(input, weightsInputHidden));
        double[] outputLayer = activate(multiply(hiddenLayer, weightsHiddenOutput));
        return outputLayer;
    }

    // Backpropagation and weight update
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

    private double[] activate(double[] layer) {
        double[] activated = new double[layer.length];
        for (int i = 0; i < layer.length; i++) {
            activated[i] = sigmoid(layer[i]);
        }
        return activated;
    }

    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

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

    private double[] backpropagate(double[] error, double[][] weights) {
        double[] propagatedError = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                propagatedError[i] += error[j] * weights[i][j];
            }
        }
        return propagatedError;
    }

    private void updateWeights(double[][] weights, double[] inputs, double[] errors) {
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                weights[i][j] += learningRate * errors[j] * inputs[i];
            }
        }
    }
}
