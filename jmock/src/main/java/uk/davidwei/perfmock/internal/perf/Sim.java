package uk.davidwei.perfmock.internal.perf;

import uk.davidwei.perfmock.internal.NetworkDispatcher;
import uk.davidwei.perfmock.internal.perf.network.JavaTense;
import uk.davidwei.perfmock.internal.perf.network.request.Customer;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class Sim {
    public static final Sim INSTANCE = new Sim();

    private final Queue<Event<? extends Customer>> diary = new PriorityBlockingQueue<>(1000, new Comparator<Event<? extends Customer>>() {
        @Override
        public int compare(Event<? extends Customer> c1, Event<? extends Customer> c2) {
            return Double.compare(c1.invokeTime(), c2.invokeTime());
        }
    });
    private final Map<Long, Double> perThreadEntryTime = new HashMap<>();
    private final Map<Long, Double> perThreadExitTime = new HashMap<>();

    private final Map<Long, Long> threadPrevCpuTime = new HashMap<>();
    private final Map<Long, Long> threadSumCpuTime = new HashMap<>();
    private final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

    private double currentVTime = 0.0;
    private boolean isTenseEnabled = false;
    private long tenseStartTime = -1;

    // Called from QN models
    // Not called if Tense is enabled
    public double now() {
        return currentVTime;
    }

    public void enableTense() {
        isTenseEnabled = true;
        JavaTense.init();
    }

    // Called from QN models
    // Not called if Tense is enabled
    public void schedule(Event<? extends Customer> e) {
        Customer customer = e.getEventObject();
        Long parentThreadId = NetworkDispatcher.childToParentMap.get(customer.threadId());
        if (parentThreadId == null) {
            parentThreadId = customer.threadId();
        }
        synchronized (this) {
            if (!perThreadEntryTime.containsKey(parentThreadId)) {
                Double v = perThreadEntryTime.put(parentThreadId, customer.arrivalTime());
            }
        }
        diary.add(e);
    }

    // Called when Tense is enabled
    // TODO Multithread?
    public void schedule(long nanos) {
        if (isTenseEnabled) {
            JavaTense.jump0(nanos);
        }
    }

    // Called from QN models
    // Not called if Tense is enabled
    public void deschedule(Event<? extends Customer> e) {
        Customer customer = e.getEventObject();
        Long parentThreadId = NetworkDispatcher.childToParentMap.get(customer.threadId());
        if (parentThreadId == null) {
            parentThreadId = customer.threadId();
        }
        diary.remove(e);
    }

    // Called from Mockery::invoke() and PerformanceMockery::checking()
    // Do nothing if Tense is enabled
    public void start() {
        if (isTenseEnabled) {
            if (tenseStartTime < 0) {
                tenseStartTime = JavaTense.time();
            }
            return;
        }
        long threadId = Thread.currentThread().getId();
        if (!threadPrevCpuTime.containsKey(threadId)) {
            threadPrevCpuTime.put(threadId, mxBean.getThreadCpuTime(threadId));
        }
    }

    // Called from Mockery::invoke() and PerformanceMockery::repeat()
    // Do nothing if Tense is enabled
    public void stop() {
        if (isTenseEnabled) {
            return;
        }
        long threadId = Thread.currentThread().getId();
        long now = mxBean.getThreadCpuTime(threadId);
        Long prev = threadPrevCpuTime.get(threadId);
        if (prev != null) {
            if (threadSumCpuTime.containsKey(threadId)) {
                long sum = threadSumCpuTime.get(threadId);
                threadSumCpuTime.put(threadId, sum + (now - prev));
            } else {
                threadSumCpuTime.put(threadId, (now - prev));
            }
            threadPrevCpuTime.remove(threadId);
        }
    }

    // Called from PerformanceMockery::repeat()
    // Return 0 if Tense is enabled
    public long getCurrentThreadTotalCpuTime() {
        if (isTenseEnabled) {
            return 0;
        }
        long threadId = Thread.currentThread().getId();
        long res = threadSumCpuTime.get(threadId);
        threadSumCpuTime.remove(threadId);
        return res;
    }

    // Called from PerformanceMockery::mainLoop() -> NetworkDispatcher::tick()
    // And NetworkDispatcher::query()
    // Not called if Tense is enabled
    public Long runOnce() {
        while (!diary.isEmpty()) {
            Event<? extends Customer> e = diary.poll();
            assert(e != null);
            Customer customer = e.getEventObject();
            currentVTime = e.invokeTime();
            boolean stop = e.invoke();
            if (stop) {
                Long customerParentThreadId = NetworkDispatcher.childToParentMap.get(customer.threadId());
                if (customerParentThreadId == null) {
                    customerParentThreadId = customer.threadId();
                }
                assert (perThreadEntryTime.containsKey(customerParentThreadId));
                perThreadExitTime.put(customerParentThreadId, currentVTime);
                // TODO 21-11: if using a load generator... need to flush background load
                return customer.threadId();
            }
        }
        //throw new SimDiaryEmptyException();
        return null;
    }

    // This is called from the outer parent thread always.
    // This can also be called from thread 1 "main", if runConcurrent is NOT used
    //
    // Called from PerformanceMockery::parentEndCallback(), PerformanceMockery::repeat()
    // PerformanceMockery::doExtraStuff() (no repeat and no runConcurrent), and PerformanceMockery::runtime()
    // Return start time minus end time if Tense is enabled
    public double finalThreadResponseTime() {
        if (isTenseEnabled) {
            return (JavaTense.time() - tenseStartTime) / 1000000.0;
        }
        long threadId = Thread.currentThread().getId();
        if (perThreadEntryTime.get(threadId) == null) {
            assert (perThreadEntryTime.size() == 1);
            for (Map.Entry<Long, Double> e : perThreadEntryTime.entrySet()) {
                long thread = e.getKey();
                double entryTime = e.getValue();
                double exitTime = perThreadExitTime.get(thread);
                return exitTime - entryTime;
            }
        }
        return perThreadExitTime.get(threadId) - perThreadEntryTime.get(threadId);
    }

    public void resetCurrentThread() {
        long threadId = Thread.currentThread().getId();
        perThreadEntryTime.remove(threadId);
        perThreadExitTime.remove(threadId);

        diary.clear();
        //currentVTime = 0.0;
        tenseStartTime = -1;
    }

    public void reset() {
        diary.clear();
        perThreadEntryTime.clear();
        perThreadExitTime.clear();
        threadPrevCpuTime.clear();
        threadSumCpuTime.clear();
        currentVTime = 0.0;
    }
}