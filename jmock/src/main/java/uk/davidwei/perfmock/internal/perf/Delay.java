package uk.davidwei.perfmock.internal.perf;

import org.apache.commons.math3.distribution.RealDistribution;
import uk.davidwei.perfmock.internal.perf.distribution.Distribution;

public class Delay {
    private final Distribution distribution;
    private final RealDistribution realdistribution;

    public Delay(Distribution distribution) {
        this.distribution = distribution;
        this.realdistribution = null;
    }

    public Delay(RealDistribution distribution) {
        this.distribution = null;
        this.realdistribution = distribution;
    }

    public double sample() {
        assert(distribution != null || realdistribution != null);
        if (distribution != null) {
            return distribution.sample();
        } else {
            return realdistribution.sample();
        }
    }
}