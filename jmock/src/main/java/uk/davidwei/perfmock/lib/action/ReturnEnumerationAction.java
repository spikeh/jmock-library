package uk.davidwei.perfmock.lib.action;

import org.hamcrest.Description;
import uk.davidwei.perfmock.api.Action;
import uk.davidwei.perfmock.api.Invocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

import static java.util.Collections.enumeration;

/**
 * Returns an {@link Enumeration} over a collection.
 * 
 * @author nat
 */
public class ReturnEnumerationAction implements Action {
    private Collection<?> collection;
    
    public ReturnEnumerationAction(Collection<?> collection) {
        this.collection = collection;
    }
    
    public ReturnEnumerationAction(Object... array) {
        this.collection = Arrays.asList(array);
    }
    
    public Enumeration<?> invoke(Invocation invocation) throws Throwable {
        return enumeration(collection);
    }
    
    public void describeTo(Description description) {
        description.appendValueList("return enumeration over ", ", ", "", collection);
    }
}
