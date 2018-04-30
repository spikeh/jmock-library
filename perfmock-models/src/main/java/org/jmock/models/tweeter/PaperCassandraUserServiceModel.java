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

public class PaperCassandraUserServiceModel extends Network<Customer> {

    private final Node<Customer> getByUsernameNode;
    private final Sink<Customer> sink;

    public PaperCassandraUserServiceModel(String fig) {
        super(PerformanceMockery.INSTANCE.sim());
        this.getByUsernameNode = new InfiniteServerNode<>(this, sim, "ISNode", new Delay(new RandomEmpiricalDistribution(fig + "_getRandomUser.txt")));
        this.sink = new Sink<>(this, sim);
        Link<Customer> nodeToSink = new Link<>(this, sink);
        getByUsernameNode.link(nodeToSink);
    }

    @Override
    public void schedule(long threadId, Invocation invocation, Param param) {
        Customer customer = new Customer(this, sim, Thread.currentThread().getId(), invocation);
        getByUsernameNode.enter(customer);
    }
}