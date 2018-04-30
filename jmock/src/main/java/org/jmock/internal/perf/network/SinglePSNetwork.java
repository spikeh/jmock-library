package org.jmock.internal.perf.network;

import org.jmock.api.Invocation;
import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.Param;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.distribution.Distribution;
import org.jmock.internal.perf.network.link.Link;
import org.jmock.internal.perf.network.node.ProcessorSharingNode;
import org.jmock.internal.perf.network.node.QueueingNode;
import org.jmock.internal.perf.network.node.Sink;
import org.jmock.internal.perf.network.request.Customer;

public class SinglePSNetwork extends Network {
    private final QueueingNode<Customer> node;
    private final Sink<Customer> sink;

    public SinglePSNetwork(Sim sim, Distribution serviceTime) {
        super(sim);
        this.node = new ProcessorSharingNode<Customer>(this, sim, "node", new Delay(serviceTime), 1);
        this.sink = new Sink<>(this, sim);
        Link<Customer> nodeToSink = new Link<>(this, sink);
        node.link(nodeToSink);
    }

    public void schedule(long threadId, Invocation invocation, Param param) {
        Customer customer = new Customer(this, sim, Thread.currentThread().getId(), invocation);
        node.enter(customer);
    }
}