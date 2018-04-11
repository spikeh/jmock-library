package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.request.CassandraCustomer;

public class CassandraNode extends Node<CassandraCustomer> {
    private final int nodeId;

    public CassandraNode(Network network, Sim sim, String nodeName, int nodeId) {
        super(network, sim, nodeName);
        this.nodeId = nodeId;
    }

    public int nodeId() {
        return nodeId;
    }
}