package com.um_project_game.board.Bot;

import com.um_project_game.AI.ReplayBuffer;
import com.um_project_game.board.BoardRendered;
import com.um_project_game.board.BoardState;
import com.um_project_game.board.CapturePath;
import com.um_project_game.board.MainBoard;
import com.um_project_game.board.MoveManager;
import com.um_project_game.board.MoveResult;
import com.um_project_game.board.Pawn;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Bot {

    protected BoardState boardState;
    protected MoveManager moveManager;
    protected MainBoard mainBoard;
    protected ReplayBuffer replayBuffer;
    protected BoardRendered boardRendered;

    public Bot(
            BoardState boardState,
            MoveManager moveManager,
            MainBoard mainBoard,
            ReplayBuffer replayBuffer,
            BoardRendered boardRendered) {
        this.boardState = boardState;
        this.moveManager = moveManager;
        this.mainBoard = mainBoard;
        this.replayBuffer = replayBuffer;
        this.boardRendered = boardRendered;
    }

    public abstract void move();

    public List<CapturePath> computeCapturePathsForBot() {
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

    public void applyMove(MoveResult result) {
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

    public double computeReward(MoveResult result) {
        return result.getReward();
    }
}
