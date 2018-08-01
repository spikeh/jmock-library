package uk.davidwei.perfmock.internal;

import org.hamcrest.Matcher;
import uk.davidwei.perfmock.Sequence;
import uk.davidwei.perfmock.api.Action;
import uk.davidwei.perfmock.api.Expectation;
import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.internal.matcher.AllParametersMatcher;
import uk.davidwei.perfmock.internal.matcher.MethodNameMatcher;
import uk.davidwei.perfmock.internal.matcher.MockObjectMatcher;
import uk.davidwei.perfmock.internal.perf.Param;
import uk.davidwei.perfmock.internal.perf.PerformanceModel;
import uk.davidwei.perfmock.syntax.MethodClause;
import uk.davidwei.perfmock.syntax.ParametersClause;
import uk.davidwei.perfmock.syntax.ReceiverClause;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvocationExpectationBuilder 
    implements ExpectationCapture, 
               ReceiverClause, MethodClause, ParametersClause
{
    private final InvocationExpectation expectation = new InvocationExpectation();
    
    private boolean isFullySpecified = false;
    private boolean needsDefaultAction = true;
    private List<Matcher<?>> capturedParameterMatchers = new ArrayList<Matcher<?>>();
    
    public Expectation toExpectation(Action defaultAction) {
        if (needsDefaultAction) {
            expectation.setDefaultAction(defaultAction);
        }
        
        return expectation;
    }
    
    public void setCardinality(Cardinality cardinality) {
        expectation.setCardinality(cardinality);
    }
    
    public void addParameterMatcher(Matcher<?> matcher) {
        capturedParameterMatchers.add(matcher);
    }
    
    public void addOrderingConstraint(OrderingConstraint constraint) {
        expectation.addOrderingConstraint(constraint);
    }
    
    public void addInSequenceOrderingConstraint(Sequence sequence) {
        sequence.constrainAsNextInSequence(expectation);
    }
    
    public void setAction(Action action) {
        expectation.setAction(action);
        needsDefaultAction = false;
    }

    public void setParam(Param param) {
        expectation.setParam(param);
    }

    public void setModel(PerformanceModel model) {
        expectation.setPerformanceModel(model);
    }
    
    public void addSideEffect(SideEffect sideEffect) {
        expectation.addSideEffect(sideEffect);
    }
    
    private <T> T captureExpectedObject(T mockObject) {
        if (!(mockObject instanceof CaptureControl)) {
            throw new IllegalArgumentException("can only set expectations on mock objects");
        }
        
        expectation.setObjectMatcher(new MockObjectMatcher(mockObject));
        isFullySpecified = true;
        
        Object capturingImposter = ((CaptureControl)mockObject).captureExpectationTo(this);
        
        return asMockedType(mockObject, capturingImposter);
    }
    
    // Damn you Java generics! Damn you to HELL!
    @SuppressWarnings("unchecked")
    private <T> T asMockedType(@SuppressWarnings("unused") T mockObject, 
                               Object capturingImposter) 
    {
        return (T) capturingImposter;
    }
    
    public void createExpectationFrom(Invocation invocation) {
        expectation.setMethod(invocation.getInvokedMethod());
        
        if (capturedParameterMatchers.isEmpty()) {
            expectation.setParametersMatcher(new AllParametersMatcher(invocation.getParametersAsArray()));
        }
        else {
            checkParameterMatcherCount(invocation);
            expectation.setParametersMatcher(new AllParametersMatcher(capturedParameterMatchers));
        }
    }
    
    private void checkParameterMatcherCount(Invocation invocation) {
        if (capturedParameterMatchers.size() != invocation.getParameterCount()) {
            throw new IllegalArgumentException("not all parameters were given explicit matchers: either all parameters must be specified by matchers or all must be specified by values, you cannot mix matchers and values");
        }
    }
    
    public void checkWasFullySpecified() {
        if (!isFullySpecified) {
            throw new IllegalStateException("expectation was not fully specified");
        }
    }

    /* 
     * Syntactic sugar
     */
    
    public <T> T of(T mockObject) {
        return captureExpectedObject(mockObject);
    }

    public MethodClause of(Matcher<?> objectMatcher) {
        expectation.setObjectMatcher(objectMatcher);
        isFullySpecified = true;
        return this;
    }

    public ParametersClause method(Matcher<Method> methodMatcher) {
        expectation.setMethodMatcher(methodMatcher);
        return this;
    }
    
    public ParametersClause method(String nameRegex) {
        return method(new MethodNameMatcher(nameRegex));
    }
    
    public void with(Matcher<?>... parameterMatchers) {
        expectation.setParametersMatcher(new AllParametersMatcher(Arrays.asList(parameterMatchers)));
    }
    
    public void withNoArguments() {
        with();
    }
}
