package com.um_project_game.AI.MiniMax;

import com.um_project_game.board.Move;

import java.util.ArrayList;
import java.util.List;

public class MMResult {
    private double score;
    private List<Move> moves;

    public MMResult(double score, List<Move> moves) {
        this.score = score;
        this.moves = new ArrayList<>(moves);
    }

    public double getScore() {
        return score;
    }

    public List<Move> getMoves() {
        return moves;
    }
}