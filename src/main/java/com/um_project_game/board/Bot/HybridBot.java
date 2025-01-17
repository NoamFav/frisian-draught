package com.um_project_game.board.Bot;

import com.um_project_game.AI.DQNModel;
import com.um_project_game.AI.MiniMax.HybridAgent;
import com.um_project_game.AI.ReplayBuffer;
import com.um_project_game.board.BoardRendered;
import com.um_project_game.board.BoardState;
import com.um_project_game.board.CapturePath;
import com.um_project_game.board.GameState;
import com.um_project_game.board.MainBoard;
import com.um_project_game.board.Move;
import com.um_project_game.board.MoveManager;
import com.um_project_game.board.Pawn;

import javafx.application.Platform;

import java.util.List;
import java.util.stream.Collectors;

public class HybridBot extends Bot {

    public HybridBot(
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
                        // Access the DQNModel
                        DQNModel dqnModel = boardState.getBotModel();
                        if (dqnModel == null) {
                            System.out.println("DQNModel is not initialized.");
                            return;
                        }

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

                            // Use DQN to prioritize capture paths
                            CapturePath bestPath =
                                    capturePaths.stream()
                                            .max(
                                                    (path1, path2) ->
                                                            Double.compare(
                                                                    dqnModel.predict(
                                                                                    mainBoard
                                                                                            .getBoardState())
                                                                            .getOrDefault(
                                                                                    path1
                                                                                            .getLastPosition(),
                                                                                    0.0),
                                                                    dqnModel.predict(
                                                                                    mainBoard
                                                                                            .getBoardState())
                                                                            .getOrDefault(
                                                                                    path2
                                                                                            .getLastPosition(),
                                                                                    0.0)))
                                            .orElse(null);

                            if (bestPath != null) {
                                System.out.println("Selected capture path: " + bestPath);
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

                                    mainBoard.animatePawnCaptureMovement(
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
                        GameState currentState = mainBoard.getBoardState();
                        List<Move> possibleMoves = currentState.generateMoves();

                        if (possibleMoves.isEmpty()) {
                            System.out.println("No possible moves for the agent.");
                            return;
                        }

                        // Initialize Minimax with DQN model for evaluation
                        HybridAgent miniMaxTree = new HybridAgent(currentState, dqnModel);
                        Move[] selectedMove = {
                            miniMaxTree.getBestMove(currentState, 3, boardState.isWhiteTurn())
                        };

                        // Fallback to DQN-guided random move if Minimax fails
                        if (selectedMove[0] == null) {
                            System.out.println(
                                    "Minimax did not find a valid move, falling back to DQN.");
                            selectedMove[0] =
                                    possibleMoves.stream()
                                            .max(
                                                    (move1, move2) ->
                                                            Double.compare(
                                                                    dqnModel.predict(currentState)
                                                                            .getOrDefault(
                                                                                    move1
                                                                                            .getEndPosition(),
                                                                                    0.0),
                                                                    dqnModel.predict(currentState)
                                                                            .getOrDefault(
                                                                                    move2
                                                                                            .getEndPosition(),
                                                                                    0.0)))
                                            .orElse(null);
                        }

                        if (selectedMove != null) {
                            Pawn pawn =
                                    moveManager.getPawnAtPosition(
                                            selectedMove[0].getStartPosition());
                            System.out.println(
                                    "Selected move: Start = "
                                            + selectedMove[0].getStartPosition()
                                            + ", End = "
                                            + selectedMove[0].getEndPosition());

                            if (pawn != null) {
                                boardState.getTakenMoves().add(selectedMove[0]);
                                mainBoard.animatePawnMovement(
                                        pawn,
                                        selectedMove[0].getEndPosition(),
                                        () -> applyMove(currentState.applyMove(selectedMove[0])));
                            } else {
                                boardState.getTakenMoves().add(selectedMove[0]);
                                applyMove(currentState.applyMove(selectedMove[0]));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
