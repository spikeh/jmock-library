package org.jmock.integration.junit4;

import org.hamcrest.Description;
import org.jmock.api.Expectation;
import org.jmock.api.Invocation;
import org.jmock.internal.InvocationDispatcher;
import org.jmock.internal.StateMachine;

import java.util.*;

public class ParallelInvocationDispatcher extends InvocationDispatcher {
    private final Map<Long, Double> threadResponseTimes = Collections.synchronizedMap(new HashMap<Long, Double>());
    private final Map<Long, InvocationDispatcher> dispatcherMap = Collections.synchronizedMap(new HashMap<Long, InvocationDispatcher>());

    private InvocationDispatcher get() {
        long threadId = Thread.currentThread().getId();
        if (!PerformanceMockery.parentThreads.containsKey(threadId) && PerformanceMockery.childToParentMap.containsKey(threadId)) {
            long parentId = PerformanceMockery.childToParentMap.get(threadId);
            assert (dispatcherMap.containsKey(parentId));
            return dispatcherMap.get(parentId);
        }
        InvocationDispatcher dispatcher = dispatcherMap.get(threadId);
        if (dispatcher == null) {
            dispatcher = new InvocationDispatcher();
            dispatcherMap.put(threadId, dispatcher);
            return dispatcher;
        }
        return dispatcher;
    }

    @Override
    public StateMachine newStateMachine(String name) {
        return get().newStateMachine(name);
    }

    @Override
    public void add(Expectation expectation) {
        get().add(expectation);
    }

    @Override
    public void describeTo(Description description) {
        get().describeTo(description);
    }

    @Override
    public void describeMismatch(Invocation invocation, Description description) {
        get().describeMismatch(invocation, description);
    }

    @Override
    public void updateResponseTime(long threadId) {
        get().updateResponseTime(threadId);
        System.out.println("<!> Thread " + threadId + " " + get().totalResponseTime());
    }

    @Override
    public double totalResponseTime() {
        return get().totalResponseTime();
    }

    @Override
    public boolean isSatisfied() {
        return get().isSatisfied();
    }

    @Override
    public Object dispatch(Invocation invocation) throws Throwable {
        // multiple A calling one or more Bs
        Object ret = get().dispatch(invocation);
        Long k = Thread.currentThread().getId();
        Double v = totalResponseTime();
        threadResponseTimes.put(k, v);
        return ret;
    }
}