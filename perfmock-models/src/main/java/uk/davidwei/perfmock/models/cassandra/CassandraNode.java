package uk.davidwei.perfmock.models.cassandra;

import uk.davidwei.perfmock.internal.perf.Sim;
import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.node.Node;

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