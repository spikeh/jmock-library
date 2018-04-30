package org.jmock.models.tweeter;

import org.jmock.api.Invocation;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.request.Customer;

public class TweeterCustomer extends Customer {
    private String cmd;
    private String query;
    private String table;
    private Integer clients;

    public TweeterCustomer(Network network, Sim sim, long threadId, Invocation invocation, String cmd, String query, String table, Integer clients) {
        super(network, sim, threadId, invocation);
        this.cmd = cmd;
        this.query = query;
        this.table = table;
        this.clients = clients;
    }

    public String getCmd() {
        return cmd;
    }

    public String getQuery() {
        return query;
    }

    public String getTable() {
        return table;
    }

    public void setClients(Integer clients) {
        this.clients = clients;
    }

    public Integer getClients() {
        return clients;
    }
}