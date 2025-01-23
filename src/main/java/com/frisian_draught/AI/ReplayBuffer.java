package com.frisian_draught.AI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The ReplayBuffer class stores experiences for training a reinforcement learning model.
 * It supports adding new experiences and sampling a batch of experiences for training.
 */
public class ReplayBuffer {
    private List<Experience> buffer;
    private int maxSize;

    /**
     * Constructs a ReplayBuffer with the specified maximum size.
     *
     * @param maxSize the maximum number of experiences to store in the buffer
     */
    public ReplayBuffer(int maxSize) {
        this.maxSize = maxSize;
        this.buffer = new ArrayList<>();
    }

    /**
     * Adds a new experience to the buffer. If the buffer is full, the oldest experience is removed.
     *
     * @param experience the experience to add
     */
    public void addExperience(Experience experience) {
        if (buffer.size() >= maxSize) {
            buffer.remove(0);
        }
        buffer.add(experience);
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return the number of experiences in the buffer
     */
    public int size() {
        return buffer.size();
    }

    /**
     * Samples a batch of experiences from the buffer.
     *
     * @param batchSize the number of experiences to sample
     * @return a list of sampled experiences
     */
    public List<Experience> sample(int batchSize) {
        List<Experience> sample = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < batchSize; i++) {
            sample.add(buffer.get(rand.nextInt(buffer.size())));
        }
        return sample;
    }
}