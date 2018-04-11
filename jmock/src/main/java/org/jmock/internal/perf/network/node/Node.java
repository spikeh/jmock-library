package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.link.Link;
import org.jmock.internal.perf.network.request.Customer;

public class Node<T extends Customer> {
    protected final Network<T> network;
    protected final Sim sim;
    protected final String name;

    private final int id;

    private Link<T> link = null;

    public Node(Network<T> network, Sim sim, String nodeName) {
        this.network = network;
        this.sim = sim;
        this.name = nodeName;

        this.id = network.add(this);
    }

    public String name() {
        return name;
    }

    public int id() {
        return id;
    }

    public void link(Link<T> link) {
        link.setSource(this);
        this.link = link;
    }

    public void enter(T customer) {
        customer.setLocation(this);
        customer.setNodeArrivalTime(sim.now());
        accept(customer);
    }

    protected void accept(T customer) {
        forward(customer);
    }

    protected void forward(T customer) {
        link.move(customer);
    }
}