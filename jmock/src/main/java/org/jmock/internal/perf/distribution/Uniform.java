package org.jmock.internal.perf.distribution;

public class Uniform implements Distribution {
    private final double lower;
    private final double upper;

    public Uniform() {
        this(0, 1);
    }

    public Uniform(double lower, double upper) {
        // FIXME: Error if lower >= upper
        this.lower = lower;
        this.upper = upper;
    }

    public double sample() {
        double u = Math.random();
        return u * upper + (1 - u) * lower;
    }
}