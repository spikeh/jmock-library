package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.request.CassandraConsistencyLevel;
import org.jmock.internal.perf.network.request.CassandraCustomer;
import org.jmock.internal.perf.network.request.CassandraRequestType;
import org.jmock.internal.perf.network.Network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CassandraJoin extends CassandraNode {

    private Map<UUID, Integer> expectedResponses = new HashMap<>();
    private Map<UUID, CassandraCustomer> waitingCustomers = new HashMap<>();
    private final CassandraConsistencyLevel consistencyLevel;

    public CassandraJoin(Network network, Sim sim, String nodeName, int nodeId, CassandraConsistencyLevel consistencyLevel) {
        super(network, sim, nodeName, nodeId);
        this.consistencyLevel = consistencyLevel;
    }

    public void setExpected(UUID customerUuid, Integer totalNodes) {
        if (!expectedResponses.containsKey(customerUuid)) {
            expectedResponses.put(customerUuid, totalNodes);
        }
    }

    @Override
    protected void accept(CassandraCustomer customer) {
        CassandraRequestType rt = customer.requestType();
        int coordinatorNodeId = customer.coordinatorNodeId();
        int destinationNodeId = customer.destinationNodeId();
        if (consistencyLevel != CassandraConsistencyLevel.ONE) {
            // QUORUM or ALL
            if (rt == CassandraRequestType.READ_REMOTE_RETURN && coordinatorNodeId == this.nodeId() && destinationNodeId != this.nodeId()) {
                // reply from other nodes
                UUID uuid = customer.associatedUuid();
                Integer remainingExpectedResponses = expectedResponses.get(uuid);
                if (remainingExpectedResponses - 1 == 0) {
                    // okay, we've received all the required responses, forward the original customer on
                    expectedResponses.remove(uuid);
                    CassandraCustomer origCustomer = waitingCustomers.remove(uuid);
                    System.out.println("<!> All responses received, forwarding customer " + customer.threadId() + " " + customer.uuid());
                    if (origCustomer != null) {
                        // in the case of: READ_LOCAL with QUORUM or ALL
                        forward(origCustomer);
                    } else {
                        // in the case of: READ_REMOTE with QUORUM or ALL
                        CassandraCustomer newCustomer = new CassandraCustomer(network, sim, customer.threadId(), customer.invocation(), CassandraRequestType.READ_REMOTE_END, coordinatorNodeId);
                        forward(newCustomer);
                    }
                } else {
                    // decrement the number of waiting responses
                    System.out.println("<!> Received 1 READ_REMOTE_ID response");
                    expectedResponses.put(uuid, remainingExpectedResponses - 1);
                }
            } else if (rt == CassandraRequestType.READ_LOCAL && expectedResponses.containsKey(customer.uuid())) {
                // queue the customer, wait for replies
                System.out.println(customer.uuid() + " " + customer.requestType() + " being queued in " + name);
                waitingCustomers.put(customer.uuid(), customer);
            } else {
                forward(customer);
            }
        } else {
            if (rt == CassandraRequestType.READ_REMOTE_RETURN && coordinatorNodeId == this.nodeId() && destinationNodeId != this.nodeId()) {
                // this HAS to be READ_REMOTE with ONE
                if (expectedResponses.containsKey(customer.associatedUuid())) {
                    expectedResponses.remove(customer.associatedUuid());
                    // make NEW customer here...
                    CassandraCustomer newCustomer = new CassandraCustomer(network, sim, customer.threadId(), customer.invocation(), CassandraRequestType.READ_REMOTE_END, coordinatorNodeId);
                    forward(newCustomer);
                }
            } else {
                // this is thus READ_LOCAL with ONE
                forward(customer);
            }
        }
    }
}