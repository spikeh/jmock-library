package uk.davidwei.perfmock;

import uk.davidwei.perfmock.internal.InvocationExpectation;

/**
 * A sequence of expectations; invocations can be constrained to occur in a strict 
 * order defined by a sequence.
 * 
 * @author nat
 */
public interface Sequence {
    void constrainAsNextInSequence(InvocationExpectation expectation);
}
