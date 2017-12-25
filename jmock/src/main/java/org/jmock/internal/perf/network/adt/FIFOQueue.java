package org.jmock.internal.perf.network.adt;

import org.jmock.internal.perf.network.request.Customer;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FIFOQueue<T extends Customer> extends ConcurrentLinkedQueue<T> implements CappedQueue<T> {
    private int cap;

    public FIFOQueue() {
        this.cap = Integer.MAX_VALUE;
    }

    public FIFOQueue(int cap) {
        this.cap = cap;
    }

    public boolean canAccept(T c) {
        return size() < cap;
    }

    public boolean add(T c) {
        if (canAccept(c)) {
            return super.add(c);
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean offer(T c) {
        return canAccept(c) && super.offer(c);
    }
}