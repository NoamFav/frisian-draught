package com.frisian_draught.board.Bot;

import com.frisian_draught.AI.ReplayBuffer;
import com.frisian_draught.board.BoardRendered;
import com.frisian_draught.board.BoardState;
import com.frisian_draught.board.CapturePath;
import com.frisian_draught.board.MainBoard;
import com.frisian_draught.board.MoveManager;
import com.frisian_draught.board.MoveResult;
import com.frisian_draught.board.Pawn;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Bot class represents an abstract bot that can play the game.
 * It contains methods for making moves, computing capture paths, applying moves, and computing rewards.
 */
public abstract class Bot {

    protected BoardState boardState;
    protected MoveManager moveManager;
    protected MainBoard mainBoard;
    protected ReplayBuffer replayBuffer;
    protected BoardRendered boardRendered;

    /**
     * Constructs a Bot with the specified parameters.
     *
     * @param boardState the current state of the board
     * @param moveManager the move manager
     * @param mainBoard the main board
     * @param replayBuffer the replay buffer
     * @param boardRendered the board renderer
     */
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

    /**
     * Makes a move. This method should be implemented by subclasses.
     */
    public abstract void move();

    /**
     * Computes the capture paths for the bot.
     *
     * @return a list of capture paths
     */
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

    /**
     * Applies the given move result to the board state.
     *
     * @param result the move result to apply
     */
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

    /**
     * Computes the reward for the given move result.
     *
     * @param result the move result
     * @return the computed reward
     */
    public double computeReward(MoveResult result) {
        return result.getReward();
    }
}