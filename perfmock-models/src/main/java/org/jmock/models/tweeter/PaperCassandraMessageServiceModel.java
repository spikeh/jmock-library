package org.jmock.models.tweeter;

import org.jmock.api.Invocation;
import org.jmock.integration.junit4.PerformanceMockery;
import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.Param;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.link.Link;
import org.jmock.internal.perf.network.node.InfiniteServerNode;
import org.jmock.internal.perf.network.node.Node;
import org.jmock.internal.perf.network.node.Sink;
import org.jmock.internal.perf.network.request.Customer;

public class PaperCassandraMessageServiceModel extends Network<Customer> {

    private final Node<Customer> getUserTimeline;
    private final Node<Customer> getReplies;
    private final Sink<Customer> sink;

    public PaperCassandraMessageServiceModel(String fig) {
        super(PerformanceMockery.INSTANCE.sim());
        this.getUserTimeline = new InfiniteServerNode<>(this, sim, "ISNode", new Delay(new RandomEmpiricalDistribution(fig + "_getUserTimelineFixed.txt")));
        this.getReplies = new InfiniteServerNode<>(this, sim, "ISNode", new Delay(new RandomEmpiricalDistribution(fig + "_getRepliesFixed.txt")));
        this.sink = new Sink<>(this, sim);
        Link<Customer> nodeToSink = new Link<>(this, sink);
        getUserTimeline.link(nodeToSink);
        getReplies.link(nodeToSink);
    }

    @Override
    public void schedule(long threadId, Invocation invocation, Param param) {
        Customer customer = new Customer(this, sim, Thread.currentThread().getId(), invocation);

        String methodName = invocation.getInvokedMethod().getName();
        if (methodName.matches("getUserTimeline")) {
            getUserTimeline.enter(customer);
        } else if (methodName.matches("getReplies")) {
            getReplies.enter(customer);
        } else {
            System.out.println("PaperCassandraMessageServiceModel: unknown method");
        }
    }
}