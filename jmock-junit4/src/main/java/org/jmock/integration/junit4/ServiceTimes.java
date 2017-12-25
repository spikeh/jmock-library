package org.jmock.integration.junit4;

import org.jmock.internal.perf.distribution.*;

public class ServiceTimes {

    public static Distribution exponential(double lambda) {
        return new Exp(lambda);
    }

    public static Distribution constant(double value) {
        return new Deterministic(value);
    }

    public static Distribution normalDist(double mean, double stdev) {
        return new Normal(mean, stdev);
    }

    public static Distribution paretoDist(double scale, double shape) {
        return new Pareto(scale, shape);
    }

    public static Distribution poissonDist(double lambda) {
        return new Poisson(lambda);
    }

    public static Distribution uniformDist(double min, double max) {
        return new Uniform(min, max);
    }
}