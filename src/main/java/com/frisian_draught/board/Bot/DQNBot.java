package com.frisian_draught.board.Bot;

import com.frisian_draught.AI.Experience;
import com.frisian_draught.AI.ReplayBuffer;
import com.frisian_draught.board.BoardRendered;
import com.frisian_draught.board.BoardState;
import com.frisian_draught.board.CapturePath;
import com.frisian_draught.board.GameState;
import com.frisian_draught.board.MainBoard;
import com.frisian_draught.board.Move;
import com.frisian_draught.board.MoveManager;
import com.frisian_draught.board.Pawn;

import javafx.application.Platform;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DQNBot extends Bot {

    public DQNBot(
            BoardState boardState,
            MoveManager moveManager,
            MainBoard mainBoard,
            ReplayBuffer replayBuffer,
            BoardRendered boardRendered) {
        super(boardState, moveManager, mainBoard, replayBuffer, boardRendered);
    }

    @Override
    public void move() {
        Platform.runLater(
                () -> {
                    try {
                        GameState currentState = mainBoard.getBoardState();

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

                                    mainBoard.animatePawnCaptureMovement(
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
                                mainBoard.animatePawnMovement(
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
                                        mainBoard.getBoardState(),
                                        currentState.isTerminal());
                        replayBuffer.addExperience(experience);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
