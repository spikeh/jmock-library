package uk.davidwei.perfmock.models.cassandra;

import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.link.Link;
import uk.davidwei.perfmock.internal.perf.network.node.Node;

public class CassandraLink extends Link<CassandraCustomer> {
    private final int nodeId;

    public CassandraLink(Network network, int nodeId) {
        super(network);
        this.nodeId = nodeId;
    }

    public CassandraLink(Network network, int nodeId, Node dst) {
        super(network, dst);
        this.nodeId = nodeId;
    }

    public int nodeId() {
        return nodeId;
    }

    @Override
    public void move(CassandraCustomer customer) {
        //System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + dst.name());
        send(customer, dst);
    }
}