package uk.davidwei.perfmock.internal;

import uk.davidwei.perfmock.api.Invocation;


public interface ExpectationCapture {
    void createExpectationFrom(Invocation invocation);
}
