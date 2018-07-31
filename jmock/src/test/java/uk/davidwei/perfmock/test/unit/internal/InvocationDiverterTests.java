package uk.davidwei.perfmock.test.unit.internal;

import junit.framework.TestCase;

import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.internal.InvocationDiverter;
import uk.davidwei.perfmock.test.unit.support.StubInvokable;

public class InvocationDiverterTests extends TestCase {
    public interface TargetInterface {
        void doSomething();
    }
    
    public class Target implements TargetInterface {
        public boolean wasInvoked = false;
        
        public void doSomething() {
            wasInvoked = true;
        }
    }
    
    public interface OtherInterface {
        void doSomethingElse();
    }
    
    Target target = new Target();
    StubInvokable next = new StubInvokable();
    
    InvocationDiverter<TargetInterface> diverter =
        new InvocationDiverter<TargetInterface>(TargetInterface.class, target, next);

    
    public void testAppliesInvocationToGivenObjectIfInvokedMethodDeclaredInGivenClass() throws Throwable {
        Invocation invocation = 
            new Invocation("invokedObject", 
                           TargetInterface.class.getMethod("doSomething"), 
                           Invocation.NO_PARAMETERS);
        
        diverter.invoke(invocation);
        
        assertTrue("target should have been invoked", 
                   target.wasInvoked);
        assertTrue("next should not have been invoked",
                   !next.wasInvoked);
    }
    
    public void testPassesInvocationToNextIfInvocationNotDeclaredInGivenClass() throws Throwable {
        Invocation invocation = 
            new Invocation("invokedObject", 
                           OtherInterface.class.getMethod("doSomethingElse"), 
                           Invocation.NO_PARAMETERS);
        
        diverter.invoke(invocation);
        
        assertTrue("target should not have been invoked", 
                   !target.wasInvoked);
        assertTrue("next should have been invoked",
                   next.wasInvoked);
    }
    
    public void testDelegatesToStringToNext() {
        next.toStringResult = "next.toStringResult";
        
        assertEquals(next.toStringResult, diverter.toString());
    }
}
