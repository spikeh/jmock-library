package uk.davidwei.perfmock.models.cassandra;

import uk.davidwei.perfmock.internal.perf.Sim;
import uk.davidwei.perfmock.internal.perf.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static uk.davidwei.perfmock.models.cassandra.CassandraRequestType.READ_LOCAL;
import static uk.davidwei.perfmock.models.cassandra.CassandraRequestType.READ_REMOTE_ID;

public class CassandraFork extends CassandraNode {
    private final CassandraConsistencyLevel cLevel;
    private final CassandraJoin join;
    private final List<CassandraNode> networkList;
    private final Random rng;
    private final int replicationFactor;

    public CassandraFork(Network network, Sim sim, String nodeName, int nodeId, CassandraConsistencyLevel cLevel, int replicationFactor, CassandraJoin join, List<CassandraNode> networkList) {
        super(network, sim, nodeName, nodeId);
        this.cLevel = cLevel;
        this.join = join;
        this.replicationFactor = replicationFactor;
        this.networkList = networkList;
        this.rng = new Random();
    }

    private void sendReadRemoteIdCustomers(CassandraCustomer customer, int numNodes) {
        join.setExpected(customer.uuid(), numNodes);
        //System.out.println("<!> " + name + " " + customer.uuid() + " sending " + numNodes + " READ_REMOTE_ID messages");
        List<Integer> sent = new ArrayList<>();
        while (sent.size() < numNodes) {
            int dest;
            do {
                dest = rng.nextInt(networkList.size()) + 1;
            } while (dest == this.nodeId() || sent.contains(dest));
            sent.add(dest);
            CassandraCustomer newCustomer = new CassandraCustomer(network, sim, customer.threadId(), customer.invocation(), READ_REMOTE_ID, this.nodeId());
            newCustomer.setDestinationNodeId(dest);
            newCustomer.setAssociatedUuid(customer.uuid());
            super.accept(newCustomer);
        }
    }
    
    private void sendAllReadRemoteIdCustomers(CassandraCustomer customer, int numNodes) {
        join.setExpected(customer.uuid(), numNodes);
        //System.out.println("<!> " + name + " " + customer.uuid() + " sending " + numNodes + " READ_REMOTE_ID messages");
        for (CassandraNode node : networkList) {
            // make READ_REMOTE_ID here...
            if (node.nodeId() != this.nodeId()) {
                CassandraCustomer newCustomer = new CassandraCustomer(network, sim, customer.threadId(), customer.invocation(), READ_REMOTE_ID, this.nodeId());
                newCustomer.setDestinationNodeId(node.nodeId());
                newCustomer.setAssociatedUuid(customer.uuid());
                // send to ROUTER which then sends to our net which then sends to node's initial
                super.accept(newCustomer);
            }
        }
    }

    private boolean isReadRemote(CassandraRequestType rt, int coordinatorNodeId, int destinationNodeId) {
        return rt == READ_REMOTE_ID && coordinatorNodeId == this.nodeId() && destinationNodeId == this.nodeId();
    }

    protected void accept(CassandraCustomer customer) {
        CassandraRequestType rt = customer.requestType();
        if (cLevel == CassandraConsistencyLevel.QUORUM) {
            int numNodes;
            if (rt == READ_LOCAL) {
                // notify join node that we're waiting for (replicationFactor / 2) responses, rounded down...
                numNodes = replicationFactor / 2;
                sendReadRemoteIdCustomers(customer, numNodes);
                super.accept(customer);
            } else if (isReadRemote(rt, customer.coordinatorNodeId(), customer.destinationNodeId())) {
                // rt == CassandraRequestType.READ_REMOTE
                // notify join node that we're waiting for (replicationFactor / 2) + 1 responses, rounded down...
                numNodes = (replicationFactor / 2 ) + 1;
                sendReadRemoteIdCustomers(customer, numNodes);
            } else {
                super.accept(customer);
            }
        } else if (cLevel == CassandraConsistencyLevel.ALL) {
            // always equal to the replication factor
            if (rt == READ_LOCAL) {
                // notify join node that we're waiting for replicationFactor - 1 responses...
                int numNodes = replicationFactor - 1;
                if (replicationFactor == networkList.size()) {
                    sendAllReadRemoteIdCustomers(customer, numNodes);
                } else {
                    sendReadRemoteIdCustomers(customer, numNodes);
                }
                super.accept(customer);
            } else if (isReadRemote(rt, customer.coordinatorNodeId(), customer.destinationNodeId())) {
                // rt == CassandraRequestType.READ_REMOTE
                int numNodes = replicationFactor;
                // replicationFactor CANNOT equal networkList.size() here, else it wouldn't be a READ_REMOTE request as every node has 100% of the data
                assert(replicationFactor != networkList.size());
                if (replicationFactor == networkList.size() - 1) {
                    sendAllReadRemoteIdCustomers(customer, numNodes);
                } else {
                    sendReadRemoteIdCustomers(customer, numNodes);
                }
            } else {
                super.accept(customer);
            }
        } else if (isReadRemote(rt, customer.coordinatorNodeId(), customer.destinationNodeId())) {
            // cLevel == CassandraConsistencyLevel.ONE
            // 3: read-remote-id
            // make READ_REMOTE_ID here...
            sendReadRemoteIdCustomers(customer, 1);
        } else {
            super.accept(customer);
        }
    }
}