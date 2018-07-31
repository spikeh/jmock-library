package uk.davidwei.perfmock.internal.perf.network;

import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.internal.perf.Delay;
import uk.davidwei.perfmock.internal.perf.Param;
import uk.davidwei.perfmock.internal.perf.Sim;
import uk.davidwei.perfmock.internal.perf.distribution.Distribution;
import uk.davidwei.perfmock.internal.perf.network.adt.CappedQueue;
import uk.davidwei.perfmock.internal.perf.network.link.Link;
import uk.davidwei.perfmock.internal.perf.network.node.QueueingNode;
import uk.davidwei.perfmock.internal.perf.network.node.Sink;
import uk.davidwei.perfmock.internal.perf.network.request.Customer;

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

    public void schedule(long threadId, Invocation invocation, Param param) {
        Customer customer = new Customer(this, sim, Thread.currentThread().getId(), invocation);
        node.enter(customer);
    }
}