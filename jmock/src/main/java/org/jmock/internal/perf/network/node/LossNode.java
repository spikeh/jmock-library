package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.request.Customer;

public class LossNode<T extends Customer> extends Node<T> {
    public LossNode(Network network, Sim sim) {
        this(network, sim, "Loss node");
    }

    public LossNode(Network network, Sim sim, String nodeName) {
        super(network, sim, nodeName);
    }

    @Override
    protected void accept(T customer) {
        network.registerLoss(customer);
    }
}