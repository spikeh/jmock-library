package uk.davidwei.perfmock.models.tweeter;

import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.integration.junit4.PerformanceMockery;
import uk.davidwei.perfmock.internal.perf.Delay;
import uk.davidwei.perfmock.internal.perf.Param;
import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.link.Link;
import uk.davidwei.perfmock.internal.perf.network.node.InfiniteServerNode;
import uk.davidwei.perfmock.internal.perf.network.node.Node;
import uk.davidwei.perfmock.internal.perf.network.node.Sink;
import uk.davidwei.perfmock.internal.perf.network.request.Customer;

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