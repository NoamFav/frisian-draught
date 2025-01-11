package com.um_project_game.util;

public class Trophy {
    private String name;
    private String description;
    private boolean unlocked;

    public Trophy(String name, String description, boolean unlocked) {
        this.name = name;
        this.description = description;
        this.unlocked = unlocked;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    @Override
    public String toString() {
        return "Trophy{name='"
                + name
                + '\''
                + ", description='"
                + description
                + '\''
                + ", unlocked="
                + unlocked
                + '}';
    }
}
