package uk.davidwei.perfmock.test.unit.lib.action;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

import junit.framework.TestCase;

import org.hamcrest.StringDescription;
import uk.davidwei.perfmock.api.Action;
import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.lib.action.ReturnEnumerationAction;

public class ReturnEnumerationActionTests extends TestCase {
    private static final Object[] resultElements = {"0", "1", "2", "3"};
    
    public void testReturnsIteratorOverContentsOfCollection() throws Throwable {
        Collection<Object> collection = collectionOf(resultElements);
        ReturnEnumerationAction action = new ReturnEnumerationAction(collection);
        
        assertEnumerationOverSequence(action.invoke(ANY_INVOCATION), resultElements);
    }
    
    public void testReturnsNewIteratorOnEachInvocation() throws Throwable {
        Collection<?> collection = collectionOf(resultElements);
        ReturnEnumerationAction action = new ReturnEnumerationAction(collection);
        
        assertEnumerationOverSequence(action.invoke(ANY_INVOCATION), resultElements);
        assertEnumerationOverSequence(action.invoke(ANY_INVOCATION), resultElements);
        assertEnumerationOverSequence(action.invoke(ANY_INVOCATION), resultElements);
    }
    
    public void testCanReturnIteratorOverArray() throws Throwable {
        ReturnEnumerationAction action = new ReturnEnumerationAction(resultElements);
        
        assertEnumerationOverSequence(action.invoke(ANY_INVOCATION), resultElements);
    }
    
    public void testHasAReadableDescription() {
        Action action = new ReturnEnumerationAction(resultElements);
        
        assertEquals("return enumeration over \"0\", \"1\", \"2\", \"3\"", 
                	 StringDescription.toString(action));
    }
    
    private <T> Collection<T> collectionOf(T... values) {
        return Arrays.asList(values);
    }
    
    private <T> void assertEnumerationOverSequence(Enumeration<?> e, T[] values) {
        for (int i = 0; i < values.length; i++) {
            assertEquals("element " + i,
                    	 values[i], e.nextElement());
        }
    }
    
    private static final Invocation ANY_INVOCATION = null;
}
