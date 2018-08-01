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

    private int searchInsert(double target) {
        int i = 0;
        int j = cdf.length - 1;
        while (i <= j) {
            int mid = (i + j) / 2;
            if (target > cdf[mid])
                i = mid + 1;
            else if (target < cdf[mid])
                j = mid - 1;
            else
                return mid;
        }
        return i;
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