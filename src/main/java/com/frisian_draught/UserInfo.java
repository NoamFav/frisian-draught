package com.frisian_draught;

import java.util.ArrayList;
import java.util.List;

/** The UserInfo class is responsible for storing the user's information and statistics */
public class UserInfo {
    private String name;
    private int level;
    private int experience;
    private String rank;
    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;
    private int draws;
    private int winStreak;
    private int highestWinStreak;
    private int kingsPromoted;
    private int piecesCaptured;
    private int longestGame;
    private int fastestWin;
    private int mostPiecesCapturedInGame;
    private List<String> trophiesUnlocked;
    private List<String> achievements;
    private String profilePicture;
    private List<String> themesUnlocked;
    private String currentTheme;
    private List<String> kingStylesUnlocked;
    private String currentKingStyle;
    private int multiplayerGamesPlayed;
    private int multiplayerGamesWon;
    private int multiplayerGamesLost;
    private String multiplayerRank;
    private boolean soundEffects;
    private boolean music;
    private boolean notifications;
    private String difficultyLevel;
    private boolean darkMode;
    private int accuracy;
    private int criticalMovesMade;
    private int mistakesMade;
    private int averageGameDuration;
    private List<String> friends;
    private List<String> blockedPlayers;
    private String personalNotes;

    List<Integer> levelThresholds;

    public UserInfo() {
        this.levelThresholds = generateLevelThresholds(100, 1.3, 154);
        System.out.println("Level Thresholds: " + levelThresholds);
    }

    /**
     * Generates the level thresholds for the user Uses an exponential growth factor to calculate
     * the XP required to reach each Level. Makes the XP required to reach each level increase
     * exponentially
     *
     * @param baseXP The base XP required to reach level 1
     * @param growthFactor The growth factor for the XP required to reach the next level
     * @param maxLevel The maximum level that can be reached
     * @return A list of the XP thresholds required to reach each level
     */
    public List<Integer> generateLevelThresholds(int baseXP, double growthFactor, int maxLevel) {
        List<Integer> thresholds = new ArrayList<>();
        thresholds.add(0); // Level 0 XP

        for (int i = 1; i <= maxLevel; i++) {
            // XP required to reach the next level is calculated using an exponential growth factor
            int xp = (int) (baseXP * Math.pow(i, growthFactor));
            thresholds.add(thresholds.get(i - 1) + xp);
        }
        return thresholds;
    }

    public List<Integer> getLevelThresholds() {
        return levelThresholds;
    }

    /** Updates the user's level and rank based on their current experience */
    public void updateLevel() {
        for (int i = 1; i < levelThresholds.size(); i++) {
            if (experience < levelThresholds.get(i)) {
                level = i - 1;
                // Assign rank based on level range (Love the name of the ranks)
                assignRank(level);
                break;
            }
        }
        level = levelThresholds.size() - 1;
        assignRank(level);
    }

    public void assignRank(int level) {
        switch (level) {
            case 0, 1, 2, 3, 4 -> rank = "Beginner";
            case 5, 6, 7, 8, 9 -> rank = "Novice";
            case 10, 11, 12, 13, 14 -> rank = "Apprentice";
            case 15, 16, 17, 18, 19 -> rank = "Intermediate";
            case 20, 21, 22, 23, 24 -> rank = "Skilled";
            case 25, 26, 27, 28, 29 -> rank = "Advanced";
            case 30, 31, 32, 33, 34 -> rank = "Expert";
            case 35, 36, 37, 38, 39 -> rank = "Master";
            case 40, 41, 42, 43, 44 -> rank = "Grandmaster";
            case 45, 46, 47, 48, 49 -> rank = "Legend";
            case 50, 51, 52, 53, 54 -> rank = "Myth";
            case 55, 56, 57, 58, 59 -> rank = "Immortal";
            case 60, 61, 62, 63, 64 -> rank = "Divine";
            case 65, 66, 67, 68, 69 -> rank = "God";
            case 70, 71, 72, 73, 74 -> rank = "Titan";
            case 75, 76, 77, 78, 79 -> rank = "Kratos";
            case 80, 81, 82, 83, 84 -> rank = "The One";
            case 85, 86, 87, 88, 89 -> rank = "Infinite";
            case 90, 91, 92, 93, 94 -> rank = "Game Over";
            case 95, 96, 97, 98, 99 -> rank = "Why Are You Here?";
            case 100, 101, 102, 103, 104 -> rank = "Touch Some Grass";
            case 105, 106, 107, 108, 109 -> rank = "Get a Life";
            case 110, 111, 112, 113, 114 -> rank = "You're Addicted";
            case 115, 116, 117, 118, 119 -> rank = "You Need Help";
            case 120, 121, 122, 123, 124 -> rank = "Lost Cause";
            case 125, 126, 127, 128, 129 -> rank = "Stop Playing!!";
            case 130, 131, 132, 133, 134 -> rank = "Try League of Legends?";
            case 135, 136, 137, 138, 139 -> rank = "Do You Have a Life?";
            case 140, 141, 142, 143, 144 -> rank = "I Give Up";
            case 145, 146, 147, 148, 149 -> rank = "Seriously??";
            case 150, 151, 152, 153, 154 -> rank = "Want a Hug?";
            default -> rank = "Unknown Rank";
        }
    }

    /**
     * Adds experience to the user and updates their level
     *
     * @param xp The amount of experience to add
     */
    public void addExperience(int xp) {
        experience += xp;
        updateLevel();
    }

    public void playedGame() {
        gamesPlayed++;
        addExperience(30);
    }

    public void wonGame() {
        gamesWon++;
        winStreak++;
        if (winStreak > highestWinStreak) {
            highestWinStreak = winStreak;
            if (winStreak >= 3) {
                addExperience(100);
            } else if (winStreak >= 5) {
                addExperience(200);
            } else if (winStreak >= 10) {
                addExperience(500);
            }
        }
        addExperience(150);
    }

    public void lostGame() {
        gamesLost++;
        winStreak = 0;
        addExperience(50);
    }

    public void forfeitedGame() {
        gamesLost++;
        winStreak = 0;
        addExperience(25);
    }

    public void drewGame() {
        draws++;
        addExperience(75);
    }

    public void completedTutorialStage() {
        addExperience(50);
    }

    public void completedTutorial() {
        addExperience(200);
    }

    public void capturedPiece() {
        piecesCaptured++;
        addExperience(20);
    }

    public void capturedKing() {
        piecesCaptured += 2;
        addExperience(40);
    }

    public void promotedKing() {
        kingsPromoted++;
        addExperience(75);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public int getWinStreak() {
        return winStreak;
    }

    public void setWinStreak(int winStreak) {
        this.winStreak = winStreak;
    }

    public int getHighestWinStreak() {
        return highestWinStreak;
    }

    public void setHighestWinStreak(int highestWinStreak) {
        this.highestWinStreak = highestWinStreak;
    }

    public int getKingsPromoted() {
        return kingsPromoted;
    }

    public void setKingsPromoted(int kingsPromoted) {
        this.kingsPromoted = kingsPromoted;
    }

    public int getPiecesCaptured() {
        return piecesCaptured;
    }

    public void setPiecesCaptured(int piecesCaptured) {
        this.piecesCaptured = piecesCaptured;
    }

    public int getLongestGame() {
        return longestGame;
    }

    public void setLongestGame(int longestGame) {
        this.longestGame = longestGame;
    }

    public int getFastestWin() {
        return fastestWin;
    }

    public void setFastestWin(int fastestWin) {
        this.fastestWin = fastestWin;
    }

    public int getMostPiecesCapturedInGame() {
        return mostPiecesCapturedInGame;
    }

    public void setMostPiecesCapturedInGame(int mostPiecesCapturedInGame) {
        this.mostPiecesCapturedInGame = mostPiecesCapturedInGame;
    }

    public List<String> getTrophiesUnlocked() {
        return trophiesUnlocked;
    }

    public void setTrophiesUnlocked(List<String> trophiesUnlocked) {
        this.trophiesUnlocked = trophiesUnlocked;
    }

    public List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<String> achievements) {
        this.achievements = achievements;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public List<String> getThemesUnlocked() {
        return themesUnlocked;
    }

    public void setThemesUnlocked(List<String> themesUnlocked) {
        this.themesUnlocked = themesUnlocked;
    }

    public String getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(String currentTheme) {
        this.currentTheme = currentTheme;
    }

    public List<String> getKingStylesUnlocked() {
        return kingStylesUnlocked;
    }

    public void setKingStylesUnlocked(List<String> kingStylesUnlocked) {
        this.kingStylesUnlocked = kingStylesUnlocked;
    }

    public String getCurrentKingStyle() {
        return currentKingStyle;
    }

    public void setCurrentKingStyle(String currentKingStyle) {
        this.currentKingStyle = currentKingStyle;
    }

    public int getMultiplayerGamesPlayed() {
        return multiplayerGamesPlayed;
    }

    public void setMultiplayerGamesPlayed(int multiplayerGamesPlayed) {
        this.multiplayerGamesPlayed = multiplayerGamesPlayed;
    }

    public int getMultiplayerGamesWon() {
        return multiplayerGamesWon;
    }

    public void setMultiplayerGamesWon(int multiplayerGamesWon) {
        this.multiplayerGamesWon = multiplayerGamesWon;
    }

    public int getMultiplayerGamesLost() {
        return multiplayerGamesLost;
    }

    public void setMultiplayerGamesLost(int multiplayerGamesLost) {
        this.multiplayerGamesLost = multiplayerGamesLost;
    }

    public String getMultiplayerRank() {
        return multiplayerRank;
    }

    public void setMultiplayerRank(String multiplayerRank) {
        this.multiplayerRank = multiplayerRank;
    }

    public boolean isSoundEffects() {
        return soundEffects;
    }

    public void setSoundEffects(boolean soundEffects) {
        this.soundEffects = soundEffects;
    }

    public boolean isMusic() {
        return music;
    }

    public void setMusic(boolean music) {
        this.music = music;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public int getCriticalMovesMade() {
        return criticalMovesMade;
    }

    public void setCriticalMovesMade(int criticalMovesMade) {
        this.criticalMovesMade = criticalMovesMade;
    }

    public int getMistakesMade() {
        return mistakesMade;
    }

    public void setMistakesMade(int mistakesMade) {
        this.mistakesMade = mistakesMade;
    }

    public int getAverageGameDuration() {
        return averageGameDuration;
    }

    public void setAverageGameDuration(int averageGameDuration) {
        this.averageGameDuration = averageGameDuration;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getBlockedPlayers() {
        return blockedPlayers;
    }

    public void setBlockedPlayers(List<String> blockedPlayers) {
        this.blockedPlayers = blockedPlayers;
    }

    public String getPersonalNotes() {
        return personalNotes;
    }

    public void setPersonalNotes(String personalNotes) {
        this.personalNotes = personalNotes;
    }
}
