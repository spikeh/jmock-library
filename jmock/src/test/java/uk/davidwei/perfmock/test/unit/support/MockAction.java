package uk.davidwei.perfmock.test.unit.support;

import org.hamcrest.Description;
import uk.davidwei.perfmock.api.Action;
import uk.davidwei.perfmock.api.Invocation;
import org.junit.Assert;

public class MockAction extends Assert implements Action {
	public boolean expectInvoke = true;
	public Invocation expectedInvocation = null;
	public Object result = null;
	public Throwable exception = null;
	public MockAction previous = null;
	public boolean wasInvoked = false;
	
	public String descriptionText;
	
	public MockAction() {
		descriptionText = this.toString();
	}
	
	public Object invoke(Invocation actualInvocation) throws Throwable {
		assertTrue("should not be invoked", expectInvoke);
		
		if (expectedInvocation != null) {
			assertSame("invocation", expectedInvocation, actualInvocation);
		}
		
		if (previous != null) {
			assertTrue("invoked out of order", previous.wasInvoked);
		}
		
		wasInvoked = true;
		
		if (exception != null) {
			throw exception;
		}
		else {
			return result;
		}
	}

	public void describeTo(Description description) {
		description.appendText(descriptionText);
	}
}
