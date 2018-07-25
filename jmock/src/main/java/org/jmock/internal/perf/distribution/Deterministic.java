package org.jmock.internal.perf.distribution;

import org.apache.commons.math3.distribution.RealDistribution;

public class Deterministic implements Distribution {
    private final double value;

    public Deterministic(double v) {
        this.value = v;
    }

    public double sample() {
        return value;
    }

    @Override
    public RealDistribution getDistribution() {
        return null;
    }
}