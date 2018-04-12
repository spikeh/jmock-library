package org.jmock.internal.perf.network.link;

import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.node.Node;
import org.jmock.internal.perf.network.request.Customer;

public class Link<T extends Customer> {
    private final Network network;
    protected Node dst;
    protected Node src;

    public Link(Network network) {
        this.network = network;
        this.dst = network.nullNode();
    }

    public Link(Network network, Node dst) {
        this.network = network;
        this.dst = dst;
    }

    public void setSource(Node src) {
        this.src = src;
    }

    public void send(T customer, Node destination) {
        network.send(customer, destination);
    }

    public void move(T customer) {
        send(customer, dst);
    }
}