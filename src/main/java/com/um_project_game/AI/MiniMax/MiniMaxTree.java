package com.um_project_game.AI.MiniMax;

import com.um_project_game.board.GameState;
import com.um_project_game.board.Move;
import com.um_project_game.board.Pawn;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The MiniMaxTree class implements the MiniMax algorithm with alpha-beta pruning
 * to determine the best move in a game state.
 */
public class MiniMaxTree {

    public GameState rootState;

    /**
     * Constructs a MiniMaxTree with the given root state.
     *
     * @param rootState the initial game state
     */
    public MiniMaxTree(GameState rootState) {
        this.rootState = rootState;
    }

    /**
     * Gets the best move for the given game state using the MiniMax algorithm.
     *
     * @param state the current game state
     * @param depth the depth to search in the game tree
     * @param maximizingPlayer true if the current player is maximizing, false otherwise
     * @return the best move found
     */
    public Move getBestMove(GameState state, int depth, boolean maximizingPlayer) {
        MMResult result = minimax(state.clone(), depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, maximizingPlayer, 0, new ArrayList<>());
        if (result.getMoves().isEmpty()) {
            return null;
        }
        return result.getMoves().get(0);
    }

    /**
     * Implements the MiniMax algorithm with alpha-beta pruning.
     *
     * @param state the current game state
     * @param depth the depth to search in the game tree
     * @param alpha the alpha value for alpha-beta pruning
     * @param beta the beta value for alpha-beta pruning
     * @param maximizingPlayer true if the current player is maximizing, false otherwise
     * @param currentEval the current evaluation score
     * @param moveSequence the sequence of moves leading to the current state
     * @return the result of the MiniMax evaluation
     */
    public MMResult minimax(GameState state, int depth, double alpha, double beta, boolean maximizingPlayer, double currentEval, List<Move> moveSequence) {
        if (depth == 0 || state.isTerminal()) {
            return new MMResult(currentEval, moveSequence);
        }

        if (maximizingPlayer) {
            double maxEval = Double.NEGATIVE_INFINITY;
            List<Move> bestMoveSequence = new ArrayList<>();
            for (Move move : state.generateMoves()) {
                GameState newState = state.simulateMove(move).getNextState();
                List<Move> newMoveSequence = new ArrayList<>(moveSequence);
                newMoveSequence.add(move);
                double eval = minimax(newState.clone(), depth - 1, alpha, beta, false, currentEval + evaluate(state, newState, maximizingPlayer), newMoveSequence).getScore();
                if (eval > maxEval) {
                    maxEval = eval;
                    bestMoveSequence = newMoveSequence;
                }
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return new MMResult(maxEval, bestMoveSequence);
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            List<Move> bestMoveSequence = new ArrayList<>();
            for (Move move : state.generateMoves()) {
                GameState newState = state.simulateMove(move).getNextState();
                List<Move> newMoveSequence = new ArrayList<>(moveSequence);
                newMoveSequence.add(move);
                double eval = minimax(newState.clone(), depth - 1, alpha, beta, true, currentEval + evaluate(state, newState, maximizingPlayer), newMoveSequence).getScore();
                if (eval < minEval) {
                    minEval = eval;
                    bestMoveSequence = newMoveSequence;
                }
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return new MMResult(minEval, bestMoveSequence);
        }
    }

    /**
     * Evaluates the difference between the old and new game states.
     *
     * @param oldState the previous game state
     * @param newState the new game state
     * @param maximizingPlayer true if the current player is maximizing, false otherwise
     * @return the evaluation score
     */
    public double evaluate(GameState oldState, GameState newState, boolean maximizingPlayer) {
        Map<Vector2i, Pawn> oldBoardState = oldState.getBoardState();
        Map<Vector2i, Pawn> newBoardState = newState.getBoardState();
        double eval = 0;

        long oldWhitePawns = oldBoardState.values().stream().filter(pawn -> pawn.isWhite() && !pawn.isKing()).count();
        long newWhitePawns = newBoardState.values().stream().filter(pawn -> pawn.isWhite() && !pawn.isKing()).count();
        long oldBlackPawns = oldBoardState.values().stream().filter(pawn -> !pawn.isWhite() && !pawn.isKing()).count();
        long newBlackPawns = newBoardState.values().stream().filter(pawn -> !pawn.isWhite() && !pawn.isKing()).count();
        long oldWhiteKings = oldBoardState.values().stream().filter(pawn -> pawn.isWhite() && pawn.isKing()).count();
        long newWhiteKings = newBoardState.values().stream().filter(pawn -> pawn.isWhite() && pawn.isKing()).count();
        long oldBlackKings = oldBoardState.values().stream().filter(pawn -> !pawn.isWhite() && pawn.isKing()).count();
        long newBlackKings = newBoardState.values().stream().filter(pawn -> !pawn.isWhite() && pawn.isKing()).count();

        // Calculate the difference in pawns and kings, white maximizing, black minimizing
        long whitePawnsCaptured = oldWhitePawns - newWhitePawns;
        long blackPawnsCaptured = oldBlackPawns - newBlackPawns;
        long whiteKingsCaptured = oldWhiteKings - newWhiteKings;
        long blackKingsCaptured = oldBlackKings - newBlackKings;

        // Evaluate based on captured pieces
        eval += (blackPawnsCaptured * 1.0 + blackKingsCaptured * 3.0) - (whitePawnsCaptured * 1.0 + whiteKingsCaptured * 3.0);

        // Evaluate based on control of the edges
        long whiteEdgeControl = newBoardState.values().stream().filter(pawn -> pawn.isWhite() && isEdge(pawn.getPosition())).count();
        long blackEdgeControl = newBoardState.values().stream().filter(pawn -> !pawn.isWhite() && isEdge(pawn.getPosition())).count();
        eval += (whiteEdgeControl - blackEdgeControl) * 0.5;

        // Evaluate based on mobility (number of possible moves per piece)
        long whiteMobility = newState.generateMoves().stream().filter(move -> newBoardState.get(move.getStartPosition()).isWhite()).count();
        long blackMobility = newState.generateMoves().stream().filter(move -> !newBoardState.get(move.getStartPosition()).isWhite()).count();
        eval += (whiteMobility - blackMobility) * 0.1;

        return eval;
    }

    /**
     * Checks if a position is on the edge of the board.
     *
     * @param position the position to check
     * @return true if the position is on the edge, false otherwise
     */
    private boolean isEdge(Vector2i position) {
        int x = position.x;
        return (x == 0 || x == 1 || x == 8 || x == 9);
    }
}