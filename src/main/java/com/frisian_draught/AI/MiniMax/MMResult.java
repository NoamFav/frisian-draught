package com.frisian_draught.AI.MiniMax;

import com.frisian_draught.board.Move;

import java.util.ArrayList;
import java.util.List;

/**
 * The MMResult class represents the result of a MiniMax evaluation.
 * It contains the evaluation score and the sequence of moves leading to that score.
 */
public class MMResult {
    private double score;
    private List<Move> moves;

    /**
     * Constructs an MMResult with the given score and list of moves.
     *
     * @param score the evaluation score
     * @param moves the sequence of moves leading to the score
     */
    public MMResult(double score, List<Move> moves) {
        this.score = score;
        this.moves = new ArrayList<>(moves);
    }

    /**
     * Returns the evaluation score.
     *
     * @return the evaluation score
     */
    public double getScore() {
        return score;
    }

    /**
     * Returns the sequence of moves leading to the score.
     *
     * @return the list of moves
     */
    public List<Move> getMoves() {
        return moves;
    }
}