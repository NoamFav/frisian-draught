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

    public Move getBestMove(GameState state, int depth, boolean maximizingPlayer) {
        MMResult result =
                minimax(
                        state.clone(),
                        depth,
                        Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        maximizingPlayer,
                        0,
                        new ArrayList<>());
        if (result.getMoves().isEmpty()) {
            return null;
        }
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
                if (state.getMainBoard().moveManager.getPawnAtPosition(move.getStartPosition())
                        == null) {
                    System.out.println("Null pawn at position: " + move.getStartPosition());
                }
                System.out.println("SMove: " + move.getStartPosition());
                System.out.println("EMove: " + move.getEndPosition());
                state.printBoardState();
                GameState newState = state.simulateMove(move).getNextState();
                newState.printBoardState();
                List<Move> newMoveSequence = new ArrayList<>(moveSequence);
                newMoveSequence.add(move);
                double eval =
                        minimax(
                                        newState.clone(),
                                        depth - 1,
                                        alpha,
                                        beta,
                                        false,
                                        currentEval + evaluate(state, newState, maximizingPlayer),
                                        newMoveSequence)
                                .getScore();
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
                if (state.getMainBoard().moveManager.getPawnAtPosition(move.getStartPosition())
                        == null) {
                    System.out.println("Null pawn at position: " + move.getStartPosition());
                }
                System.out.println("SMove: " + move.getStartPosition());
                System.out.println("EMove: " + move.getEndPosition());
                state.printBoardState();
                GameState newState = state.simulateMove(move).getNextState();
                newState.printBoardState();
                List<Move> newMoveSequence = new ArrayList<>(moveSequence);
                newMoveSequence.add(move);
                double eval =
                        minimax(
                                        newState.clone(),
                                        depth - 1,
                                        alpha,
                                        beta,
                                        true,
                                        currentEval + evaluate(state, newState, maximizingPlayer),
                                        newMoveSequence)
                                .getScore();
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

        // Count the number of pawns and kings for both players
        long oldWhitePawns =
                oldBoardState.values().stream()
                        .filter(pawn -> pawn.isWhite() && !pawn.isKing())
                        .count();
        long newWhitePawns =
                newBoardState.values().stream()
                        .filter(pawn -> pawn.isWhite() && !pawn.isKing())
                        .count();
        long oldBlackPawns =
                oldBoardState.values().stream()
                        .filter(pawn -> !pawn.isWhite() && !pawn.isKing())
                        .count();
        long newBlackPawns =
                newBoardState.values().stream()
                        .filter(pawn -> !pawn.isWhite() && !pawn.isKing())
                        .count();
        long oldWhiteKings =
                oldBoardState.values().stream()
                        .filter(pawn -> pawn.isWhite() && pawn.isKing())
                        .count();
        long newWhiteKings =
                newBoardState.values().stream()
                        .filter(pawn -> pawn.isWhite() && pawn.isKing())
                        .count();
        long oldBlackKings =
                oldBoardState.values().stream()
                        .filter(pawn -> !pawn.isWhite() && pawn.isKing())
                        .count();
        long newBlackKings =
                newBoardState.values().stream()
                        .filter(pawn -> !pawn.isWhite() && pawn.isKing())
                        .count();

        // Calculate the difference in pawns and kings
        long whitePawnsCaptured = oldWhitePawns - newWhitePawns;
        long blackPawnsCaptured = oldBlackPawns - newBlackPawns;
        long whiteKingsCaptured = oldWhiteKings - newWhiteKings;
        long blackKingsCaptured = oldBlackKings - newBlackKings;

        // Evaluate based on captured pieces
        eval +=
                (blackPawnsCaptured * 1.0 + blackKingsCaptured * 3.0)
                        - (whitePawnsCaptured * 1.0 + whiteKingsCaptured * 3.0);

        // Evaluate based on control of the edges
        long whiteEdgeControl =
                newBoardState.values().stream()
                        .filter(pawn -> pawn.isWhite() && isEdge(pawn.getPosition()))
                        .count();
        long blackEdgeControl =
                newBoardState.values().stream()
                        .filter(pawn -> !pawn.isWhite() && isEdge(pawn.getPosition()))
                        .count();
        eval += (whiteEdgeControl - blackEdgeControl) * 0.5;

        // Evaluate based on mobility (number of possible moves)
        long whiteMobility =
                newState.generateMoves().stream()
                        .filter(move -> newBoardState.get(move.getStartPosition()).isWhite())
                        .count();
        long blackMobility =
                newState.generateMoves().stream()
                        .filter(move -> !newBoardState.get(move.getStartPosition()).isWhite())
                        .count();
        eval += (whiteMobility - blackMobility) * 0.1;

        System.out.println("Eval: " + eval);

        return eval;
    }

    private boolean isEdge(Vector2i position) {
        int x = position.x;

        return (x == 0 || x == 1 || x == 8 || x == 9);
    }
}
