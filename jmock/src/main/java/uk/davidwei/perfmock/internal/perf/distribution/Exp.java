package uk.davidwei.perfmock.internal.perf.distribution;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

public class Exp implements Distribution {
    private final RealDistribution exp;

    public Exp(double rate) {
        exp = new ExponentialDistribution(rate);
    }

    public static double exp(double rate) {
        return -Math.log(Math.random()) / rate;
    }

    public double sample() {
        return exp.sample();
    }

    @Override
    public RealDistribution getDistribution() {
        return exp;
    }
}