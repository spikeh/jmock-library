package uk.davidwei.perfmock.internal.perf;

import uk.davidwei.perfmock.api.Invocation;

public interface PerformanceModel {
    void schedule(long threadId, Invocation invocation, Param param);
}