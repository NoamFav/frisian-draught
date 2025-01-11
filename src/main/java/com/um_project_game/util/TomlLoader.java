package com.um_project_game.util;

import com.moandjiezana.toml.Toml;
import com.um_project_game.UserInfo;

import java.io.InputStream;
import java.util.Objects;

public class TomlLoader {

    public static UserInfo loadPlayerInfo() {
        try {
            // Load the TOML file
            InputStream input =
                    Objects.requireNonNull(
                            TomlLoader.class.getResourceAsStream("/user/info.toml"),
                            "TOML file not found!");

            if (input == null) {
                System.err.println("Error loading player data: File not found");
                return null;
            }
            // Parse the TOML file
            Toml toml = new Toml().read(input);

            if (toml == null) {
                System.err.println("Error loading player data: File is empty");
                return null;
            }

            // Create a Player object and map the values
            UserInfo player = new UserInfo();

            // Load basic profile info
            player.setName(toml.getString("name"));
            player.setLevel(toml.getLong("level").intValue());
            player.setExperience(toml.getLong("experience").intValue());
            player.setRank(toml.getString("rank"));

            // Load stats
            Toml stats = toml.getTable("stats");
            player.setGamesPlayed(stats.getLong("games_played").intValue());
            player.setGamesWon(stats.getLong("games_won").intValue());
            player.setGamesLost(stats.getLong("games_lost").intValue());
            player.setDraws(stats.getLong("draws").intValue());
            player.setWinStreak(stats.getLong("win_streak").intValue());
            player.setHighestWinStreak(stats.getLong("highest_win_streak").intValue());

            // Load special stats
            Toml specialStats = toml.getTable("special_stats");
            player.setKingsPromoted(specialStats.getLong("kings_promoted").intValue());
            player.setPiecesCaptured(specialStats.getLong("pieces_captured").intValue());
            player.setLongestGame(specialStats.getLong("longest_game").intValue());
            player.setFastestWin(specialStats.getLong("fastest_win").intValue());
            player.setMostPiecesCapturedInGame(
                    specialStats.getLong("most_pieces_captured_in_game").intValue());

            // Load trophies
            Toml trophies = toml.getTable("trophies");
            player.setTrophiesUnlocked(trophies.getList("trophies_unlocked"));
            player.setAchievements(trophies.getList("achievements"));

            // Load inventory
            Toml inventory = toml.getTable("inventory");
            player.setProfilePicture(inventory.getString("profile_picture"));
            player.setThemesUnlocked(inventory.getList("themes_unlocked"));
            player.setCurrentTheme(inventory.getString("current_theme"));
            player.setKingStylesUnlocked(inventory.getList("king_styles_unlocked"));
            player.setCurrentKingStyle(inventory.getString("current_king_style"));

            // Load multiplayer stats
            Toml multiplayerStats = toml.getTable("multiplayer_stats");
            player.setMultiplayerGamesPlayed(
                    multiplayerStats.getLong("multiplayer_games_played").intValue());
            player.setMultiplayerGamesWon(
                    multiplayerStats.getLong("multiplayer_games_won").intValue());
            player.setMultiplayerGamesLost(
                    multiplayerStats.getLong("multiplayer_games_lost").intValue());
            player.setMultiplayerRank(multiplayerStats.getString("multiplayer_rank"));

            // Load settings
            Toml settings = toml.getTable("settings");
            player.setSoundEffects(settings.getBoolean("sound_effects"));
            player.setMusic(settings.getBoolean("music"));
            player.setNotifications(settings.getBoolean("notifications"));
            player.setDifficultyLevel(settings.getString("difficulty_level"));
            player.setDarkMode(settings.getBoolean("dark_mode"));

            // Load performance tracking
            Toml performance = toml.getTable("performance");
            player.setAccuracy(performance.getLong("accuracy").intValue());
            player.setCriticalMovesMade(performance.getLong("critical_moves_made").intValue());
            player.setMistakesMade(performance.getLong("mistakes_made").intValue());
            player.setAverageGameDuration(performance.getLong("average_game_duration").intValue());

            // Load connections
            Toml connections = toml.getTable("connections");
            player.setFriends(connections.getList("friends"));
            player.setBlockedPlayers(connections.getList("blocked_players"));

            // Load custom notes
            Toml notes = toml.getTable("notes");
            player.setPersonalNotes(notes.getString("personal_notes"));

            return player;

        } catch (Exception e) {
            System.err.println("Error loading player data: " + e.getMessage());
            return null;
        }
    }
}
