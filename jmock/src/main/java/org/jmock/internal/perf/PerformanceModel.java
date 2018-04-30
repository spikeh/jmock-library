package org.jmock.internal.perf;

import org.jmock.api.Invocation;

public interface PerformanceModel {
    void schedule(long threadId, Invocation invocation, Param param);
}