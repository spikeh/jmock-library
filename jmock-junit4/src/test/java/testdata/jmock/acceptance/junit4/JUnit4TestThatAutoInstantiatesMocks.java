package testdata.jmock.acceptance.junit4;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import uk.davidwei.perfmock.Sequence;
import uk.davidwei.perfmock.States;
import uk.davidwei.perfmock.auto.Auto;
import uk.davidwei.perfmock.auto.Mock;
import org.junit.Test;

public class JUnit4TestThatAutoInstantiatesMocks extends BaseClassWithMockery {
    @Mock Runnable runnable;
    @Auto States states;
    @Auto Sequence sequence;
    
    @Test
    public void fieldsHaveBeenAutoInstantiated() {
        assertThat(runnable, notNullValue());
        assertThat(states, notNullValue());
        assertThat(sequence, notNullValue());
    }
}
