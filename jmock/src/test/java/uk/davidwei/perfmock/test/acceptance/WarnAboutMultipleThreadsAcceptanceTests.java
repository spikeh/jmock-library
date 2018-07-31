package uk.davidwei.perfmock.test.acceptance;

import junit.framework.TestCase;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import uk.davidwei.perfmock.Expectations;
import uk.davidwei.perfmock.Mockery;
import uk.davidwei.perfmock.lib.concurrent.Blitzer;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import static org.junit.Assert.assertThat;

@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
@Ignore
public class WarnAboutMultipleThreadsAcceptanceTests extends TestCase {
    BlockingQueue<Throwable> exceptionsOnBackgroundThreads = new LinkedBlockingQueue<Throwable>();

    private ThreadFactory exceptionCapturingThreadFactory = new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    try {
                        exceptionsOnBackgroundThreads.put(e);
                    } catch (InterruptedException e1) {
                        throw new ThreadDeath();
                    }
                }
            });
            return t;
        }
    };

    Blitzer blitzer = new Blitzer(1, Executors.newFixedThreadPool(1, exceptionCapturingThreadFactory));

    public void testKillsThreadsThatTryToCallMockeryThatIsNotThreadSafe() throws InterruptedException {
        Mockery mockery = new Mockery();
        
        final MockedType mock = mockery.mock(MockedType.class, "mock");
        
        mockery.checking(new Expectations() {{
            allowing (mock).doSomething();
        }});
        
        blitzer.blitz(new Runnable() {
            public void run() {
                mock.doSomething();
            }            
        });

        Throwable exception = exceptionsOnBackgroundThreads.take();
        assertThat(exception.getMessage(), Matchers.containsString("the Mockery is not thread-safe"));
    }
    
    @Override
    public void tearDown() {
        blitzer.shutdown();
    }
}
