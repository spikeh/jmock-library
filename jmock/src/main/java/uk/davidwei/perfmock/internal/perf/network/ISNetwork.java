package uk.davidwei.perfmock.internal.perf.network;

import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.internal.perf.Delay;
import uk.davidwei.perfmock.internal.perf.Param;
import uk.davidwei.perfmock.internal.perf.Sim;
import uk.davidwei.perfmock.internal.perf.network.link.Link;
import uk.davidwei.perfmock.internal.perf.network.node.InfiniteServerNode;
import uk.davidwei.perfmock.internal.perf.network.node.Node;
import uk.davidwei.perfmock.internal.perf.network.node.Sink;
import uk.davidwei.perfmock.internal.perf.network.request.Customer;

public class ISNetwork extends Network {
    private final Node<Customer> node;
    private final Sink<Customer> sink;

    public ISNetwork(Sim sim, Delay delay) {
        super(sim);
        this.node = new InfiniteServerNode<>(this, sim, "ISNode", delay);
        this.sink = new Sink<>(this, sim);
        Link<Customer> nodeToSink = new Link<>(this, sink);
        node.link(nodeToSink);
    }

    public void schedule(long threadId, Invocation invocation, Param param) {
        Customer customer = new Customer(this, sim, Thread.currentThread().getId(), invocation);
        node.enter(customer);
    }
}