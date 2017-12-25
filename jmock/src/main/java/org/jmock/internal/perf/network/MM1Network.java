package org.jmock.internal.perf.network;

import org.jmock.api.Invocation;
import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.distribution.Exp;
import org.jmock.internal.perf.network.link.Link;
import org.jmock.internal.perf.network.node.QueueingNode;
import org.jmock.internal.perf.network.node.Sink;
import org.jmock.internal.perf.network.request.Customer;

public class MM1Network extends Network {
    private final QueueingNode<Customer> node;
    private final Sink<Customer> sink;

    public MM1Network(Sim sim) {
        super(sim);
        this.node = new QueueingNode<>(this, sim, "FCFS", new Delay(new Exp(0.5)));
        this.sink = new Sink<>(this, sim);
        Link<Customer> nodeToSink = new Link<>(this, sink);
        node.link(nodeToSink);
    }

    public void query(long threadId, Invocation invocation) {
        Customer customer = new Customer(this, sim, Thread.currentThread().getId(), invocation);
        node.enter(customer);
    }
}