package uk.davidwei.perfmock.internal;

import uk.davidwei.perfmock.api.Action;
import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.api.Invokable;

public class InvocationToExpectationTranslator implements Invokable {
    private final ExpectationCapture capture;
    private Action defaultAction;
    
    public InvocationToExpectationTranslator(ExpectationCapture capture,
                                             Action defaultAction) 
    {
        this.capture = capture;
        this.defaultAction = defaultAction;
    }
    
    public Object invoke(Invocation invocation) throws Throwable {
        capture.createExpectationFrom(invocation);
        return defaultAction.invoke(invocation);
    }
}
