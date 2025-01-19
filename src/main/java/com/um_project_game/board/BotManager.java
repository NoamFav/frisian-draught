package com.um_project_game.board;

import com.um_project_game.AI.Experience;
import com.um_project_game.AI.MiniMax.MiniMaxTree;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import org.joml.Vector2i;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class BotManager {
    private final BoardState boardState;
    private final MainBoard mainboard;
    private MoveManager moveManager;
    private BoardRendered boardRendered;

    public BotManager(BoardState boardState, MainBoard mainboard) {
        this.boardState = boardState;
        this.mainboard = mainboard;
    }

    public void setMoveManager(MoveManager moveManager) {
        this.moveManager = moveManager;
    }

    public void setBoardRendered(BoardRendered boardRendered) {
        this.boardRendered = boardRendered;
    }

    /**
     * Decides the best move using a combination of DQL and MiniMax.
     *
     * @param state The current game state.
     * @param depth The search depth for MiniMax.
     * @param epsilon The exploration rate for DQL.
     * @param maximizingPlayer True if the bot is the maximizing player.
     * @return The best move.
     */
    public Move getBestMove(GameState state, int depth, double epsilon, boolean maximizingPlayer) {
        // Use epsilon-greedy for exploration
        if (Math.random() < epsilon) {
            List<Move> possibleMoves = state.generateMoves();
            return possibleMoves.get(new Random().nextInt(possibleMoves.size()));
        }

        // Use DQL to predict the best move
        Map<Vector2i, Double> qValues = boardState.getBotModel().predict(state);
        Move bestDQLMove = chooseBestMoveFromQValues(state, qValues);

        // Evaluate confidence in DQL's decision
        double confidence = evaluateConfidence(qValues, bestDQLMove);
        if (confidence >= boardState.getCONFIDENCE_THRESHOLD()) {
            return bestDQLMove; // High confidence: Use DQL's decision
        }

        // Low confidence: Fall back to MiniMax
        MiniMaxTree miniMaxTree = new MiniMaxTree(state);
        return miniMaxTree.getBestMove(state, depth, maximizingPlayer);
    }

    private double evaluateConfidence(Map<Vector2i, Double> qValues, Move bestMove) {
        double bestQ = qValues.getOrDefault(bestMove.getEndPosition(), 0.0);
        double secondBestQ =
                qValues.values().stream()
                        .sorted(Comparator.reverseOrder())
                        .skip(1)
                        .findFirst()
                        .orElse(0.0);
        return bestQ
                - secondBestQ; // Confidence is the gap between the best and second-best Q-values
    }

    /**
     * Selects the best move based on Q-values predicted by the DQL model.
     *
     * @param state The current game state.
     * @param qValues The predicted Q-values.
     * @return The best move.
     */
    private Move chooseBestMoveFromQValues(GameState state, Map<Vector2i, Double> qValues) {
        return state.generateMoves().stream()
                .max(
                        Comparator.comparingDouble(
                                move ->
                                        qValues.getOrDefault(
                                                move.getEndPosition(), Double.NEGATIVE_INFINITY)))
                .orElse(null);
    }

    public void playBotVsBot(String savePath) {
        Platform.runLater(
                () -> {
                    if (!boardState.isActive()) {
                        System.out.println("Game is inactive. Stopping bot-vs-bot.");
                        return;
                    }

                    if (boardState.isBotvsBot()) {
                        if (boardState.isWhiteTurn()) {
                            boardState.getBotvsBotWhite().move();
                        } else {
                            boardState.getBotvsBotBlack().move();
                        }

                        // Train the model with a batch from the replay buffer
                        if (boardState.getReplayBuffer().size() >= boardState.getBATCH_SIZE()) {
                            List<Experience> batch =
                                    boardState.getReplayBuffer().sample(boardState.getBATCH_SIZE());
                            trainModel(batch);
                        }

                        // Save the model every 100 episodes
                        boardState.setEpisodeCounter(boardState.getEpisodeCounter() + 1);
                        if (boardState.getEpisodeCounter() % 100 == 0) {
                            String filename = generateUniqueFilename(savePath + "/dqn_model");
                            boardState.getBotModel().saveModel(filename);
                        }

                        PauseTransition pause = new PauseTransition(Duration.millis(1000));
                        pause.setOnFinished(_ -> playBotVsBot(savePath));
                        pause.play();
                    }
                });
    }

    private void trainModel(List<Experience> batch) {
        double totalLoss = 0.0;
        for (Experience experience : batch) {
            Map<Vector2i, Double> qValues = boardState.getBotModel().predict(experience.state);
            double target = experience.reward;
            if (!experience.isTerminal) {
                Map<Vector2i, Double> nextQValues =
                        boardState.getBotModel().predict(experience.nextState);
                target +=
                        boardState.getGAMMA()
                                * nextQValues.values().stream().max(Double::compareTo).orElse(0.0);
            }
            boardState
                    .getBotModel()
                    .updateWeights(
                            experience.state,
                            experience.action,
                            target 
                            );
        }
        System.out.println("Average Loss: " + (totalLoss / batch.size()));
    }

    private String generateUniqueFilename(String baseName) {
        return baseName + "_" + System.currentTimeMillis() + ".bin";
    }

    public void triggerBotMoveR() {}

    public void loadLatestModel(Path savePath) {
        try {
            Optional<Path> latestFile =
                    Files.list(savePath)
                            .filter(path -> path.toString().endsWith(".bin"))
                            .max(Comparator.comparingLong(path -> path.toFile().lastModified()));

            if (latestFile.isPresent()) {
                boardState.getBotModel().loadModel(latestFile.get().toString());
                System.out.println("Loaded model: " + latestFile.get());
            } else {
                System.out.println("No saved models found. Starting fresh.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
