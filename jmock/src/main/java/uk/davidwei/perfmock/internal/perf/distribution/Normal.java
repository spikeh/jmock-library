package uk.davidwei.perfmock.internal.perf.distribution;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

public class Normal implements Distribution {
    private final RealDistribution normal;

    public Normal(double mean, double stdev) {
        normal = new NormalDistribution(mean, stdev);
    }

    public double sample() {
        return normal.sample();
    }

    @Override
    public RealDistribution getDistribution() {
        return normal;
    }
}