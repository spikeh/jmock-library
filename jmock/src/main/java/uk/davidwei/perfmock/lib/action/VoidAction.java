/*  Copyright (c) 2000-2004 jMock.org
 */
package uk.davidwei.perfmock.lib.action;

import org.hamcrest.Description;
import uk.davidwei.perfmock.api.Action;
import uk.davidwei.perfmock.api.Invocation;

/**
 * Returns nothing from a void method.
 */
public class VoidAction implements Action {
    public static final VoidAction INSTANCE = new VoidAction();

    public Object invoke(Invocation invocation) throws Throwable {
        return null;
    }

    public void describeTo(Description description) {
        description.appendText("is void");
    }
}
