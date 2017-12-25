package org.jmock.internal.perf.network.request;

import org.jmock.api.Invocation;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;

import java.util.UUID;

public class CassandraCustomer extends Customer {
    private CassandraRequestType requestType;
    private int coordinatorNodeId;
    private int destinationNodeId;
    private boolean isServiced;
    private UUID uuid = UUID.randomUUID();
    private UUID associatedUuid;

    public CassandraCustomer(Network network, Sim sim, long threadId, Invocation invocation, CassandraRequestType requestType, int coordinatorNodeId) {
        super(network, sim, threadId, invocation);
        this.requestType = requestType;
        this.coordinatorNodeId = coordinatorNodeId;
        this.destinationNodeId = coordinatorNodeId;
        this.isServiced = false;
    }

    public void setRequestType(CassandraRequestType rt) {
        requestType = rt;
    }

    public CassandraRequestType requestType() {
        return requestType;
    }

    public int coordinatorNodeId() {
        return coordinatorNodeId;
    }

    public void setServiced() {
        isServiced = true;
    }

    public boolean isServiced() {
        return isServiced;
    }

    public void setDestinationNodeId(int dest) {
        destinationNodeId = dest;
    }

    public int destinationNodeId() {
        return destinationNodeId;
    }

    public UUID uuid() {
        return uuid;
    }

    public UUID associatedUuid() {
        return associatedUuid;
    }

    public void setAssociatedUuid(UUID associatedUuid) {
        this.associatedUuid = associatedUuid;
    }
}