package org.jmock.models.cassandra;

import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.node.Node;

public class CassandraRouterLink extends CassandraLink {
    private Node net;
    private Node disk;

    public CassandraRouterLink(Network network, int nodeId, Node net, Node disk) {
        super(network, nodeId);
        this.net = net;
        this.disk = disk;
    }

    @Override
    public void move(CassandraCustomer customer) {
        CassandraRequestType rt = customer.requestType();
        int coordinatorNodeId = customer.coordinatorNodeId();
        int destinationNodeId = customer.destinationNodeId();
        if (rt == CassandraRequestType.READ_LOCAL) {
            // 1: read-local
            //System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + disk.name());
            send(customer, disk);
        } else if (rt == CassandraRequestType.READ_REMOTE_ID && coordinatorNodeId == this.nodeId() && destinationNodeId != this.nodeId()) {
            // 3_1: read-remote-id
            //System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + net.name());
            send(customer, net);
        } else if (rt == CassandraRequestType.READ_REMOTE_ID && coordinatorNodeId != this.nodeId() && destinationNodeId == this.nodeId()) {
            // 3_*: read-remote-id
            //System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + disk.name());
            send(customer, disk);
        } else {
            System.out.println("We have a problem in RouterLink");
            System.out.println(rt);
            System.out.println("Coordinator node = " + coordinatorNodeId);
            System.out.println("Destination node = " + destinationNodeId);
        }
    }
}