/*  Copyright (c) 2000-2004 jMock.org
 */
package uk.davidwei.perfmock.api;

import org.hamcrest.SelfDescribing;
import uk.davidwei.perfmock.internal.InvocationExpectation;


/**
 * An object that fakes the behaviour of an {@link InvocationExpectation}.
 */
public interface Action extends SelfDescribing, Invokable {
}
