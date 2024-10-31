package com.um_project_game.AI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReplayBuffer {
    private List<Experience> buffer;
    private int maxSize;

    public ReplayBuffer(int maxSize) {
        this.maxSize = maxSize;
        this.buffer = new ArrayList<>();
    }

    public void addExperience(Experience experience) {
        if (buffer.size() >= maxSize) {
            buffer.remove(0);
        }
        buffer.add(experience);
    }

    public List<Experience> sample(int batchSize) {
        List<Experience> sample = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < batchSize; i++) {
            sample.add(buffer.get(rand.nextInt(buffer.size())));
        }
        return sample;
    }
}
