package testdata.jmock.acceptance.junit4;

import uk.davidwei.perfmock.Mockery;
import uk.davidwei.perfmock.integration.junit4.JMock;
import uk.davidwei.perfmock.integration.junit4.JUnit4Mockery;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class BaseClassWithMockery {
    protected Mockery context = new JUnit4Mockery();
}
