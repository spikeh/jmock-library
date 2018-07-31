/*  Copyright (c) 2000-2004 jMock.org
 */
package uk.davidwei.perfmock.lib.action;

import org.hamcrest.Description;
import uk.davidwei.perfmock.api.Action;
import uk.davidwei.perfmock.api.Invocation;

/**
 * Returns a value.
 * 
 * @author nat
 *
 */
public class ReturnValueAction implements Action {
    private Object result;

    public ReturnValueAction(Object result) {
        this.result = result;
    }

    public Object invoke(Invocation invocation) throws Throwable {
        return result;
    }

    public void describeTo(Description description) {
        description.appendText("returns ");
        description.appendValue(result);
    }
}
