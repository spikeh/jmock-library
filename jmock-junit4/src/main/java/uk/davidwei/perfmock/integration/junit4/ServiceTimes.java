package uk.davidwei.perfmock.integration.junit4;

import uk.davidwei.perfmock.internal.perf.distribution.*;

import java.io.IOException;

public class ServiceTimes {

    public static Distribution exponentialDist(double mean) {
        return new Exp(mean);
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

    public static Distribution uniformDist(double min, double max) {
        return new Uniform(min, max);
    }

    public static Distribution alias(String filePath) throws IOException {
        return new Alias(filePath);
    }
}