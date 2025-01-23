package com.frisian_draught.util;

import com.moandjiezana.toml.Toml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TrophyLoader {

    public static Map<String, Trophy> loadTrophies() {
        try {
            // Load the TOML file from resources
            InputStream input =
                    Objects.requireNonNull(
                            TrophyLoader.class.getResourceAsStream("/user/trophy.toml"),
                            "TOML file not found!");

            Map<String, Trophy> trophies = new HashMap<>();

            // Parse the TOML file
            Toml toml = new Toml().read(input);

            // Loop through trophies
            for (int i = 1; i <= 50; i++) {
                // Use default values if keys are missing
                String name = toml.getString("trophy_" + i + "_name", "Unknown Trophy");
                String description =
                        toml.getString("trophy_" + i + "_description", "No description available");
                boolean unlocked = toml.getBoolean("trophy_" + i + "_unlocked", false);

                // Create Trophy object and add to map
                Trophy trophy = new Trophy(name, description, unlocked);
                trophies.put(name, trophy);
            }

            System.out.println("Trophies loaded successfully!");
            return trophies;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
