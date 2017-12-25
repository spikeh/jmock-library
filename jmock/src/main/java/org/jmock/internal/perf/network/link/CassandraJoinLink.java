package org.jmock.internal.perf.network.link;

import org.jmock.internal.perf.network.request.CassandraCustomer;
import org.jmock.internal.perf.network.request.CassandraRequestType;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.node.Node;

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
            System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + net.name());
            send(customer, net);
        } else if (rt == CassandraRequestType.READ_REMOTE_END) {
            System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + end.name());
            send(customer, end);
        } else if (rt == CassandraRequestType.READ_LOCAL) {
            System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + end.name());
            send(customer, end);
        }
    }
}