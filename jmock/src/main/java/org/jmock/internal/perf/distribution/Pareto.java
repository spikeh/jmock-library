package org.jmock.internal.perf.distribution;

public class Pareto implements Distribution {
    private final double k;
    private final double alpha;

    public Pareto() {
        this(1, 1);
    }

    public Pareto(double scale, double shape) {
        this.k = scale;
        this.alpha = shape;
    }

    public double sample() {
        double n = Math.random();
        return k / Math.pow(n, 1 / alpha);
    }
}