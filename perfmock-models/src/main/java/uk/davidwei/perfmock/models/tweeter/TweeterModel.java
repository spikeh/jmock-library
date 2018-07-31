package uk.davidwei.perfmock.models.tweeter;

import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.integration.junit4.PerformanceMockery;
import uk.davidwei.perfmock.internal.perf.Param;
import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.link.Link;
import uk.davidwei.perfmock.internal.perf.network.node.Sink;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TweeterModel extends Network<TweeterCustomer> {
    private static List<String> tables = Arrays.asList("followers-all", "followers-count", "follows-all", "follows-count", "messages", "messages_empty", "users_by_username", "replies");
    private Sink<TweeterCustomer> sink;
    private Map<String, TweeterTableNode> nodeMap = new HashMap<>();
    private final List<TweeterCustomer> queuedCustomers = Collections.synchronizedList(new ArrayList<TweeterCustomer>());

    public TweeterModel() {
        super(PerformanceMockery.INSTANCE.sim());
        this.sink = new TweeterSink(this, sim);
        for (String tableName : tables) {
            TweeterTableNode node = new TweeterTableNode(this, sim, tableName);
            nodeMap.put(tableName, node);
            Link<TweeterCustomer> nodeToSink = new Link<>(this, sink);
            node.link(nodeToSink);
        }
    }

    @Override
    public void schedule(long threadId, Invocation invocation, Param param) {
        Pattern p = Pattern.compile("^([A-Za-z]+) ([A-Za-z0-9()*]+) FROM ([A-Za-z_]+)");
        String cql = (String)invocation.getParameter(0);
        Matcher m = p.matcher(cql);
        boolean find = m.find();
        if (!find) {
            System.out.println("TweeterModel: can't find matching table for schedule " + invocation.getParameter(0));
            return;
        }
        String cmd = m.group(1);
        String query = m.group(2);
        String table = m.group(3);
        Integer i = (Integer)param.getParameter("clients");
        if (i == null) {
            i = 1;
        }
        TweeterCustomer customer = null;
        if (tables.contains(table)) {
            TweeterTableNode node = nodeMap.get(table);
            customer = new TweeterCustomer(this, sim, Thread.currentThread().getId(), invocation, cmd, query, table, i);
            node.enter(customer);
        } else if (table.equals("followers") || table.equals("follows")) {
            customer = new TweeterCustomer(this, sim, Thread.currentThread().getId(), invocation, cmd, query, table, i);
            if (cmd.equals("*")) {
                String appendedTable = table + "-all";
                TweeterTableNode node = nodeMap.get(appendedTable);
                node.enter(customer);
            } else if (cmd.startsWith("count")) {
                String appendedTable = table + "-count";
                TweeterTableNode node = nodeMap.get(appendedTable);
                node.enter(customer);
            }
        } else {
            System.out.println("TweeterModel: unknown table: " + table);
        }
        if (customer != null) {
            queuedCustomers.add(customer);
        }
    }

    int getRequests(double arrivalTime, double invokeTime) {
        synchronized (queuedCustomers) {
            if (arrivalTime != invokeTime) {
                System.out.println("TweeterModel::getRequests arrivalTime = " + arrivalTime + ", invokeTime = " + invokeTime);
            }
            /*
            int count = 0;
            for (TweeterCustomer c : queuedCustomers) {
                // TODO check this inequality
                if (c.arrivalTime() >= arrivalTime) {
                    count += 1;
                }
            }
            return count;
            */
            return queuedCustomers.size();
        }
    }

    void removeCustomer(TweeterCustomer customer) {
        if (!queuedCustomers.contains(customer)) {
            System.out.println("TweeterModel: can't remove customer " + customer);
        }
        queuedCustomers.remove(customer);
    }

    public static Param param(int clients) {
        Param ret = new Param();
        ret.addParameter("clients", clients);
        return ret;
    }
}