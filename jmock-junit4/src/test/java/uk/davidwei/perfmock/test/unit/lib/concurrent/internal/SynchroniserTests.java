package uk.davidwei.perfmock.test.unit.lib.concurrent.internal;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;

import uk.davidwei.perfmock.Expectations;
import uk.davidwei.perfmock.Mockery;
import uk.davidwei.perfmock.States;
import uk.davidwei.perfmock.integration.junit4.JUnit4Mockery;
import uk.davidwei.perfmock.lib.concurrent.Blitzer;
import uk.davidwei.perfmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SynchroniserTests {
    public interface Events {
        void action();
        void finished();
    }
    
    Synchroniser synchroniser = new Synchroniser();
    
    Mockery mockery = new JUnit4Mockery() {{
        setThreadingPolicy(synchroniser);
    }};
    
    Blitzer blitzer = new Blitzer(16, 4);
    
    Events mockObject = mockery.mock(Events.class, "mockObject");
    
    @Test(timeout=250)
    public void allowsMultipleThreadsToCallMockObjects() throws InterruptedException {
        mockery.checking(new Expectations() {{
            exactly(blitzer.totalActionCount()).of(mockObject).action();
        }});
        
        blitzer.blitz(new Runnable() {
            public void run() {
                mockObject.action();
            }
        });
        
        mockery.assertIsSatisfied();
    }
    
    @Test(timeout=250)
    public void canWaitForAStateMachineToEnterAGivenState() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(blitzer.totalActionCount());
        
        final States threads = mockery.states("threads");
        
        mockery.checking(new Expectations() {{
            exactly(blitzer.totalActionCount()).of(mockObject).action();
                when(threads.isNot("finished"));
                
            oneOf(mockObject).finished();
                then(threads.is("finished"));
        }});
        
        blitzer.blitz(new Runnable() {
            public void run() {
                mockObject.action();
                if (counter.decrementAndGet() == 0) {
                    mockObject.finished();
                }
            }
        });
        
        synchroniser.waitUntil(threads.is("finished"));
    }

    @Test(timeout=250)
    public void canWaitForAStateMachineToEnterAGivenStateWithinSomeTimeout() throws InterruptedException {
        final States threads = mockery.states("threads");
        
        mockery.checking(new Expectations() {{
            exactly(blitzer.totalActionCount()).of(mockObject).action();
                when(threads.isNot("finished"));
                
            oneOf(mockObject).finished();
                then(threads.is("finished"));
        }});
        
        blitzer.blitz(new Runnable() {
            AtomicInteger counter = new AtomicInteger(blitzer.totalActionCount());
            
            public void run() {
                mockObject.action();
                if (counter.decrementAndGet() == 0) {
                    mockObject.finished();
                }
            }
        });
        
        synchroniser.waitUntil(threads.is("finished"), 100);
    }

    @Test(timeout=250)
    public void failsTheTestIfStateMachineDoesNotEnterExpectedStateWithinTimeout() throws InterruptedException {
        States threads = mockery.states("threads");
        
        try {
            synchroniser.waitUntil(threads.is("finished"), 100);
        }
        catch (AssertionError e) {
            return;
        }
        
        fail("should have thrown AssertionError");
    }
    
    @Test
    public void throwsExpectationErrorIfExpectationFailsWhileWaitingForStateMachine() throws InterruptedException {
        final States threads = mockery.states("threads");
        
        // This will cause an expectation error, and nothing will make
        // the "threads" state machine transition to "finished" 
        
        blitzer.blitz(new Runnable() {
            public void run() {
                mockObject.action();
            }
        });
        
        try {
            synchroniser.waitUntil(threads.is("finished"), 100);
            fail("should have thrown AssertionError");
        }
        catch (AssertionError e) {
            assertThat(e.getMessage(), containsString("action()"));
        }
    }

    @Test
    public void throwsExpectationErrorIfExpectationFailsWhileWaitingForStateMachineEvenIfWaitSucceeds() throws InterruptedException {
        final States threads = mockery.states("threads");
        
        mockery.checking(new Expectations() {{
            oneOf(mockObject).finished();
                then(threads.is("finished"));
        }});
        
        blitzer.blitz(new Runnable() {
            AtomicInteger counter = new AtomicInteger(blitzer.totalActionCount());
            
            public void run() {
                if (counter.decrementAndGet() == 0) {
                    mockObject.finished();
                }
                else {
                    mockObject.action();
                }
            }
        });
        
        try {
            synchroniser.waitUntil(threads.is("finished"), 100);
            fail("should have thrown AssertionError");
        }
        catch (AssertionError e) {
            assertThat(e.getMessage(), containsString("action()"));
        }
    }
    
    @After
    public void cleanUp() {
        blitzer.shutdown();
    }
}
