package uk.davidwei.perfmock.lib;

import uk.davidwei.perfmock.api.ExpectationError;
import uk.davidwei.perfmock.api.ExpectationErrorTranslator;

/**
 * An {@link ExpectationErrorTranslator} that doesn't do any translation.  
 * It returns the {@link ExpectationError} it is given.
 * 
 * @author nat
 * 
 */
public class IdentityExpectationErrorTranslator implements
    ExpectationErrorTranslator
{
    public static final IdentityExpectationErrorTranslator INSTANCE = new IdentityExpectationErrorTranslator();
    
    private IdentityExpectationErrorTranslator() {}
    
    public Error translate(ExpectationError e) {
        return e;
    }
}
