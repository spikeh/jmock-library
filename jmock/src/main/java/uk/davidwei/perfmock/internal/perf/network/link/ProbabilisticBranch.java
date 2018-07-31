package uk.davidwei.perfmock.internal.perf.network.link;

import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.node.Node;
import uk.davidwei.perfmock.internal.perf.network.request.Customer;

public class ProbabilisticBranch<T extends Customer> extends Link<T> {
    private DiscreteSampler dist;
    private Node[] nodes;

    public ProbabilisticBranch(Network network, double[] probs, Node[] nodes) {
        super(network);
        this.dist = new DiscreteSampler(probs);
        this.nodes = nodes;
    }

    public void move(T customer) {
        int next = dist.next();
        if (next == 0) {
            System.out.println("\nProbabilisticBranch: sending thread " + customer.threadId() + " to Sink!\n");
        } else {
            System.out.println("ProbabilisticBranch: sending thread " + customer.threadId() + " to " + nodes[next].name());
        }
        send(customer, nodes[next]);
    }
}