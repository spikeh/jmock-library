package testdata.jmock.acceptance.junit4;

import uk.davidwei.perfmock.Mockery;
import uk.davidwei.perfmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class JUnit4TestThatDoesNotCreateAMockery {
    Mockery context = null;
    
    @Test
    public void happy() {
        // a-ok!
    }
}
