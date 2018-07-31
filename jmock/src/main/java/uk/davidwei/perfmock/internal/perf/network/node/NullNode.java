package uk.davidwei.perfmock.internal.perf.network.node;

import uk.davidwei.perfmock.internal.perf.Sim;
import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.request.Customer;

public class NullNode<T extends Customer> extends Node<T> {
    public NullNode(Network network, Sim sim) {
        super(network, sim, "Null node");
    }
}