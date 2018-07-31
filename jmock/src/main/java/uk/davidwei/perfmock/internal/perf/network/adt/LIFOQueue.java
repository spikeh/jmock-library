package uk.davidwei.perfmock.internal.perf.network.adt;

import uk.davidwei.perfmock.internal.perf.network.request.Customer;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LIFOQueue<T extends Customer> extends AbstractQueue<T> implements CappedQueue<T> {
    private final Deque<T> q;
    private int cap;

    public LIFOQueue() {
        this.q = new ConcurrentLinkedDeque<>();
        this.cap = Integer.MAX_VALUE;
    }

    public LIFOQueue(int cap) {
        this.q = new LinkedList<>();
        this.cap = cap;
    }

    public boolean canAccept(T c) {
        return q.size() < cap;
    }

    public boolean add(T c) {
        if (canAccept(c)) {
            q.addFirst(c);
            return true;
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean offer(T c) {
        return canAccept(c) && q.offerFirst(c);
    }

    public T remove() {
        return q.removeFirst();
    }

    public T poll() {
        return q.pollFirst();
    }

    public T element() {
        return q.getFirst();
    }

    public T peek() {
        return q.peekFirst();
    }

    public void clear() {
        q.clear();
    }

    public boolean contains(Object o) {
        return q.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return q.containsAll(c);
    }

    public boolean isEmpty() {
        return q.isEmpty();
    }

    public Iterator<T> iterator() {
        return q.iterator();
    }

    public boolean remove(Object o) {
        return q.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return q.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return q.retainAll(c);
    }

    public int size() {
        return q.size();
    }

    public Object[] toArray() {
        return q.toArray();
    }

    public String toString() {
        return q.toString();
    }
}