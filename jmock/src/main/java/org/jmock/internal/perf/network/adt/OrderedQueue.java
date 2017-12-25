package org.jmock.internal.perf.network.adt;

import org.jmock.internal.perf.network.request.Customer;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

import static java.util.Comparator.comparingDouble;

public class OrderedQueue<T extends Customer> extends PriorityBlockingQueue<T> implements CappedQueue<T> {
    private int cap;

    public OrderedQueue() {
        this(99999);
        this.cap = Integer.MAX_VALUE;
    }

    public OrderedQueue(int cap) {
        super(cap, new Comparator<T>() {
            @Override
            public int compare(T t1, T t2) {
                return Double.compare(t1.aTime(), t2.aTime());
            }
        });
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