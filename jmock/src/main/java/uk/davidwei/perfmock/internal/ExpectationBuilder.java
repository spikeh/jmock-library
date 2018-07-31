package uk.davidwei.perfmock.internal;

import uk.davidwei.perfmock.api.Action;

public interface ExpectationBuilder {
    void buildExpectations(Action defaultAction, ExpectationCollector collector);
}
