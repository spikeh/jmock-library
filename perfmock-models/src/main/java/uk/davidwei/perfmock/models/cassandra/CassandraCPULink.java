package uk.davidwei.perfmock.models.cassandra;

import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.node.Node;

public class CassandraCPULink extends CassandraLink {
    private Node net;
    private Node fork;

    public CassandraCPULink(Network network, int nodeId, Node net, Node fork) {
        super(network, nodeId);
        this.net = net;
        this.fork = fork;
    }

    @Override
    public void move(CassandraCustomer customer) {
        CassandraRequestType rt = customer.requestType();
        // should NEVER be a READ_REMOTE here
        if (rt == CassandraRequestType.READ_LOCAL_END || rt == CassandraRequestType.READ_REMOTE_END) {
            // 4: read-local-end
            // 5: read-remote-end
            //System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + net.name());
            send(customer, net);
        } else {
            //System.out.println(customer.uuid() + " " + customer.requestType() + " sent to " + fork.name());
            send(customer, fork);
        }
    }
}