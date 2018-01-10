# PerfMock

PerfMock is an extension to jMock 2 for Java that introduces *performance unit tests*.

Performance unit tests check the performance characteristics of an object under test; they are similar to how unit tests check for behavioural correctness.

An object under test may have complex dependencies, which makes real instances of them problematic to create or use. *Mock objects* that simulate the external behaviour of dependencies are used to get around this problem.

PerfMock checks performance by timing the execution time of a test which contains the object under test, created using mocked dependencies. It's important to note that the time (wall-clock or CPU) spent executing a method on a mock object representing a dependency bears no direct relation to time spent executing the same method on a real instance; consider a method on a dependency that involves I/O. Timing mock objects only measure the time spent inside the mock object framework.

PerfMock adds *performance models* to mock objects, which simulate the performance characteristics of a dependency. These models are used by PerfMock to predict the amount of time it takes to execute a method on a mock object. The key idea is that the predicted times returned by a performance model can be significantly longer than the actual time taken to execute the model.

Performance models have many different implementations. It could be a statistical model that randomly samples from a distribution e.g. normal with specific parameters; it could be simulation models using formal languages such as queueing networks or stochastic Petri nets; or it could sample recorded performance metrics from a production environment.

PerfMock interleaves predicted *virtual* times returned by performance models with measured *real* times spent executing the rest of the performance unit test to produce an overall execution time. Assertions can be made on test execution times to ensure that the object under test is sufficiently performant.

Performance unit tests can be written alongside unit tests, integrating performance testing alongside the development process.

This work was presented at BCS SPA 2017 in London and Mini BCS SPA 2017 in Leeds.

# Building

Please note that PerfMock overwrites jMock 2's group ID and artifact ID!

To build locally, simply run Maven:

`mvn package`

To install PerfMock into a local Maven repository, run:

`mvn install`

# Usage

It's very easy to write performance unit tests using PerfMock. First, create a `PerformanceMockery` object in a test class. This is a drop-in replacement for a jMock 2 `Mockery` context, which is a factory for creating mock objects.

```java
class TestClass {
    @Rule
    public PerformanceMockery context = new PerformanceMockery();
}
```

Next, create a mock of an interface using the Mockery context we just created and use it to instantiate a real object under test.

```java
class TestClass {
    @Test
    public void testMethod() {
        AnInterface instance = context.mock(AnInterface.class, normalDist(100.0, 20.0));
        ObjectUnderTest obj = new ObjectUnderTest(instance);
    }
}
```

The 2nd parameter to the `mock()` method is a `PerformanceModel`, which has many types of concrete implementations. This example uses a statistical normal distribution model, with a mean of 100 ms and standard deviation of 20 ms.

Note that, most performance models are non-deterministic, therefore it's not very useful to simply execute a test once. PerfMock provides a method `repeat()` that allows a test kernel to be repeated and grouped together as a single *experiment*.

```java
class TestClass {
    @Test
    public void testMethod() {
        AnInterface instance = context.mock(AnInterface.class, normalDist(100.0, 20.0));
        ObjectUnderTest obj = new ObjectUnderTest(instance);
        
        context.repeat(100, () -> {
            exactly(1).of(instance).implementedMethod();
        });
        
        obj.doSomething();
    }
}
```

Finally, assertions can be made on performance requirements:

```java
class TestClass {
    @Test
    public void testMethod() {
        // ...
        assertThat(context.runtimes(), hasPercentile(80, lessThan(150.0)));
    }
}
```

This assertion checks that the 80th percentile execution time is under 150 ms. Note that assertions are made statistically on an entire experiment.
