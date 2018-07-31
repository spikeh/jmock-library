package uk.davidwei.perfmock.api;

public interface ThreadingPolicy {
    Invokable synchroniseAccessTo(Invokable mockObject);
}
