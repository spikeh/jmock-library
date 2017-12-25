package org.jmock.internal.perf.network.link;

import org.jmock.internal.perf.network.request.CassandraCustomer;
import org.jmock.internal.perf.network.request.CassandraRequestType;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.node.CassandraNode;
import org.jmock.internal.perf.network.node.Node;

import java.util.List;

public class CassandraNetLink extends CassandraLink {
    private final List<CassandraNode> switchList;
    private Node exit;
    private Node initial;

    public CassandraNetLink(Network network, int nodeId, Node exit, Node initial, List<CassandraNode> switchList) {
        super(network, nodeId);
        this.exit = exit;
        this.initial = initial;
        this.switchList = switchList;
    }

    @Override
    public void move(CassandraCustomer customer) {
        CassandraRequestType rt = customer.requestType();
        if ((rt == CassandraRequestType.READ_LOCAL_END && customer.isServiced())
                || (rt == CassandraRequestType.READ_REMOTE_END && customer.isServiced())) {
            // 4p: read-local-end
            // 5p: read-remote-end
            System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + exit.name());
            send(customer, exit);
        } else if (rt == CassandraRequestType.READ_LOCAL
                || rt == CassandraRequestType.READ_REMOTE) {
            // 1: read-local
            // 2: read-remote
            System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + initial.name());
            send(customer, initial);
        } else if (rt == CassandraRequestType.READ_REMOTE_ID) {
            int customerNodeId = customer.coordinatorNodeId();
            if (customerNodeId == this.nodeId()) {
                int destinationNodeId = customer.destinationNodeId();
                Node dest = switchList.get(destinationNodeId - 1);
                System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + dest.name());
                send(customer, dest);
            } else {
                int destinationNodeId = customer.coordinatorNodeId();
                Node dest = switchList.get(destinationNodeId - 1);
                System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + dest.name());
                send(customer, dest);
            }
        }
    }
}