package uk.davidwei.perfmock.models.cassandra;

import uk.davidwei.perfmock.internal.perf.Sim;
import uk.davidwei.perfmock.internal.perf.network.Network;

public class CassandraRouter extends CassandraNode {
    public CassandraRouter(Network network, Sim sim, String nodeName, int nodeId) {
        super(network, sim, nodeName, nodeId);
    }
}