package com.frisian_draught.board.Bot;

import com.frisian_draught.AI.MiniMax.MiniMaxTree;
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

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MinimaxBot extends Bot {

    public MinimaxBot(
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
                                mainBoard.animatePawnMovement(
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
}
