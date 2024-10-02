package com.um_project_game.util;

import org.joml.Vector2i;

public class TileConversion {

    public static int getRow(int tile) {
        return (tile - 1) / 5;
    }

    public static int getCol(int tile) {
        int remainder = tile % 10;

        // even tiles from 0 to 8
        if (remainder > 5) {
            return (remainder - 6) * 2;
        }

        // odd tiles from 1 to 9
        return remainder == 0 ?  9 : (remainder*2) - 1;
    }

    public static Vector2i getVector(int tile) {
        return new Vector2i(getRow(tile),getCol(tile));
    }
}
