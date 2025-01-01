package com.um_project_game.board;

import com.um_project_game.AI.Experience;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

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

    private List<CapturePath> computeCapturePathsForBot() {

        List<Pawn> whitepawns =
                this.boardState.getPawns().stream()
                        .filter(pawn -> pawn.isWhite())
                        .collect(Collectors.toList()); // Bot's pawns

        List<Pawn> botPawnsblack =
                boardState.getPawns().stream()
                        .filter(pawn -> !pawn.isWhite())
                        .collect(Collectors.toList()); // Bot's pawns
        List<CapturePath> allCapturePaths = new ArrayList<>();

        for (Pawn pawn :
                boardState.isWhiteTurn() && boardState.isBotvsBot() ? whitepawns : botPawnsblack) {
            moveManager.seePossibleMove(pawn);
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

    public void playBotVsBot() {
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
                            triggerBotMove(); // Other bot for Black
                        }
                        // Delay to make the moves visually distinguishable and avoid crash due to
                        // the game buffer
                        PauseTransition pause = new PauseTransition(Duration.millis(1000));
                        pause.setOnFinished(_ -> playBotVsBot());
                        pause.play();
                    }
                });
    }
}
