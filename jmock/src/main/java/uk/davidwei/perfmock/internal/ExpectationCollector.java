package uk.davidwei.perfmock.internal;

import uk.davidwei.perfmock.api.Expectation;

public interface ExpectationCollector {
    void add(Expectation expectation);
}
