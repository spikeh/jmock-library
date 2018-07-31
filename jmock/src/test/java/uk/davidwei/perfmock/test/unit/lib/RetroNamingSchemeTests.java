package uk.davidwei.perfmock.test.unit.lib;

import junit.framework.TestCase;

import uk.davidwei.perfmock.api.MockObjectNamingScheme;
import uk.davidwei.perfmock.lib.RetroNamingScheme;
import uk.davidwei.perfmock.test.unit.support.DummyInterface;

public class RetroNamingSchemeTests extends TestCase {
    public void testNamesMocksByLowerCasingFirstCharacterOfTypeName() {
        MockObjectNamingScheme namingScheme = RetroNamingScheme.INSTANCE;
        
        assertEquals("mockRunnable", namingScheme.defaultNameFor(Runnable.class));
        assertEquals("mockDummyInterface", namingScheme.defaultNameFor(DummyInterface.class));
    }
}
