package com.frisian_draught.util;

public class PDNConstraints {

    // Constant values for PDN file generation
    public static final String DEFAULT_EVENT = "Player against Player";
    public static final String DEFAULT_SITE = "Project 2-1";
    public static final String DEFAULT_WHITE_PLAYER = "Player1";
    public static final String DEFAULT_BLACK_PLAYER = "Player2";
    public static final String DEFAULT_RESULT_DRAW = "1/2-1/2"; // Draw result
    public static final String DEFAULT_RESULT_WHITE_WIN = "1-0"; // White wins
    public static final String DEFAULT_RESULT_BLACK_WIN = "0-1"; // Black wins
    public static final String GAMETYPE =
            "40"; // Official game type "frisian" constant for PDN files

    private PDNConstraints() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }
}
