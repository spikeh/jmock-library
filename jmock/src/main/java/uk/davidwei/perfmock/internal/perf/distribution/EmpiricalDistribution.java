package uk.davidwei.perfmock.internal.perf.distribution;

import org.apache.commons.math3.distribution.RealDistribution;

import java.util.Arrays;
import java.util.Random;

public abstract class EmpiricalDistribution implements Distribution {

    private final double[] mids;
    private final double[] cdf;
    private final Random rng = new Random();

    EmpiricalDistribution(double[] mids, double[] cdf) {
        this.mids = mids;
        this.cdf = cdf;
    }

    public double sample() {
        double rn = rng.nextDouble();
        int pos = Arrays.binarySearch(cdf, rn);
        if (pos < 0)
            pos = ~pos;
        return mids[pos];
    }

    public RealDistribution getDistribution() {
        return null;
    }
}