package uk.davidwei.perfmock.internal;

import org.hamcrest.SelfDescribing;

public interface OrderingConstraint extends SelfDescribing {
    boolean allowsInvocationNow();
}
