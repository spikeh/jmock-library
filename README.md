# PerfMock

Testing helps us to build reliable and robust software. Pure unit tests are the smallest unit of testing that focus on testing a single component in isolation. While useful, particularly within the test-driven development (TDD) process, they do not indicate whether a system works as a whole, nor address performance concerns.

Performance testing is typically done when a system is sufficiently complete to be deployed and tested as a whole. Performance issues identified at this late stage can be expensive to resolve due to fundamental design issues of a system. Performance tests are also often slow and inconvenient to run, which is at odds with the fast feedback loops associated with TDD.

Mock objects replace the collaborators of an object under test with an alternative implementation that serve to only support the test, and are used to perform unit testing in isolation. They can be configured to behave in particular ways to simulate different scenarios, and used to verify that the expected messages are exchanged between the various objects involved in a given test scenario.

We introduce the concept of *performance unit tests* that builds on the use of *mock objects* in unit testing by embedding a *performance model* that predict the time the object being mocked will take to respond to a message. While the execution of real code can be timed at the unit testing level (a form of micro-benchmarking), the real execution time of a mock object will be very different to that of the object it represents as it contains minimal internal logic. By embedding a *performance model* within the mock object, we obtain a way to estimate the actual execution time of the real object whose behaviour is being mocked.

It's crucial to point out that performance models work entirely in virtual time, i.e. the predicted response times can be significantly longer than the real time spent executing the model. This enables performance unit tests to be written that run quickly in real time and can make assertions about performance as well as behaviour.

The key idea behind performance unit tests is that they help to drive development by enabling developers to reason about the performance impact of new features. Collaborators to the object under test may not yet exist, so these tests can help determine how they must perform in order for the application to perform within requirements. Unlike traditional performance testing, these tests do not require the code to be complete and deployed, and integration with unit testing frameworks enables them to be executed early and often, as part of the TDD process, even before code is committed into source control. **PerfMock** is an extension to jMock 2 framework for Java that enables developers to write such performance unit tests.

# Getting PerfMock

The easiest way is to download packaged JARs from https://davidwei.uk/perfmock

Then install them into your local Maven repository using the groupId `org.jmock`. The artifactId and version numbers are in the file names. For example:

`mvn install:install-file -Dfile=jmock-2.8.3.jar -DgroupId=org.jmock -DartifactId=jmock -Dversion=2.8.3 -Dpackaging=jar`

# TDD using unit tests and mock objects

Let's use a running example to illustrate PerfMock. Suppose that we are creating a micro-blogging web application named Tweeter, using a Model-View-Controller pattern. Specifically, we are developing a new controller method that renders a user's timeline of messages. The figure below shows an overview of its architecture, where a controller \(C\) handles a request from a client by using two collaborators `UserService` (US) and `MessageService` (MS). The controller then prepares a model containing the data needed by the view to render the response, and returns it to the client.

![](https://davidwei.uk/perfmock/Fig1.png)

Applying TDD, we would write a unit test for this method first:

```java
public class ControllerTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    
    UserService usrs = context.mock(UserService.class);
    MessageService msgs = context.mock(MessageService.class);
    
    @Test
    public void rendersUserTimeline() {
        TweeterController controller = new TweeterController(usrs, msgs);
        User alice = new User("Alice");
        
        context.checking(new Expectations() {{
            exactly(1).of(usrs).getByUsername("Alice");
                will(returnValue(alice));
            exactly(1).of(msgs).getUserTimeline(alice);
        }});
        
        controller.userTimeline("Alice", new ModelMap());
    }
}
```

The test class manages mock objects and their expected method invocations via the jMock 2 context object, `JUnitRuleMockery`. By setting expectations that specify how the controller should interact with its collaborators, the process of writing the unit test can help developers define the API. In this case, we expect one call to `getByUsername` on `UserService` and `getUserTimeline` on `MessageService`.

Assuming a skeleton method for userTimeline such that the test compiles, if we run the test it will fail due to expectations not being met. The next step is to write the minimal implementation needed to make it pass, an example of which is shown below.

```java
@Controller
public class TweeterController {
    private final UserService usrs;
    private final MessageService msgs;
    
    public TweeterController(UserService usrs, MessageService msgs) {
        this.usrs = usrs;
        this.msgs = msgs;
    }
    
    @RequestMapping("/u/{username}")
    public String userTimeline(@PathVariable String username, Model model) {
        User userProfile = usrs.getByUsername(username);
        model.addAttribute("username", username);
        List<Message> userMsgs = msgs.getUserTimeline(userProfile);
        model.addAttribute("messages", userMsgs);
        return "timeline";
    }
}
```

Once the test passes, we may refactor and then repeat the cycle for the next requirement. This red (write a failing test), green (make it pass) and refactor cycle is the core of the TDD process.

# Using performance unit tests

Let's now demonstrate how to convert the above unit test into a performance unit test, and to apply performance test driven development.

```java
public class ControllerTest {
    @Rule
    public PerformanceMockery context = new PerformanceMockery();
    
    UserService usrs = context.mock(UserService.class, exponentialDist(1.5));
    MessageService msgs = context.mock(MessageService.class, exponentialDist(1.5));
    
    @Test
    public void rendersUserTimeline() {
        TweeterController controller = new TweeterController(usrs, msgs);
        User alice = new User("Alice");
        
        context.repeat(2000, () -> {
            context.checking(new Expectations() {{
                exactly(1).of(usrs).getByUsername("Alice");
                    will(returnValue(alice));
                exactly(1).of(msgs).getUserTimeline(alice);
            }});
            
            controller.userTimeline("Alice", new ModelMap());
        });
        
        assertThat(context.runtimes(), hasPercentile(80, lessThan(15.0)));
    }
}
```

The original `JUnitRuleMockery` is replaced with `PerformanceMockery`, a direct drop-in replacement. The `mock` method accepts a second parameter that specifies a performance model, which in this case is a *probability distribution model*. Random samples are drawn from the distribution each time the mock object is invoked as the predicted response time.

The stochastic nature of performance models means that it is not sufficient to run a performance unit test just once. PerfMock allows a test kernel to be repeatedly executed, grouped together as a single performance experiment, by using the `repeat` method.

Performance experiments enable performance assertions to be made against the aggregated list of predicted response times using statistical measures. In the example, the `assertThat` assertion requires the 80th percentile predicted response time to be less than 15 ms.

# Adding new features

The above performance test driven development process is repeated for the next requirement. Let's say we want to add the ability to reply to messages. This means that on the user timeline, we need to render all the replies for a given message.

Again, we start by writing a performance unit test for this new desired behaviour.

```java
public class ControllerTest {
    @Rule
    public PerformanceMockery context = new PerformanceMockery();
    
    UserService usrs = context.mock(UserService.class, exponentialDist(1.5));
    MessageService msgs = context.mock(MessageService.class, exponentialDist(1.5));
    
    @Test
    public void rendersUserTimeline() {
        TweeterController controller = new TweeterController(usrs, msgs);
        User alice = new User("Alice");
        List<Message> TEN_MSGS = Arrays.asList(...);
        
        context.repeat(2000, () -> {
            context.checking(new Expectations() {{
                exactly(1).of(usrs).getByUsername("Alice");
                    will(returnValue(alice));
                exactly(1).of(msgs).getUserTimeline(alice);
                    will(returnValue(TEN_MSGS));
                exactly(10).of(msgs).getReplies(with(any(Message.class)));
            }});
            
            controller.userTimeline("Alice", new ModelMap());
        });
        
        assertThat(context.runtimes(), hasPercentile(80, lessThan(15.0)));
    }
}
```

We require a new method `getReplies` to be added to the existing `MessageService` interface. We configure Alice's timeline to have 10 messages, and would thus expect 10 calls to `getReplies`. We assume the same overall performance target as before, so models and assertions remain unchanged.

To make the functional part of the test pass, we implement the required changes to the controller.

```java
@Controller
public class TweeterController {
    @RequestMapping("/u/{username}")
    public String userTimeline(@PathVariable String username, Model model) {
        User userProfile = usrs.getByUsername(username);
        model.addAttribute("username", username);
        List<Message> userMsgs = msgs.getUserTimeline(userProfile);
        for (Message m : userMsgs) {
            List<Reply> replies = msgs.getReplies(m);
            m.addReplies(replies);
        }
        model.addAttribute("messages", userMsgs);
        return "timeline";
    }
}
```

# Making tests pass

When we run the test above, it fails, which can happen at any point in development.

```
java.lang.AssertionError:
Expected: percentile 80 to be a value less than <15.0>
     but: <22.172782855636996>
```

When this happens, there are three things that can be done:

## Relax performance assertions

The performance assertions themselves can be adjusted, and whether this is acceptable or not depends on how they were defined in the first place. If they are not derived from hard requirements i.e. service level agreements, then it may be fine to loosen them for additional features.

## Adjust the models

The parameters to a performance model can be adjusted, which means changing how a collaborator needs to perform in order to meet the performance requirements of the object under test. Later on, the model can be used as a contract to drive the development of the collaborator.

In our Tweeter controller example, both `UserService` and `MessageService` are likely to rely on an underlying database, so adjusting the model parameters such that those two services perform faster might have consequences for the choice of database software, hardware provisioning, configuration, or data modelling.

## Optimise the object under test

The object under test itself can be optimised to run faster, by using better algorithms, better data structures, make fewer calls to collaborators, or make these calls in parallel.

Let's suppose we decide to use threads in order to make concurrent calls to `getReplies`. For PerfMock to support this, the test requires a special Java agent `perfmock-instrumenter.jar` to be attached and the test kernel must be wrapped in a call to `expectThreads` that specifies the number of threads the object under test is expected to create. This can be thought of as an additional test expectation.

```java
public class ControllerTest {
    @Test
    public void rendersUserTimeline() {
        TweeterController controller = new TweeterController(usrs, msgs);
        User alice = new User("Alice");
        List<Message> TEN_MSGS = Arrays.asList(...);
        
        context.repeat(2000, () -> {
            context.expectThreads(3, () -> {
                context.checking(new Expectations() {{
                    exactly(1).of(usrs).getByUsername("Alice");
                        will(returnValue(alice));
                    exactly(1).of(msgs).getUserTimeline(alice);
                        will(returnValue(TEN_MSGS));
                    exactly(10).of(msgs).getReplies(with(any(Message.class)));
                }});
                
                controller.userTimeline("Alice", new ModelMap());
            });
        });
        
        assertThat(context.runtimes(), hasPercentile(80, lessThan(15.0)));
    }
}
```

In an effort to make the test pass, we change the implementation of `userTimeline` in the controller to use a fixed thread pool with 3 threads in order to make concurrent calls to `getReplies`.

```java
@Controller
public class TweeterController {
    @RequestMapping("/u/{username}")
    public String userTimeline(@PathVariable String username, Model model) {
        ExecutorService es = Executors.newFixedThreadPool(3);
        User userProfile = usrs.getByUsername(username);
        model.addAttribute("username", username);
        List<Message> userMsgs = msgs.getUserTimeline(userProfile);
        for (Message m : userMsgs) {
            es.submit(() -> {
                List<Reply> replies = msgs.getReplies(m);
                m.addReplies(replies);
            });
        }
        es.shutdown();
        model.addAttribute("messages", userMsgs);
        try { es.awaitTermination(5, TimeUnit.SECONDS); }
        catch (InterruptedException e) {}
        return "timeline";
    }
}
```

With the above changes, the previously failing test now passes.

# Moving the lens

When we move onto implementing the downstream services e.g. `UserService` and `MessageService` using the same performance test driven development process, we can use the models defined in the performance unit tests for the controller to drive it. The performance models and their parameters specify how an implementation must perform in order for the controller to pass its performance assertions.

We need to choose a database implementation first, and let us assume we chose Apache Cassandra. We then begin with `getByUsername` in `UserService` by writing a performance unit test. We require a driver to communicate with the Cassandra database, and Spring Data Cassandra provides a convenient high level interface `CassandraOperations` that has the usual CRUD operations.

```java
public class CassandraUsersServiceTest {
    @Rule
    public PerformanceMockery context = new PerformanceMockery();

    @Test
    public void getsUsersByUserid() {
        CassandraOperations driver = context.mock(CassandraOperations.class, exponentialDist(1.2));
        UserService userService = new CassandraUsersService(driver);
        
        context.repeat(2000, () -> {
            context.checking(new Expectations() {{
                exactly(1).of(driver).selectOne("SELECT * FROM users WHERE username='Alice';",
                                                User.class);
            }});
            
            userService.getByUsername("Alice");
        });
        
        assertThat(context.runtimes(), matchDistribution(exponentialDist(1.5)));
    }
}
```

We can now make an assertion that the performance of CassandraUserService match or exceed the requirements set in the performance unit test for the controller by using `matchDistribution`.
