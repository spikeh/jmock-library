package org.jmock.internal.perf;

import org.jmock.internal.perf.distribution.Distribution;

public class Delay {
    private final Distribution distribution;

    public Delay(Distribution distribution) {
        this.distribution = distribution;
    }

    public double sample() {
        return distribution.sample();
    }
}