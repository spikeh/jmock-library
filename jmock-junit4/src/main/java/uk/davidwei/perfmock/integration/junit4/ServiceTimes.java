package uk.davidwei.perfmock.integration.junit4;

import uk.davidwei.perfmock.internal.perf.Delay;
import uk.davidwei.perfmock.internal.perf.PerformanceModel;
import uk.davidwei.perfmock.internal.perf.distribution.*;
import uk.davidwei.perfmock.internal.perf.network.ISNetwork;

import java.io.IOException;

public class ServiceTimes {

    public static PerformanceModel exponentialDist(double mean) {
        return new ISNetwork(PerformanceMockery.INSTANCE.sim(), new Delay(new Exp(mean)));
    }

    public static PerformanceModel constant(double value) {
        return new ISNetwork(PerformanceMockery.INSTANCE.sim(), new Delay(new Deterministic(value)));
    }

    public static PerformanceModel normalDist(double mean, double stdev) {
        return new ISNetwork(PerformanceMockery.INSTANCE.sim(), new Delay(new Normal(mean, stdev)));
    }

    public static PerformanceModel paretoDist(double scale, double shape) {
        return new ISNetwork(PerformanceMockery.INSTANCE.sim(), new Delay(new Pareto(scale, shape)));
    }

    public static PerformanceModel uniformDist(double min, double max) {
        return new ISNetwork(PerformanceMockery.INSTANCE.sim(), new Delay(new Uniform(min, max)));
    }

    public static PerformanceModel alias(String filePath) throws IOException {
        return new ISNetwork(PerformanceMockery.INSTANCE.sim(), new Delay(new Alias(filePath)));
    }
}