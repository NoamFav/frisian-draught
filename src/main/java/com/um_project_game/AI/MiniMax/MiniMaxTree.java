package com.um_project_game.AI.MiniMax;

import com.um_project_game.board.GameState;
import com.um_project_game.board.Move;
import com.um_project_game.board.Pawn;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MiniMaxTree {

    public GameState rootState;

    public MiniMaxTree(GameState rootState) {
        this.rootState = rootState;
    }

    public Move getBestMove(GameState state, int depth) {
        MMResult result = minimax(state.clone(), depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false, 0, new ArrayList<>());
        return result.getMoves().get(0);
    }

    public MMResult minimax(
            GameState state,
            int depth,
            double alpha,
            double beta,
            boolean maximizingPlayer,
            double currentEval,
            List<Move> moveSequence) {
        if (depth == 0 || state.isTerminal()) {
            return new MMResult(currentEval, moveSequence);
        }

        if (maximizingPlayer) {
            double maxEval = Double.NEGATIVE_INFINITY;
            List<Move> bestMoveSequence = new ArrayList<>();
            for (Move move : state.generateMoves()) {
                if (state.getMainBoard().moveManager.getPawnAtPosition(move.getStartPosition()) == null) {
                    continue; // Skip invalid moves
                }
                System.out.println("SMove: " + move.getStartPosition());
                System.out.println("EMove: " + move.getEndPosition());
                state.printBoardState();
                GameState newState = state.simulateMove(move).getNextState();
                newState.printBoardState();
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
                if (state.getMainBoard().moveManager.getPawnAtPosition(move.getStartPosition()) == null) {
                    continue; // Skip invalid moves
                }
                System.out.println("SMove: " + move.getStartPosition());
                System.out.println("EMove: " + move.getEndPosition());
                state.printBoardState();
                GameState newState = state.simulateMove(move).getNextState();
                newState.printBoardState();
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

    public double evaluate(GameState oldState, GameState newState, boolean maximizingPlayer) {
        Map<Vector2i, Pawn> oldBoardState = oldState.getBoardState();
        Map<Vector2i, Pawn> newBoardState = newState.getBoardState();
        double eval = 0;
        long oldBlackCount = oldBoardState.values().stream().filter(pawn -> !pawn.isWhite()).count();
        long newBlackCount = newBoardState.values().stream().filter(pawn -> !pawn.isWhite()).count();
        long blackPiecesCaptured = oldBlackCount - newBlackCount;
        eval += blackPiecesCaptured;

        return eval;
    }

}