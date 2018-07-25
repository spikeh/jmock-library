package org.jmock.internal.perf.distribution;

import org.apache.commons.math3.distribution.RealDistribution;

public interface Distribution {
    double sample();

    RealDistribution getDistribution();
}