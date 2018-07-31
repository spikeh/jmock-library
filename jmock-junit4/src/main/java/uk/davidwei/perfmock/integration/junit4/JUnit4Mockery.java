package uk.davidwei.perfmock.integration.junit4;

import uk.davidwei.perfmock.Mockery;
import uk.davidwei.perfmock.lib.AssertionErrorTranslator;

/**
 * A {@link Mockery} that reports expectation errors as JUnit 4 test failures.
 *  
 * @author nat
 */
public class JUnit4Mockery extends Mockery {
    public JUnit4Mockery() {
        setExpectationErrorTranslator(AssertionErrorTranslator.INSTANCE);
    }
}
