package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.request.Customer;

public class NullNode<T extends Customer> extends Node<T> {
    public NullNode(Network network, Sim sim) {
        super(network, sim, "Null node");
    }
}