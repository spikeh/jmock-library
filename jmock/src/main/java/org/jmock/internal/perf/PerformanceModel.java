package org.jmock.internal.perf;

import org.jmock.api.Invocation;

public interface PerformanceModel {
    void query(long threadId, Invocation invocation, Param param);
}