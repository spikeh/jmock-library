package org.jmock.internal.perf;

public abstract class Event<T> {
    protected final double invokeTime;
    protected final T eventObject;

    public Event(double time, T eventObject) {
        this.invokeTime = time;
        this.eventObject = eventObject;
    }

    public double invokeTime() {
        return invokeTime;
    }

    public T getEventObject() {
        return eventObject;
    }

    public abstract boolean invoke();
}