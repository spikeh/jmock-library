package org.jmock.internal.perf.distribution;

import java.util.Random;

public class Normal implements Distribution {
    private final Random random;
    private double mean;
    private double stdev;

    public Normal(double mean, double stdev) {
        this.mean = mean;
        this.stdev = stdev;
        this.random = new Random();
    }

    public double sample() {
        return random.nextGaussian() * stdev + mean;
    }
}