package org.jmock.internal.perf.network.link;

import org.jmock.internal.perf.network.request.CassandraCustomer;
import org.jmock.internal.perf.network.request.CassandraRequestType;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.node.Node;

public class CassandraInitialLink extends CassandraLink {
    private Node join;
    private Node CPU;

    public CassandraInitialLink(Network network, int nodeId, Node join, Node CPU) {
        super(network, nodeId);
        this.join = join;
        this.CPU = CPU;
    }

    @Override
    public void move(CassandraCustomer customer) {
        CassandraRequestType rt = customer.requestType();
        if (rt == CassandraRequestType.READ_REMOTE_RETURN) {
            System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + join.name());
            send(customer, join);
        } else {
            System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + CPU.name());
            send(customer, CPU);
        }
    }
}