package org.jmock.integration.junit4;

import org.jmock.internal.perf.network.adt.CappedQueue;
import org.jmock.internal.perf.network.adt.FIFOQueue;
import org.jmock.internal.perf.network.adt.LIFOQueue;
import org.jmock.internal.perf.network.adt.OrderedQueue;

public class QueueingDisciplines {
    public static CappedQueue fifo() {
        return new FIFOQueue();
    }

    public static CappedQueue fifo(int size) {
        return new FIFOQueue(size);
    }

    public static CappedQueue lifo() {
        return new LIFOQueue();
    }

    public static CappedQueue lifo(int size) {
        return new LIFOQueue(size);
    }

    public static CappedQueue priorityTime() {
        return new OrderedQueue();
    }

    public static CappedQueue priorityTime(int size) {
        return new OrderedQueue(size);
    }
}