package com.um_project_game.util;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.um_project_game.UserInfo;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading and saving UserInfo from a TOML file.
 *
 * <p>On first run, this class copies the default info.toml from resources (/user/info.toml inside
 * the JAR) to an external location: ~/.um_project_game/info.toml (on Linux/Mac), or
 * C:\\Users\\username\\.um_project_game\\info.toml (on Windows).
 *
 * <p>Then it always reads/writes from that external file.
 */
public class TomlLoader {

    // Path to external config file in the user's home directory.
    // Adjust this as needed for your app.
    private static final Path EXTERNAL_CONFIG_PATH =
            Paths.get(System.getProperty("user.home"), ".frisian-draught", "info.toml");

    /**
     * Ensures that an external copy of info.toml exists in a writable location. Copies it from the
     * JAR resource if it does not already exist.
     *
     * <p>Call this once at application startup, before loadPlayerInfo().
     */
    public static void ensureExternalConfigExists() throws IOException {
        // Create directories if they don't exist
        Files.createDirectories(EXTERNAL_CONFIG_PATH.getParent());

        // If the file doesn't already exist externally, copy from the JAR resource
        if (!Files.exists(EXTERNAL_CONFIG_PATH)) {
            try (InputStream inputStream =
                    TomlLoader.class.getResourceAsStream("/user/info.toml")) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Resource '/user/info.toml' not found in JAR!");
                }
                Files.copy(inputStream, EXTERNAL_CONFIG_PATH);
                System.out.println("Copied default info.toml to " + EXTERNAL_CONFIG_PATH);
            }
        }
    }

    /**
     * Loads player info from the external TOML file into a UserInfo object.
     *
     * @return a populated UserInfo object, or null if an error occurs
     */
    public static UserInfo loadPlayerInfo() {
        try (InputStream input = Files.newInputStream(EXTERNAL_CONFIG_PATH)) {
            Toml toml = new Toml().read(input);

            if (toml == null) {
                System.err.println("Error loading player data: TOML is empty");
                return null;
            }

            UserInfo userInfo = new UserInfo();
            // --- Basic profile info ---
            userInfo.setName(toml.getString("name"));
            userInfo.setLevel(toml.getLong("level").intValue());
            userInfo.setExperience(toml.getLong("experience").intValue());
            userInfo.setRank(toml.getString("rank"));

            // --- stats table ---
            Toml stats = toml.getTable("stats");
            userInfo.setGamesPlayed(stats.getLong("games_played").intValue());
            userInfo.setGamesWon(stats.getLong("games_won").intValue());
            userInfo.setGamesLost(stats.getLong("games_lost").intValue());
            userInfo.setDraws(stats.getLong("draws").intValue());
            userInfo.setWinStreak(stats.getLong("win_streak").intValue());
            userInfo.setHighestWinStreak(stats.getLong("highest_win_streak").intValue());

            // --- special_stats table ---
            Toml specialStats = toml.getTable("special_stats");
            userInfo.setKingsPromoted(specialStats.getLong("kings_promoted").intValue());
            userInfo.setPiecesCaptured(specialStats.getLong("pieces_captured").intValue());
            userInfo.setLongestGame(specialStats.getLong("longest_game").intValue());
            userInfo.setFastestWin(specialStats.getLong("fastest_win").intValue());
            userInfo.setMostPiecesCapturedInGame(
                    specialStats.getLong("most_pieces_captured_in_game").intValue());

            // --- trophies table ---
            Toml trophies = toml.getTable("trophies");
            userInfo.setTrophiesUnlocked(trophies.getList("trophies_unlocked"));
            userInfo.setAchievements(trophies.getList("achievements"));

            // --- inventory table ---
            Toml inventory = toml.getTable("inventory");
            userInfo.setProfilePicture(inventory.getString("profile_picture"));
            userInfo.setThemesUnlocked(inventory.getList("themes_unlocked"));
            userInfo.setCurrentTheme(inventory.getString("current_theme"));
            userInfo.setKingStylesUnlocked(inventory.getList("king_styles_unlocked"));
            userInfo.setCurrentKingStyle(inventory.getString("current_king_style"));

            // --- multiplayer_stats table ---
            Toml multiplayerStats = toml.getTable("multiplayer_stats");
            userInfo.setMultiplayerGamesPlayed(
                    multiplayerStats.getLong("multiplayer_games_played").intValue());
            userInfo.setMultiplayerGamesWon(
                    multiplayerStats.getLong("multiplayer_games_won").intValue());
            userInfo.setMultiplayerGamesLost(
                    multiplayerStats.getLong("multiplayer_games_lost").intValue());
            userInfo.setMultiplayerRank(multiplayerStats.getString("multiplayer_rank"));

            // --- settings table ---
            Toml settings = toml.getTable("settings");
            userInfo.setSoundEffects(settings.getBoolean("sound_effects"));
            userInfo.setMusic(settings.getBoolean("music"));
            userInfo.setNotifications(settings.getBoolean("notifications"));
            userInfo.setDifficultyLevel(settings.getString("difficulty_level"));
            userInfo.setDarkMode(settings.getBoolean("dark_mode"));

            // --- performance table ---
            Toml performance = toml.getTable("performance");
            userInfo.setAccuracy(performance.getLong("accuracy").intValue());
            userInfo.setCriticalMovesMade(performance.getLong("critical_moves_made").intValue());
            userInfo.setMistakesMade(performance.getLong("mistakes_made").intValue());
            userInfo.setAverageGameDuration(
                    performance.getLong("average_game_duration").intValue());

            // --- connections table ---
            Toml connections = toml.getTable("connections");
            userInfo.setFriends(connections.getList("friends"));
            userInfo.setBlockedPlayers(connections.getList("blocked_players"));

            // --- notes table ---
            Toml notes = toml.getTable("notes");
            userInfo.setPersonalNotes(notes.getString("personal_notes"));

            return userInfo;

        } catch (Exception e) {
            System.err.println("Error loading player data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /** Saves the given UserInfo object back to the external TOML file. */
    public static void savePlayerInfo(UserInfo userInfo) {
        // Build a map representing the TOML structure.
        Map<String, Object> root = new HashMap<>();

        // --- Basic profile info ---
        root.put("name", userInfo.getName());
        root.put("level", userInfo.getLevel());
        root.put("experience", userInfo.getExperience());
        root.put("rank", userInfo.getRank());

        // --- stats table ---
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("games_played", userInfo.getGamesPlayed());
        statsMap.put("games_won", userInfo.getGamesWon());
        statsMap.put("games_lost", userInfo.getGamesLost());
        statsMap.put("draws", userInfo.getDraws());
        statsMap.put("win_streak", userInfo.getWinStreak());
        statsMap.put("highest_win_streak", userInfo.getHighestWinStreak());
        root.put("stats", statsMap);

        // --- special_stats table ---
        Map<String, Object> specialStatsMap = new HashMap<>();
        specialStatsMap.put("kings_promoted", userInfo.getKingsPromoted());
        specialStatsMap.put("pieces_captured", userInfo.getPiecesCaptured());
        specialStatsMap.put("longest_game", userInfo.getLongestGame());
        specialStatsMap.put("fastest_win", userInfo.getFastestWin());
        specialStatsMap.put("most_pieces_captured_in_game", userInfo.getMostPiecesCapturedInGame());
        root.put("special_stats", specialStatsMap);

        // --- trophies table ---
        Map<String, Object> trophiesMap = new HashMap<>();
        trophiesMap.put("trophies_unlocked", userInfo.getTrophiesUnlocked());
        trophiesMap.put("achievements", userInfo.getAchievements());
        root.put("trophies", trophiesMap);

        // --- inventory table ---
        Map<String, Object> inventoryMap = new HashMap<>();
        inventoryMap.put("profile_picture", userInfo.getProfilePicture());
        inventoryMap.put("themes_unlocked", userInfo.getThemesUnlocked());
        inventoryMap.put("current_theme", userInfo.getCurrentTheme());
        inventoryMap.put("king_styles_unlocked", userInfo.getKingStylesUnlocked());
        inventoryMap.put("current_king_style", userInfo.getCurrentKingStyle());
        root.put("inventory", inventoryMap);

        // --- multiplayer_stats table ---
        Map<String, Object> multiplayerStatsMap = new HashMap<>();
        multiplayerStatsMap.put("multiplayer_games_played", userInfo.getMultiplayerGamesPlayed());
        multiplayerStatsMap.put("multiplayer_games_won", userInfo.getMultiplayerGamesWon());
        multiplayerStatsMap.put("multiplayer_games_lost", userInfo.getMultiplayerGamesLost());
        multiplayerStatsMap.put("multiplayer_rank", userInfo.getMultiplayerRank());
        root.put("multiplayer_stats", multiplayerStatsMap);

        // --- settings table ---
        Map<String, Object> settingsMap = new HashMap<>();
        settingsMap.put("sound_effects", userInfo.isSoundEffects());
        settingsMap.put("music", userInfo.isMusic());
        settingsMap.put("notifications", userInfo.isNotifications());
        settingsMap.put("difficulty_level", userInfo.getDifficultyLevel());
        settingsMap.put("dark_mode", userInfo.isDarkMode());
        root.put("settings", settingsMap);

        // --- performance table ---
        Map<String, Object> performanceMap = new HashMap<>();
        performanceMap.put("accuracy", userInfo.getAccuracy());
        performanceMap.put("critical_moves_made", userInfo.getCriticalMovesMade());
        performanceMap.put("mistakes_made", userInfo.getMistakesMade());
        performanceMap.put("average_game_duration", userInfo.getAverageGameDuration());
        root.put("performance", performanceMap);

        // --- connections table ---
        Map<String, Object> connectionsMap = new HashMap<>();
        connectionsMap.put("friends", userInfo.getFriends());
        connectionsMap.put("blocked_players", userInfo.getBlockedPlayers());
        root.put("connections", connectionsMap);

        // --- notes table ---
        Map<String, Object> notesMap = new HashMap<>();
        notesMap.put("personal_notes", userInfo.getPersonalNotes());
        root.put("notes", notesMap);

        // Write the map to the external TOML file
        TomlWriter tomlWriter = new TomlWriter();
        try (BufferedWriter writer = Files.newBufferedWriter(EXTERNAL_CONFIG_PATH)) {
            tomlWriter.write(root, writer);
            System.out.println("User info successfully saved to " + EXTERNAL_CONFIG_PATH);
        } catch (IOException e) {
            System.err.println("Error saving player data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
