package com.um_project_game.board;

import com.um_project_game.AI.Experience;
import com.um_project_game.AI.MiniMax.MiniMaxTree;
import com.um_project_game.AI.ReplayBuffer;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import org.joml.Vector2i;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class BotManager {
    private final BoardState boardState;
    private final MainBoard mainboard;
    private MoveManager moveManager;
    private BoardRendered boardRendered;

    private static final double CONFIDENCE_THRESHOLD = 0.2;
    private static final int BATCH_SIZE = 32;
    private static final int REPLAY_BUFFER_SIZE = 10000;
    private ReplayBuffer replayBuffer;
    private int episodeCounter = 0;

    public BotManager(BoardState boardState, MainBoard mainboard) {
        this.boardState = boardState;
        this.mainboard = mainboard;
        this.replayBuffer = new ReplayBuffer(REPLAY_BUFFER_SIZE);
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
     * @param state           The current game state.
     * @param depth           The search depth for MiniMax.
     * @param epsilon         The exploration rate for DQL.
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
        if (confidence >= CONFIDENCE_THRESHOLD) {
            return bestDQLMove; // High confidence: Use DQL's decision
        }

        // Low confidence: Fall back to MiniMax
        MiniMaxTree miniMaxTree = new MiniMaxTree(state);
        return miniMaxTree.getBestMove(state, depth, maximizingPlayer);
    }

    private double evaluateConfidence(Map<Vector2i, Double> qValues, Move bestMove) {
        double bestQ = qValues.getOrDefault(bestMove.getEndPosition(), 0.0);
        double secondBestQ = qValues.values().stream()
                .sorted(Comparator.reverseOrder())
                .skip(1)
                .findFirst()
                .orElse(0.0);
        return bestQ - secondBestQ; // Confidence is the gap between the best and second-best Q-values
    }

    /**
     * Selects the best move based on Q-values predicted by the DQL model.
     *
     * @param state   The current game state.
     * @param qValues The predicted Q-values.
     * @return The best move.
     */
    private Move chooseBestMoveFromQValues(GameState state, Map<Vector2i, Double> qValues) {
        return state.generateMoves().stream()
                .max(Comparator.comparingDouble(
                        move -> qValues.getOrDefault(move.getEndPosition(), Double.NEGATIVE_INFINITY)))
                .orElse(null);
    }

    public void triggerBotMove() {
        Platform.runLater(
                () -> {
                    try {
                        GameState currentState = mainboard.getBoardState();

                        ArrayList<Pawn> capturedPawnsList = new ArrayList<>();

                        // Compute capture paths for the bot
                        List<CapturePath> capturePaths = computeCapturePathsForBot();
                        Map<Vector2i, Double> qValues =
                                boardState.getBotModel().predict(currentState);

                        if (capturePaths != null && !capturePaths.isEmpty()) {
                            System.out.println("Bot has capture opportunities. Available paths:");
                            for (CapturePath path : capturePaths) {
                                System.out.println(
                                        " - Path: "
                                                + path.positions
                                                + ", Captures: "
                                                + path.capturedPawns);
                            }

                            // Select the capture path with the highest predicted value
                            CapturePath bestPath =
                                    capturePaths.stream()
                                            .max(
                                                    Comparator.comparingDouble(
                                                            path ->
                                                                    qValues.getOrDefault(
                                                                            path.getLastPosition(),
                                                                            0.0)))
                                            .orElse(null);

                            if (bestPath != null) {
                                System.out.println("Best capture path: " + bestPath);
                                Pawn pawn = bestPath.initialPawn;
                                if (pawn != null) {
                                    capturedPawnsList.addAll(bestPath.capturedPawns);

                                    ArrayList<Vector2i> capturedPawnPositions = new ArrayList<>();
                                    capturedPawnsList.forEach(
                                            capturedPawn ->
                                                    capturedPawnPositions.add(
                                                            capturedPawn.getPosition()));

                                    mainboard.animatePawnCaptureMovement(
                                            pawn,
                                            bestPath,
                                            () ->
                                                    moveManager.processAfterCaptureMove(
                                                            pawn, bestPath));
                                    boardState
                                            .getTakenMoves()
                                            .add(
                                                    new Move(
                                                            pawn.getPosition(),
                                                            bestPath.getLastPosition(),
                                                            capturedPawnPositions));
                                    return; // Ensure no fallback to normal moves
                                }
                            }
                        }

                        // If no captures, proceed with normal moves
                        List<Move> possibleMoves = currentState.generateMoves();
                        if (possibleMoves.isEmpty()) {
                            System.out.println("No possible moves for the bot.");
                            return;
                        }

                        Vector2i chosenAction =
                                qValues.entrySet().stream()
                                        .max(Map.Entry.comparingByValue())
                                        .map(Map.Entry::getKey)
                                        .orElse(null);

                        Move selectedMove =
                                possibleMoves.stream()
                                        .filter(move -> move.getEndPosition().equals(chosenAction))
                                        .findFirst()
                                        .orElse(null);

                        if (selectedMove != null) {
                            Pawn pawn =
                                    moveManager.getPawnAtPosition(selectedMove.getStartPosition());
                            if (pawn != null) {
                                boardState.getTakenMoves().add(selectedMove);
                                mainboard.animatePawnMovement(
                                        pawn,
                                        selectedMove.getEndPosition(),
                                        () -> applyMove(currentState.applyMove(selectedMove)));
                                return;
                            } else {
                                System.out.println("Fallback for takenMoves: " + selectedMove);
                                boardState.getTakenMoves().add(selectedMove);
                                applyMove(currentState.applyMove(selectedMove));
                                return;
                            }
                        }
                        // Add experience to replay buffer
                        Experience experience =
                                new Experience(
                                        currentState,
                                        chosenAction,
                                        computeReward(currentState.applyMove(selectedMove)),
                                        mainboard.getBoardState(),
                                        currentState.isTerminal());
                        replayBuffer.addExperience(experience);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void triggerBotMove2(int depth, double epsilon, boolean maximizingPlayer) {
        Platform.runLater(() -> {
            try {
                GameState currentState = mainboard.getBoardState();
                Move bestMove = getBestMove(currentState, depth, epsilon, maximizingPlayer);
                if (bestMove != null) {
                    Pawn pawn = moveManager.getPawnAtPosition(bestMove.getStartPosition());
                    if (pawn != null) {
                        boardState.getTakenMoves().add(bestMove);
                        mainboard.animatePawnMovement(
                                pawn, bestMove.getEndPosition(), () -> applyMove(currentState.applyMove(bestMove)));
                    } else {
                        boardState.getTakenMoves().add(bestMove);
                        applyMove(currentState.applyMove(bestMove));
                    }
                } else {
                    System.out.println("No valid moves found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

                            triggerBotMoveR(); // Random bot for White
                        } else {
                            triggerBotMoveMM(); // Trained bot for Black
                        }

                        // Train the model with a batch from the replay buffer
                        if (replayBuffer.size() >= BATCH_SIZE) {
                            List<Experience> batch = replayBuffer.sample(BATCH_SIZE);
                            trainModel(batch);
                        }

                        // Save the model every 100 episodes
                        episodeCounter++;
                        if (episodeCounter % 100 == 0) {
                            String filename = generateUniqueFilename(savePath + "/dqn_model");
                            boardState.getBotModel().saveModel(filename);
                        }

                        PauseTransition pause = new PauseTransition(Duration.millis(1000));
                        pause.setOnFinished(_ -> playBotVsBot(savePath));
                        pause.play();
                    }
                });
    }

    private List<CapturePath> computeCapturePathsForBot() {
        List<Pawn> whitepawns =
                this.boardState.getPawns().stream()
                        .filter(Pawn::isWhite)
                        .collect(Collectors.toList()); // Bot's pawns

        List<Pawn> botPawnsblack =
                boardState.getPawns().stream()
                        .filter(pawn -> !pawn.isWhite())
                        .collect(Collectors.toList()); // Bot's pawns
        List<CapturePath> allCapturePaths = new ArrayList<>();

        for (Pawn pawn :
                boardState.isWhiteTurn() && boardState.isBotvsBot() ? whitepawns : botPawnsblack) {

            moveManager.seePossibleMove(pawn, true);

            if (boardState.getCurrentCapturePaths() != null
                    && !boardState.getCurrentCapturePaths().isEmpty()) {
                allCapturePaths.addAll(boardState.getCurrentCapturePaths()); // Add computed paths
            }
        }

        return allCapturePaths;
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
                        BoardState.getGamma()
                                * nextQValues.values().stream().max(Double::compareTo).orElse(0.0);
            }
            boardState
                    .getBotModel()
                    .updateWeights(
                            experience.state,
                            experience.action,
                            target - qValues.get(experience.action));
            totalLoss += Math.abs(target - qValues.get(experience.action));
        }
        System.out.println("Average Loss: " + (totalLoss / batch.size()));
    }

    private double computeReward(MoveResult result) {
        return result.getReward();
    }

    private void applyMove(MoveResult result) {
        if (result != null) {
            // Update the board state

            boardState.getPastStates().add(result.getNextState());
            boardRendered.renderPawns();

            if (result.isGameOver()) {
                moveManager.checkGameOver();
            }

            // Switch turn
            moveManager.switchTurn();
        }
    }

    private String generateUniqueFilename(String baseName) {
        return baseName + "_" + System.currentTimeMillis() + ".bin";
    }

    public void triggerBotMoveMM() {
        Platform.runLater(
                () -> {
                    try {
                        // Compute capture paths for the bot
                        List<CapturePath> capturePaths = computeCapturePathsForBot();
                        if (capturePaths != null && !capturePaths.isEmpty()) {
                            System.out.println("Bot has capture opportunities. Available paths:");
                            for (CapturePath path : capturePaths) {
                                System.out.println(
                                        " - Path: "
                                                + path.positions
                                                + ", Captures: "
                                                + path.capturedPawns);
                            }

                            // Find the maximum capture value
                            double maxCaptureValue =
                                    capturePaths.stream()
                                            .mapToDouble(CapturePath::getCaptureValue)
                                            .max()
                                            .orElse(Double.NEGATIVE_INFINITY);

                            // Filter paths with the maximum value
                            List<CapturePath> bestPaths =
                                    capturePaths.stream()
                                            .filter(
                                                    path ->
                                                            path.getCaptureValue()
                                                                    == maxCaptureValue)
                                            .collect(Collectors.toList());

                            // Randomly select one of the best paths
                            CapturePath bestPath =
                                    bestPaths.get(new Random().nextInt(bestPaths.size()));

                            System.out.println("Selected capture path: " + bestPath);

                            if (bestPath != null) {
                                Pawn pawn = bestPath.initialPawn;
                                if (pawn != null) {
                                    System.out.println("Bot executing capture path: " + bestPath);

                                    boardState
                                            .getTakenMoves()
                                            .add(
                                                    new Move(
                                                            pawn.getPosition(),
                                                            bestPath.getLastPosition(),
                                                            bestPath.capturedPawns.stream()
                                                                    .map(Pawn::getPosition)
                                                                    .collect(Collectors.toList())));
                                    mainboard.animatePawnCaptureMovement(
                                            pawn,
                                            bestPath,
                                            () ->
                                                    moveManager.processAfterCaptureMove(
                                                            pawn, bestPath));
                                    return; // Ensure no fallback to normal moves
                                }
                            }
                        }

                        // If no captures, proceed with normal moves
                        GameState currentState = mainboard.getBoardState();
                        List<Move> possibleMoves = currentState.generateMoves();

                        if (possibleMoves.isEmpty()) {
                            System.out.println("No possible moves for the agent.");
                            return;
                        }

                        MiniMaxTree miniMaxTree = new MiniMaxTree(currentState);
                        Move selMove = null;
                        GameState newState = null;
                        if (boardState.isWhiteTurn()) {
                            selMove = miniMaxTree.getBestMove(currentState, 4, true);
                            if (selMove == null) {
                                Random random = new Random();
                                selMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
                            }
                            newState = miniMaxTree.rootState;
                        } else {
                            selMove = miniMaxTree.getBestMove(currentState, 4, false);
                            if (selMove == null) {
                                Random random = new Random();
                                selMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
                            }
                            newState = miniMaxTree.rootState;
                        }
                        GameState resetState = newState;
                        Move selectedMove = selMove;
                        System.out.println("Move start: " + selectedMove.getStartPosition());
                        System.out.println("Move end: " + selectedMove.getEndPosition());

                        if (selectedMove != null) {
                            Pawn pawn =
                                    moveManager.getPawnAtPosition(selectedMove.getStartPosition());
                            if (pawn != null) {
                                boardState.getTakenMoves().add(selectedMove);
                                mainboard.animatePawnMovement(
                                        pawn,
                                        selectedMove.getEndPosition(),
                                        () -> applyMove(resetState.applyMove(selectedMove)));
                            } else {
                                boardState.getTakenMoves().add(selectedMove);
                                applyMove(resetState.applyMove(selectedMove));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void triggerBotMoveR() {
        Platform.runLater(
                () -> {
                    try {
                        // Compute capture paths for the bot
                        List<CapturePath> capturePaths = computeCapturePathsForBot();
                        if (capturePaths != null && !capturePaths.isEmpty()) {
                            System.out.println("Bot has capture opportunities. Available paths:");
                            for (CapturePath path : capturePaths) {
                                System.out.println(
                                        " - Path: "
                                                + path.positions
                                                + ", Captures: "
                                                + path.capturedPawns);
                            }

                            // Find the maximum capture value
                            double maxCaptureValue =
                                    capturePaths.stream()
                                            .mapToDouble(CapturePath::getCaptureValue)
                                            .max()
                                            .orElse(Double.NEGATIVE_INFINITY);

                            // Filter paths with the maximum value
                            List<CapturePath> bestPaths =
                                    capturePaths.stream()
                                            .filter(
                                                    path ->
                                                            path.getCaptureValue()
                                                                    == maxCaptureValue)
                                            .collect(Collectors.toList());

                            // Randomly select one of the best paths
                            CapturePath bestPath =
                                    bestPaths.get(new Random().nextInt(bestPaths.size()));

                            System.out.println("Selected capture path: " + bestPath);

                            if (bestPath != null) {
                                Pawn pawn = bestPath.initialPawn;
                                if (pawn != null) {
                                    System.out.println("Bot executing capture path: " + bestPath);

                                    boardState
                                            .getTakenMoves()
                                            .add(
                                                    new Move(
                                                            pawn.getPosition(),
                                                            bestPath.getLastPosition(),
                                                            bestPath.capturedPawns.stream()
                                                                    .map(Pawn::getPosition)
                                                                    .collect(Collectors.toList())));
                                    mainboard.animatePawnCaptureMovement(
                                            pawn,
                                            bestPath,
                                            () ->
                                                    moveManager.processAfterCaptureMove(
                                                            pawn, bestPath));
                                    return; // Ensure no fallback to normal moves
                                }
                            }
                        }

                        // If no captures, proceed with normal moves
                        GameState currentState = mainboard.getBoardState();
                        List<Move> possibleMoves = currentState.generateMoves();

                        if (possibleMoves.isEmpty()) {
                            System.out.println("No possible moves for the agent.");
                            return;
                        }

                        // Randomly select a normal move
                        Random random = new Random();
                        Move selectedMove = possibleMoves.get(random.nextInt(possibleMoves.size()));

                        if (selectedMove != null) {
                            Pawn pawn =
                                    moveManager.getPawnAtPosition(selectedMove.getStartPosition());
                            if (pawn != null) {
                                boardState.getTakenMoves().add(selectedMove);
                                mainboard.animatePawnMovement(
                                        pawn,
                                        selectedMove.getEndPosition(),
                                        () -> applyMove(currentState.applyMove(selectedMove)));
                            } else {
                                boardState.getTakenMoves().add(selectedMove);
                                applyMove(currentState.applyMove(selectedMove));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

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
