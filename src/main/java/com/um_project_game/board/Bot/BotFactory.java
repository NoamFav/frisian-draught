package com.um_project_game.board.Bot;

import com.um_project_game.board.MainBoard;

public class BotFactory {

    public static Bot createBot(BotType botType, MainBoard mainBoard) {
        switch (botType) {
            case RANDOM_BOT:
                return new RandomBot(
                        mainBoard.boardState,
                        mainBoard.moveManager,
                        mainBoard,
                        mainBoard.boardState.getReplayBuffer(),
                        mainBoard.boardRendered);
            case MINIMAX_BOT:
                return new MinimaxBot(
                        mainBoard.boardState,
                        mainBoard.moveManager,
                        mainBoard,
                        mainBoard.boardState.getReplayBuffer(),
                        mainBoard.boardRendered);
            case DQN_BOT:
                return new DQNBot(
                        mainBoard.boardState,
                        mainBoard.moveManager,
                        mainBoard,
                        mainBoard.boardState.getReplayBuffer(),
                        mainBoard.boardRendered);
            case HYBRID_BOT:
                return new HybridBot(
                        mainBoard.boardState,
                        mainBoard.moveManager,
                        mainBoard,
                        mainBoard.boardState.getReplayBuffer(),
                        mainBoard.boardRendered);
            default:
                throw new IllegalArgumentException("Unknown BotType: " + botType);
        }
    }
}
