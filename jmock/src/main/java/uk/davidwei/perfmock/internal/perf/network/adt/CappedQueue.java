package uk.davidwei.perfmock.internal.perf.network.adt;

import uk.davidwei.perfmock.internal.perf.network.request.Customer;

import java.util.Queue;

public interface CappedQueue<T extends Customer> extends Queue<T> {
    boolean canAccept(T c);
}