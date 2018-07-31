package uk.davidwei.perfmock.lib;

import org.hamcrest.StringDescription;
import uk.davidwei.perfmock.api.ExpectationError;
import uk.davidwei.perfmock.api.ExpectationErrorTranslator;

/**
 * Translates {@link uk.davidwei.perfmock.api.ExpectationError}s into
 * {@link java.lang.AssertionError}s that several
 * test frameworks, including JUnit 4 and TestNG, use to report
 * errors.
 * 
 * @author npryce
 *
 */
public class AssertionErrorTranslator implements ExpectationErrorTranslator {
    public static final AssertionErrorTranslator INSTANCE = new AssertionErrorTranslator();
    
    public Error translate(ExpectationError e) {
        return new AssertionError(StringDescription.toString(e));
    }
    
    private AssertionErrorTranslator() {}
}
