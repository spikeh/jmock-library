package uk.davidwei.perfmock.internal.perf.network;

import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.internal.perf.Delay;
import uk.davidwei.perfmock.internal.perf.Param;
import uk.davidwei.perfmock.internal.perf.Sim;
import uk.davidwei.perfmock.internal.perf.distribution.Exp;
import uk.davidwei.perfmock.internal.perf.network.adt.FIFOQueue;
import uk.davidwei.perfmock.internal.perf.network.link.Link;
import uk.davidwei.perfmock.internal.perf.network.link.ProbabilisticBranch;
import uk.davidwei.perfmock.internal.perf.network.node.Node;
import uk.davidwei.perfmock.internal.perf.network.node.ProcessorSharingNode;
import uk.davidwei.perfmock.internal.perf.network.node.QueueingNode;
import uk.davidwei.perfmock.internal.perf.network.node.Sink;
import uk.davidwei.perfmock.internal.perf.network.request.Customer;

public class CPUNetwork extends Network {
    private ProcessorSharingNode<Customer> psNode;
    private Sink<Customer> sink;

    public CPUNetwork(Sim sim) {
        super(sim);
        this.psNode = new ProcessorSharingNode<Customer>(this, sim, "PS Node", new Delay(new Exp(1 / 0.05)));
        this.sink = new Sink<Customer>(this, sim);
        // TODO change this syntax?
        Link nodeToSink = new Link(this, sink);
        //psNode.link(nodeToSink);

        Node<Customer> disk1 = new QueueingNode<Customer>(this, sim, "Disk1", new Delay(new Exp(0.03)), 1, new FIFOQueue());
        Node<Customer> disk2 = new QueueingNode<Customer>(this, sim, "Disk2", new Delay(new Exp(0.027)), 1, new FIFOQueue());

        double[] routingProbs = {1.0 / 121.0, 70.0 / 121.0, 50.0 / 121.0};
        ProbabilisticBranch<Customer> cpuOutputLink = new ProbabilisticBranch<>(this, routingProbs, new Node[]{sink, disk1, disk2});

        psNode.link(cpuOutputLink);
        disk1.link(new Link<>(this, psNode));
        disk2.link(new Link<>(this, psNode));
    }

    public void schedule(long threadId, Invocation invocation, Param param) {
        Customer customer = new Customer(this, sim, Thread.currentThread().getId(), invocation);
        psNode.enter(customer);
    }
}