package org.jmock.models.cassandra;

import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.distribution.Deterministic;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.adt.CappedQueue;

public class CassandraNetworkNode extends CassandraQueueingNode {
    private final Delay networkRequestDelay = new Delay(new Deterministic(0.01333));
    private final Delay networkDataDelay = new Delay(new Deterministic(0.1333));

    public CassandraNetworkNode(Network network, Sim sim, String nodeName, int nodeId, int maxRes, CappedQueue<CassandraCustomer> queue) {
        super(network, sim, nodeName, nodeId, null, maxRes, queue);
    }

    @Override
    public synchronized void enter(CassandraCustomer customer) {
        // 0 = Network Request
        // 1 = Network Data
        // TODO when is this ever changed?
        int networkRequestType = customer.priority();
        if (networkRequestType == 0) {
            customer.setServiceDemand(networkRequestDelay.sample());
        } else if (networkRequestType == 1) {
            customer.setServiceDemand(networkDataDelay.sample());
        }
        super.enter(customer);
    }
}