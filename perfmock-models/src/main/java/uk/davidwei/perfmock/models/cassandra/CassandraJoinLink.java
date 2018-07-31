package uk.davidwei.perfmock.models.cassandra;

import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.node.Node;

public class CassandraJoinLink extends CassandraLink {
    private final Node net;
    private final Node end;

    public CassandraJoinLink(Network network, int nodeId, Node net, Node end) {
        super(network, nodeId);
        this.net = net;
        this.end = end;
    }

    @Override
    public void move(CassandraCustomer customer) {
        CassandraRequestType rt = customer.requestType();
        int coordinatorNodeId = customer.coordinatorNodeId();
        if (rt == CassandraRequestType.READ_REMOTE_ID && coordinatorNodeId != this.nodeId()) {
            //System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + net.name());
            send(customer, net);
        } else if (rt == CassandraRequestType.READ_REMOTE_END) {
            //System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + end.name());
            send(customer, end);
        } else if (rt == CassandraRequestType.READ_LOCAL) {
            //System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + end.name());
            send(customer, end);
        }
    }
}