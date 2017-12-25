package org.jmock.internal.perf.network.adt;

import org.jmock.internal.perf.network.request.Customer;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import static java.util.Comparator.comparingDouble;

public class ShortestJobFirstQueue<T extends Customer> extends PriorityQueue<T> implements Queue<T> {
    public ShortestJobFirstQueue() {
        super(new Comparator<T>() {
            @Override
            public int compare(T t1, T t2) {
                return Double.compare(t1.serviceDemand(), t2.serviceDemand());
            }
        });
    }
}