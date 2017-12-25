package org.jmock.internal.perf.distribution;

public class Exp implements Distribution {
    private final double rate;

    public Exp(double rate) {
        this.rate = rate;
    }

    public static double exp(double rate) {
        return -Math.log(Math.random()) / rate;
    }

    public double sample() {
        return -Math.log(Math.random()) / rate;
    }
}