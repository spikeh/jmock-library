package uk.davidwei.perfmock.test.unit.internal;

import uk.davidwei.perfmock.internal.Formatting;

import junit.framework.TestCase;


public class FormattingTests extends TestCase {
    public void testFormatsTimesAsSingularAndPlural() {
        assertEquals("0 times", Formatting.times(0));
        assertEquals("1 time", Formatting.times(1));
        assertEquals("2 times", Formatting.times(2));
        assertEquals("99 times", Formatting.times(99));
    }
}
