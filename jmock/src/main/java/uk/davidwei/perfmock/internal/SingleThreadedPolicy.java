package uk.davidwei.perfmock.internal;

import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.api.Invokable;
import uk.davidwei.perfmock.api.ThreadingPolicy;
import uk.davidwei.perfmock.lib.concurrent.Synchroniser;

import java.util.ConcurrentModificationException;

public class SingleThreadedPolicy implements ThreadingPolicy {
    private final Thread testThread;
    
    public SingleThreadedPolicy() {
        this.testThread = Thread.currentThread();
    }

    public Invokable synchroniseAccessTo(final Invokable mockObject) {
        return new Invokable() {
            public Object invoke(Invocation invocation) throws Throwable {
                checkRunningOnTestThread();
                return mockObject.invoke(invocation);
            }
        };
    }
    
    private void checkRunningOnTestThread() {
        if (Thread.currentThread() != testThread) {
            reportError("the Mockery is not thread-safe: use a " + 
                        Synchroniser.class.getSimpleName() + " to ensure thread safety");
        }
    }
    
    private void reportError(String error) {
        System.err.println(error);
        throw new ConcurrentModificationException(error);
    }
}
