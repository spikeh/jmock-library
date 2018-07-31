package uk.davidwei.perfmock.internal.perf.distribution;

import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

public class Pareto implements Distribution {
    private final RealDistribution pareto;

    public Pareto(double scale, double shape) {
        pareto = new ParetoDistribution(scale, shape);
    }

    public double sample() {
        return pareto.sample();
    }

    @Override
    public RealDistribution getDistribution() {
        return pareto;
    }
}