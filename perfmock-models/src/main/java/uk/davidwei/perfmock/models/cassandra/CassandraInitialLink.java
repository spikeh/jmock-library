package uk.davidwei.perfmock.models.cassandra;

import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.node.Node;

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
            //System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + join.name());
            send(customer, join);
        } else {
            //System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + CPU.name());
            send(customer, CPU);
        }
    }
}