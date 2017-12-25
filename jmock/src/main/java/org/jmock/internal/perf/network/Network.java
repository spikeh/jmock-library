package org.jmock.internal.perf.network;

import org.jmock.internal.perf.PerformanceModel;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.node.LossNode;
import org.jmock.internal.perf.network.node.Node;
import org.jmock.internal.perf.network.node.NullNode;
import org.jmock.internal.perf.network.request.Customer;

import java.util.ArrayList;

public abstract class Network<T extends Customer> implements PerformanceModel {
    protected Sim sim;
    private Node<T> nullNode;
    private Node<T> lossNode;

    private ArrayList<Node<T>> nodes = new ArrayList<>();
    private int completions = 0;
    private int losses = 0;

    public Network(Sim sim) {
        this.sim = sim;
        this.nullNode = new NullNode<>(this, sim);
        this.lossNode = new LossNode<>(this, sim);
    }

    public Node nullNode() {
        return nullNode;
    }

    public Node lossNode() {
        return lossNode;
    }

    public int add(Node<T> node) {
        nodes.add(node);
        return nodes.size() - 1;
    }

    // FIXME New added method
    public void send(T customer, Node destination) {
        nodes.get(destination.id()).enter(customer);
    }

    // called by a Sink!
    public void registerCompletion(T customer) {
        completions++;
    }

    // called by any node with a FULL queue!
    public void registerLoss(T customer) {
        losses++;
    }
}