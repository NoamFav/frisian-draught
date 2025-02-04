package com.frisian_draught.util;

import org.joml.Vector2i;

public class TileConversion {

    public static int getRow(int tile) {
        return (tile - 1) / 5;
    }

    public static int getCol(int tile) {
        int remainder = tile % 10;

        // even tiles from 0 to 6
        if (remainder > 5) {
            return (remainder - 6) * 2;
        }

        // remaining tiles
        return remainder == 0 ?  8 : (remainder*2) - 1;
    }

    // returns the vector from portable draughts notation
    public static Vector2i getTileVector(int tile) {
        return new Vector2i(getCol(tile), getRow(tile));
    }

    // returns the tile in portable draughts notation ( 1 through 50 )
    public static int getTileNotation(Vector2i vector2i) {
        return vector2i.y * 5 + vector2i.x / 2 + 1;
    }
}
