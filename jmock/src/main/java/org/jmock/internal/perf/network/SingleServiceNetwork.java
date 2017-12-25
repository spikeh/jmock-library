package org.jmock.internal.perf.network;

import org.jmock.api.Invocation;
import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.distribution.Distribution;
import org.jmock.internal.perf.network.adt.CappedQueue;
import org.jmock.internal.perf.network.link.Link;
import org.jmock.internal.perf.network.node.QueueingNode;
import org.jmock.internal.perf.network.node.Sink;
import org.jmock.internal.perf.network.request.Customer;

public class SingleServiceNetwork extends Network {
    private final QueueingNode<Customer> node;
    private final Sink<Customer> sink;

    public SingleServiceNetwork(Sim sim, Distribution serviceTime, CappedQueue queueingDiscipline) {
        super(sim);
        this.node = new QueueingNode<Customer>(this, sim, "node", new Delay(serviceTime), 1, queueingDiscipline);
        this.sink = new Sink<>(this, sim);
        Link<Customer> nodeToSink = new Link<>(this, sink);
        node.link(nodeToSink);
    }

    public void query(long threadId, Invocation invocation) {
        Customer customer = new Customer(this, sim, Thread.currentThread().getId(), invocation);
        node.enter(customer);
    }
}