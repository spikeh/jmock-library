package org.jmock.models.cassandra;

import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;

import java.util.HashMap;
import java.util.Map;

public class CassandraEndSwitch extends CassandraNode {
    private static final Map<Integer, Integer> typeMap = new HashMap<>();

    public CassandraEndSwitch(Network network, Sim sim, String nodeName, int nodeId) {
        super(network, sim, nodeName, nodeId);
    }

    @Override
    public void accept(CassandraCustomer customer) {
        CassandraRequestType rt = customer.requestType();
        if (rt == CassandraRequestType.READ_LOCAL) {
            //System.out.println(customer.uuid() + " " + customer.requestType() + " changed to READ_LOCAL_END");
            customer.setRequestType(CassandraRequestType.READ_LOCAL_END);
            // TODO check isServiced here?
        }
        forward(customer);
    }
}