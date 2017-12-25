package org.jmock.internal.perf.network.adt;

import org.jmock.internal.perf.network.request.Customer;

import java.util.Queue;

public interface CappedQueue<T extends Customer> extends Queue<T> {
    boolean canAccept(T c);
}