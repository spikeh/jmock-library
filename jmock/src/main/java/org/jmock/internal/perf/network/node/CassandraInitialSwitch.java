package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.request.CassandraCustomer;
import org.jmock.internal.perf.network.request.CassandraRequestType;

public class CassandraInitialSwitch extends CassandraNode {
    public CassandraInitialSwitch(Network network, Sim sim, String nodeName, int nodeId) {
        super(network, sim, nodeName, nodeId);
    }

    @Override
    public void accept(CassandraCustomer customer) {
        CassandraRequestType rt = customer.requestType();
        if (rt == CassandraRequestType.READ_REMOTE) {
            // read-remote --> read-remote-id
            // 2           --> 3_i
            System.out.println(customer.uuid() + " " + customer.requestType() + " changed to READ_REMOTE_ID");
            customer.setRequestType(CassandraRequestType.READ_REMOTE_ID);
        } else if (rt == CassandraRequestType.READ_REMOTE_ID && customer.isServiced() && customer.coordinatorNodeId() == this.nodeId()) {
            // read-remote-id --> read-remote-return
            // 3p_i           --> 6
            System.out.println(customer.uuid() + " " + customer.requestType() + " changed to READ_REMOTE_RETURN");
            customer.setRequestType(CassandraRequestType.READ_REMOTE_RETURN);
        }
        forward(customer);
    }
}