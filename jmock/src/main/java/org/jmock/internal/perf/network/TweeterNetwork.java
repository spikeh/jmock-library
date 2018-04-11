package org.jmock.internal.perf.network;

import org.jmock.api.Invocation;
import org.jmock.internal.perf.Param;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.link.Link;
import org.jmock.internal.perf.network.node.Node;
import org.jmock.internal.perf.network.node.Sink;
import org.jmock.internal.perf.network.node.TweeterTableNode;
import org.jmock.internal.perf.network.request.TweeterCustomer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TweeterNetwork extends Network<TweeterCustomer> {
    private static List<String> tables = Arrays.asList("followers-all", "followers-count", "follows-all", "follows-count", "messages", "messages_empty", "users_by_username", "replies");
    private Sink<TweeterCustomer> sink;
    private Map<String, Node<TweeterCustomer>> nodeMap = new HashMap<>();

    public TweeterNetwork(Sim sim) throws IOException {
        super(sim);
        this.sink = new Sink<>(this, sim);
        for (String tableName : tables) {
            TweeterTableNode node = new TweeterTableNode(this, sim, tableName);
            nodeMap.put(tableName, node);
            Link<TweeterCustomer> nodeToSink = new Link<>(this, sink);
            node.link(nodeToSink);
        }
    }

    @Override
    public void query(long threadId, Invocation invocation, Param param) {
        Pattern p = Pattern.compile("^([A-Za-z]+) ([A-Za-z0-9()*]+) FROM ([A-Za-z_]+)");
        String cql = (String)invocation.getParameter(0);
        Matcher m = p.matcher(cql);
        boolean find = m.find();
        assert(find);
        String cmd = m.group(1);
        String query = m.group(2);
        String table = m.group(3);
        Integer i = (Integer)param.getParameter("clients");
        if (i == null) {
            i = 1;
        }
        TweeterCustomer customer = new TweeterCustomer(this, sim, Thread.currentThread().getId(), invocation, cmd, query, table, i);
        if (tables.contains(table)) {
            Node<TweeterCustomer> node = nodeMap.get(table);
            node.enter(customer);
        } else if (table.equals("followers") || table.equals("follows")) {
            if (cmd.equals("*")) {
                String appendedTable = table + "-all";
                Node<TweeterCustomer> node = nodeMap.get(appendedTable);
                node.enter(customer);
            } else if (cmd.startsWith("count")) {
                String appendedTable = table + "-count";
                Node<TweeterCustomer> node = nodeMap.get(appendedTable);
                node.enter(customer);
            }
        }
    }

    public static Param param(int clients) {
        Param ret = new Param();
        ret.addParameter("clients", clients);
        return ret;
    }
}