package uk.davidwei.perfmock.internal.perf.distribution;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

public class Uniform implements Distribution {
    private final RealDistribution uniform;

    public Uniform(double lower, double upper) {
        uniform = new UniformRealDistribution(lower, upper);
    }

    public double sample() {
        return uniform.sample();
    }

    @Override
    public RealDistribution getDistribution() {
        return uniform;
    }
}