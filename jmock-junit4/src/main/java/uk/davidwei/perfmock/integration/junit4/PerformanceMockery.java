package uk.davidwei.perfmock.integration.junit4;

import uk.davidwei.perfmock.agent.PerfMockInstrumenter;
import uk.davidwei.perfmock.agent.ThreadConsumer;
import uk.davidwei.perfmock.internal.ExpectationBuilder;
import uk.davidwei.perfmock.internal.InvocationDispatcher;
import uk.davidwei.perfmock.internal.NetworkDispatcher;
import uk.davidwei.perfmock.internal.perf.PerformanceModel;
import uk.davidwei.perfmock.internal.perf.Sim;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import uk.davidwei.perfmock.internal.perf.network.JavaTense;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PerformanceMockery extends JUnitRuleMockery implements MethodRule {
    // Map of parent threadId to all child threadIds
    static final Map<Long, List<Long>> parentThreads = Collections.synchronizedMap(new HashMap<Long, List<Long>>());
    // Map of child threadId to parent threadId
    static final Map<Long, Long> childToParentMap = Collections.synchronizedMap(new HashMap<Long, Long>());
    public static PerformanceMockery INSTANCE;
    // Used to synchronise ``main'' thread and PerfMockery threads
    private final CountDownLatch startSignal = new CountDownLatch(1);
    private final String mainThreadName;
    //
    private final Semaphore mockerySemaphore = new Semaphore(0);
    private final NetworkDispatcher networkDispatcher = new NetworkDispatcher(sim, mockerySemaphore);
    /* Used in multi-threaded tests to track the number of child threads still alive.
     * expectThreads()
     * = expectedThreads
     * runConcurrent() + expectThreads()
     * = concurrentThreads * expectedThreads
     * Decremented in childEndCallback()
     */
    private final AtomicInteger aliveChildThreads = new AtomicInteger();
    /* Used in multi-threaded tests to track the number of parent threads still alive.
     * expectThreads()
     * = 1
     * runConcurrent()
     * = concurrentThreads
     * Decremented in parentEndCallback()
     */
    private final AtomicInteger aliveParentThreads = new AtomicInteger();
    /* Used in multi-threaded tests to check that the correct number of threads were created.
     * Checks for too few.
     * Decremented in parentEndCallback() and childEndCallback()
     * Checked in expectThreads() and runConcurrent()
     */
    private CountDownLatch threadCompleteSignal;
    private boolean concurrentExpectThreadsInit;
    private boolean concurrentTest;
    private boolean threadedTest;
    private boolean debug = false;
    private boolean tense = true;
    private List<Double> threadResponseTimes = Collections.synchronizedList(new ArrayList<Double>());

    /* Used in multi-threaded tests to check that the correct number of threads were created.
     * Checks for too many.
     *
     */
    private ThreadLocal<AtomicInteger> createdChildThreads = new ThreadLocal<AtomicInteger>() {
        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger();
        }
    };
    /*
     *
     */
    private ThreadLocal<Integer> expectedChildThreads = new ThreadLocal<>();

    private ThreadConsumer runConcurrentPreCallback = new ThreadConsumer() {
        @Override
        public void accept(Thread newThread) {
            Thread currentThread = Thread.currentThread();
            if (currentThread.getName().equals(mainThreadName)) {
                String s = String.format("Main threadId = %d, name = %s --> Parent threadId = %d, name = %s",
                        currentThread.getId(), currentThread.getName(),
                        newThread.getId(), newThread.getName());
                debugPrint(s);
                assert (!parentThreads.containsKey(newThread.getId()));
                PerformanceMockery.parentThreads.put(newThread.getId(), new ArrayList<Long>());
                NetworkDispatcher.parentThreads.put(newThread.getId(), new ArrayList<Long>());
            } else {
                throw new RuntimeException("unexpected threads created");
            }
        }
    };

    private ThreadConsumer runConcurrentExpectThreadsPreCallback = new ThreadConsumer() {
        @Override
        public void accept(Thread newThread) {
            Thread currentThread = Thread.currentThread();
            if (currentThread.getName().equals(mainThreadName)) {
                String s = String.format("Outer: Main threadId = %d, name = %s --> Parent threadId = %d, name = %s",
                        currentThread.getId(), currentThread.getId(),
                        newThread.getId(), newThread.getName());
                debugPrint(s);
                assert (!parentThreads.containsKey(newThread.getId()));
                PerformanceMockery.parentThreads.put(newThread.getId(), new ArrayList<Long>());
                NetworkDispatcher.parentThreads.put(newThread.getId(), new ArrayList<Long>());
            } else {
                String s = String.format("Inner: Parent threadId = %d, name = %s --> Child threadId = %d, name = %s",
                        currentThread.getId(), currentThread.getName(),
                        newThread.getId(), newThread.getName());
                debugPrint(s);

                aliveChildThreads.incrementAndGet();
                int threads = createdChildThreads.get().incrementAndGet();
                if (threads > expectedChildThreads.get()) {
                    throw new RuntimeException("too many threads created: got " + threads + ", expected " + expectedChildThreads.get());
                }

                List<Long> perfParentThreads = PerformanceMockery.parentThreads.get(currentThread.getId());
                if (perfParentThreads == null) {
                    perfParentThreads = new ArrayList<>();
                }
                perfParentThreads.add(newThread.getId());

                List<Long> networkParentThreads = NetworkDispatcher.parentThreads.get(currentThread.getId());
                if (networkParentThreads == null) {
                    networkParentThreads = new ArrayList<>();
                }
                networkParentThreads.add(newThread.getId());

                PerformanceMockery.childToParentMap.put(newThread.getId(), currentThread.getId());
                NetworkDispatcher.childToParentMap.put(newThread.getId(), currentThread.getId());
            }
        }
    };

    private ThreadConsumer expectThreadsPreCallback = new ThreadConsumer() {
        @Override
        public void accept(Thread newThread) {
            Thread currentThread = Thread.currentThread();
            if (currentThread.getName().equals(mainThreadName)) {
                // Main thread creating a parent thread
                String s = String.format("Main threadId = %d, name = %s --> Parent threadId = %d, name = %s",
                        currentThread.getId(), currentThread.getName(),
                        newThread.getId(), newThread.getName());
                debugPrint(s);

                PerformanceMockery.parentThreads.put(newThread.getId(), new ArrayList<Long>());
                NetworkDispatcher.parentThreads.put(newThread.getId(), new ArrayList<Long>());
            } else {
                // Parent thread creating a child thread
                String s = String.format("Parent threadId = %d, name = %s --> Child threadId = %d, name = %s",
                        currentThread.getId(), currentThread.getName(),
                        newThread.getId(), newThread.getName());
                debugPrint(s);

                int currentChildThreads = createdChildThreads.get().incrementAndGet();
                if (currentChildThreads > expectedChildThreads.get()) {
                    throw new RuntimeException("too many threads created: got " + currentChildThreads + ", expected " + expectedChildThreads.get());
                }

                List<Long> perfParentThreads = PerformanceMockery.parentThreads.get(currentThread.getId());
                if (perfParentThreads == null) {
                    perfParentThreads = new ArrayList<>();
                }
                perfParentThreads.add(newThread.getId());

                List<Long> networkParentThreads = NetworkDispatcher.parentThreads.get(currentThread.getId());
                if (networkParentThreads == null) {
                    networkParentThreads = new ArrayList<>();
                }
                networkParentThreads.add(newThread.getId());

                PerformanceMockery.childToParentMap.put(newThread.getId(), currentThread.getId());
                NetworkDispatcher.childToParentMap.put(newThread.getId(), currentThread.getId());
                NetworkDispatcher.allChildNames.add(newThread.getName());
            }
        }
    };

    private ThreadConsumer expectThreadsPostCallback = new ThreadConsumer() {
        @Override
        public void accept(Thread currentThread) {
            // Only for child threads
            if (!parentThreads.containsKey(currentThread.getId())) {
                PerformanceMockery.INSTANCE.childEndCallback();
            }
        }
    };

    private ThreadConsumer tensePreCallback = new ThreadConsumer() {
        @Override
        public void accept(Thread newThread) {
            long currentThreadId = Thread.currentThread().getId();
            // Only the parent, the main thread, will create children
            PerformanceMockery.parentThreads.computeIfAbsent(currentThreadId, k -> new ArrayList<Long>()).add(newThread.getId());
            NetworkDispatcher.parentThreads.computeIfAbsent(currentThreadId, k -> new ArrayList<Long>()).add(newThread.getId());
            PerformanceMockery.childToParentMap.put(newThread.getId(), currentThreadId);
            NetworkDispatcher.childToParentMap.put(newThread.getId(), currentThreadId);
            NetworkDispatcher.allChildNames.add(newThread.getName());
            // tense_time(&tense_start);
            // clock_gettime(CLOCK_THREAD_CPUTIME_ID, &t_start);
            //
            // start routine...
            //
            // clock_gettime(CLOCK_THREAD_CPUTIME_ID, &t_end);
            // tense_time(&tense_end);
            //
            // compute deltas
            //
            // tense_destroy();
        }
    };

    private ThreadConsumer tenseStartCallback = new ThreadConsumer() {
        @Override
        public void accept(Thread thread) {
            JavaTense.init();
        }
    };

    private ThreadConsumer tensePostCallback = new ThreadConsumer() {
        @Override
        public void accept(Thread currentThread) {
            JavaTense.destroy();
        }
    };

    public PerformanceMockery() {
        PerformanceMockery.INSTANCE = this;
        InvocationDispatcher.setNetworkDispatcher(networkDispatcher);
        this.concurrentExpectThreadsInit = false;
        this.concurrentTest = false;
        this.threadedTest = false;
        this.mainThreadName = Thread.currentThread().getName();
    }

    private void mainLoop() {
        try {
            while (aliveParentThreads.get() > 0) {
                debugPrint("Main thread going to sleep, aliveParentThreads = " + aliveParentThreads.get());
                mockerySemaphore.acquire();
                debugPrint("Main thread is awake now");
                if (aliveParentThreads.get() > 0) {
                    Long threadToResume = networkDispatcher.tick();
                    if (threadToResume != null) {
                        debugPrint("Main thread decided to wake thread " + threadToResume);
                        networkDispatcher.wake(threadToResume);
                    } else {
                        debugPrint("Sim diary was empty, sleep again...");
                    }
                } else {
                    debugPrint("Main thread, aliveParentThreads = " + aliveParentThreads.get());
                }
            }
            debugPrint("Main thread finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void checking(ExpectationBuilder expectations) {
        super.checking(expectations);
        sim.start();
    }

    public Sim sim() {
        return sim;
    }

    public void enableDebug() {
        this.debug = true;
        networkDispatcher.enableDebug();
    }

    private void debugPrint(String msg) {
        if (debug) {
            System.out.println(msg);
        }
    }

    private void childEndCallback() {
        int alive = aliveChildThreads.decrementAndGet();
        debugPrint("Thread " + Thread.currentThread().getId() + " childEndCallback(), alive = " + alive);
        threadCompleteSignal.countDown();
        mockerySemaphore.release();
    }

    private void parentEndCallback() {
        threadResponseTimes.add(sim.finalThreadResponseTime());
        int alive = aliveParentThreads.decrementAndGet();
        debugPrint("Thread " + Thread.currentThread().getId() + " parentEndCallback(), alive = " + alive);
        threadCompleteSignal.countDown();
        mockerySemaphore.release();
    }

    public <T> T mock(Class<T> typeToMock, PerformanceModel model) {
        String defaultName = namingScheme.defaultNameFor(typeToMock);
        if (mockNames.contains(defaultName)) {
            throw new IllegalArgumentException("a mock with name " + defaultName + " already exists");
        }

        networkDispatcher.registerModel(defaultName, model);
        return mock(typeToMock, defaultName);
    }

    public void repeat(int times, final Runnable testScenario) {
        for (int i = 0; i < 10 + times; i++) {
            testScenario.run();
            sim.stop();
            mockerySemaphore.drainPermits();
            concurrentTest = false;
            threadedTest = false;
            // For the case of repeat but not runConcurrent
            if (threadResponseTimes.size() == i) {
                long nanoTime = sim.getCurrentThreadTotalCpuTime();
                threadResponseTimes.add(sim.finalThreadResponseTime() + ((double) nanoTime / 1000000));
                sim.resetCurrentThread();
            }
        }
        threadResponseTimes = threadResponseTimes.subList(10, threadResponseTimes.size());
    }

    public void runConcurrent(int numThreads, final Runnable testScenario) {
        if (threadedTest) {
            throw new RuntimeException("test syntax error: runConcurrent must be nested above expectThreads");
        }
        concurrentTest = true;
        PerfMockInstrumenter.setPreCallback(runConcurrentPreCallback);
    
        setInvocationDispatcher(new ParallelInvocationDispatcher());
        this.threadCompleteSignal = new CountDownLatch(numThreads);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    startSignal.await();
                    testScenario.run();
                    assertIsSatisfied();
                    parentEndCallback();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };

        aliveParentThreads.set(numThreads);
        networkDispatcher.setAliveParentThreads(aliveParentThreads);
        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(r, "PerfMockery-" + i);
            long threadId = t.getId();
            Semaphore threadSemaphore = new Semaphore(0);
            networkDispatcher.registerThread(threadId, threadSemaphore);
            t.start();
        }
        startSignal.countDown();
        mainLoop();
        try {
            threadCompleteSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void expectThreads(final int expectedThreads, final Runnable testScenario) {
        threadedTest = true;
        if (tense) {
            if (concurrentTest) {

            } else {
                PerfMockInstrumenter.setPreCallback(tensePreCallback);
                PerfMockInstrumenter.setPostCallback(tensePostCallback);
                PerfMockInstrumenter.setTenseStartCallback(tenseStartCallback);
                setInvocationDispatcher(new ParallelInvocationDispatcher());
                testScenario.run();
            }
        } else {
            if (concurrentTest) {
                // We're being run on a separate PerfMockery-n thread
                expectedChildThreads.set(expectedThreads);
                synchronized (this) {
                    if (!concurrentExpectThreadsInit) {
                        PerfMockInstrumenter.setPreCallback(runConcurrentExpectThreadsPreCallback);
                        PerfMockInstrumenter.setPostCallback(expectThreadsPostCallback);
                    }
                }
                testScenario.run();
            } else {
                PerfMockInstrumenter.setPreCallback(expectThreadsPreCallback);
                PerfMockInstrumenter.setPostCallback(expectThreadsPostCallback);

                setInvocationDispatcher(new ParallelInvocationDispatcher());
                // 1 = Parent
                // expectedThreads = Child
                this.threadCompleteSignal = new CountDownLatch(1 + expectedThreads);
                Runnable parentRunnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            startSignal.await();
                            expectedChildThreads.set(expectedThreads + 1);
                            testScenario.run();
                            assertIsSatisfied();
                            parentEndCallback();
                        } catch (Throwable e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                };

                aliveChildThreads.set(expectedThreads);
                networkDispatcher.setAliveChildThreads(aliveChildThreads);
                aliveParentThreads.set(1);
                networkDispatcher.setAliveParentThreads(aliveParentThreads);
                for (int i = 0; i < 1; i++) {
                    Thread t = new Thread(parentRunnable, "PerfMockery-" + i);
                    Semaphore threadSemaphore = new Semaphore(0);
                    networkDispatcher.registerThread(t.getId(), threadSemaphore);
                    t.start();
                }
                startSignal.countDown();
                mainLoop();
                try {
                    boolean success = threadCompleteSignal.await(3, TimeUnit.SECONDS);
                    if (!success) {
                        throw new Error("expected " + expectedThreads + " threads to be created");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeHtml(FrameworkMethod method) {
        //String tmpDir = System.getProperty("java.io.tmpdir");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
        Path dirPath = Paths.get("target", dtf.format(LocalDateTime.now()));
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path filePath = Paths.get(dirPath.toString(),
                method.getDeclaringClass().getName() + "-" + method.getName() + ".html");
        try {
            BufferedReader brJs = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/d3.min.js")));
            Files.write(Paths.get(dirPath.toString(), "d3.min.js"), brJs.lines().collect(Collectors.toList()));
            BufferedReader brFront = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/front.html")));
            List<String> frontLines = brFront.lines().collect(Collectors.toList());
            frontLines.add("var data = " + threadResponseTimes + ";");
            Files.write(filePath, frontLines);
            BufferedReader brBack = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/back.html")));
            Files.write(filePath, brBack.lines().collect(Collectors.toList()), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doExtraStuff(FrameworkMethod method) {
        // For the case of no repeat and no runConcurrent
        if (threadResponseTimes.isEmpty()) {
            threadResponseTimes.add(sim.finalThreadResponseTime());
        }
        //System.out.println(threadResponseTimes);
        writeHtml(method);
        if (tense) {
            JavaTense.destroy();
        }
    }

    public double runtime() {
        return sim.finalThreadResponseTime();
    }

    public List<Double> runtimes() {
        return threadResponseTimes;
    }

    public double mean() {
        if (threadResponseTimes.size() > 0) {
            Double sum = 0.0;
            for (Double d : threadResponseTimes) {
                sum += d;
            }
            return sum / threadResponseTimes.size();
        } else {
            return 0.0;
        }
    }

    public void enableTense() {
        tense = true;
        networkDispatcher.enableTense();
        sim.enableTense();
    }
}