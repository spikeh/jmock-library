package org.jmock.internal.perf.distribution;

public class Poisson implements Distribution {
    private final double lambda;

    public Poisson(double lambda) {
        // FIXME: Error if lambda >= 40.0
        this.lambda = lambda;
    }

    public double sample() {
        double p = Math.exp(-lambda);
        long n = 0;
        double r = 1.0d;
        double rnd;

        while (n < 1000 * lambda) {
            rnd = Math.random();
            r *= rnd;
            if (r >= p) {
                n++;
            } else {
                return n;
            }
        }
        return n;
    }
}