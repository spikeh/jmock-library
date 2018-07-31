package uk.davidwei.perfmock.test.unit.lib;

import junit.framework.TestCase;

import uk.davidwei.perfmock.api.ExpectationError;
import uk.davidwei.perfmock.lib.IdentityExpectationErrorTranslator;


public class IdentityExpectationErrorTranslatorTests extends TestCase{
    public void testReturnsTheErrorAsItsOwnTranslation() {
        ExpectationError e = ExpectationError.unexpected(null, null);
        
        assertSame(e, IdentityExpectationErrorTranslator.INSTANCE.translate(e));
    }
}
