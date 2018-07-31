package uk.davidwei.perfmock.integration.junit4;

import uk.davidwei.perfmock.internal.perf.network.adt.CappedQueue;
import uk.davidwei.perfmock.internal.perf.network.adt.FIFOQueue;
import uk.davidwei.perfmock.internal.perf.network.adt.LIFOQueue;
import uk.davidwei.perfmock.internal.perf.network.adt.OrderedQueue;

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