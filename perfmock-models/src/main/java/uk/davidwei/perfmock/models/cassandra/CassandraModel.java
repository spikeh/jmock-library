package uk.davidwei.perfmock.models.cassandra;

import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.integration.junit4.PerformanceMockery;
import uk.davidwei.perfmock.internal.perf.Delay;
import uk.davidwei.perfmock.internal.perf.Param;
import uk.davidwei.perfmock.internal.perf.distribution.Deterministic;
import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.adt.FIFOQueue;
import uk.davidwei.perfmock.internal.perf.network.link.Link;
import uk.davidwei.perfmock.internal.perf.network.node.Node;
import uk.davidwei.perfmock.internal.perf.network.node.Sink;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class CassandraModel extends Network {
    private final List<CassandraNode> networkList = new ArrayList<CassandraNode>();
    private final List<CassandraNode> switchList = new ArrayList<CassandraNode>();
    private final Random rng = new Random();

    private final CassandraConsistencyLevel consistencyLevel;
    private final Sink exit;
    private final Node<CassandraCustomer> loadBalancer;
    private final int replicationFactor;
    private final int numContendedRequests;
    private int[] map = {1, 2, 3, 4};
    private AtomicInteger next = new AtomicInteger(0);

    public CassandraModel(CassandraConsistencyLevel consistencyLevel, int replicationFactor) {
        this(consistencyLevel, replicationFactor, 8);
    }

    public CassandraModel(CassandraConsistencyLevel consistencyLevel, int replicationFactor, int numContendedRequests) {
        super(PerformanceMockery.INSTANCE.sim());
        this.consistencyLevel = consistencyLevel;
        this.replicationFactor = replicationFactor;
        this.numContendedRequests = numContendedRequests;

        this.loadBalancer = new Node<>(this, sim, "loadBalancer");
        Link<CassandraCustomer> lbLink = new CassandraLBLink(this, networkList);
        this.loadBalancer.link(lbLink);

        this.exit = new Sink(this, sim);

        createNode(1);
        createNode(2);
        createNode(3);
        createNode(4);
    }

    public static Param param(int rows) {
        Param ret = new Param();
        ret.addParameter("rows", rows);
        return ret;
    }

    private void createNode(int nodeId) {
        CassandraQueueingNode c_network = new CassandraNetworkNode(this, sim, String.format("c%d_network", nodeId), nodeId, 1, new FIFOQueue<CassandraCustomer>());
        networkList.add(c_network);

        CassandraPSNode c_CPU = new CassandraPSNode(this, sim, String.format("c%d_CPU", nodeId), nodeId, consistencyLevel);
        CassandraQueueingNode c_disk = new CassandraQueueingNode(this, sim, String.format("c%d_disk", nodeId), nodeId, new Delay(new Deterministic(0.0679)), 1, new FIFOQueue<CassandraCustomer>());

        CassandraRouter c_router = new CassandraRouter(this, sim, String.format("c%d_router", nodeId), nodeId);
        CassandraJoin c_join = new CassandraJoin(this, sim, String.format("c%d_join", nodeId), nodeId, consistencyLevel);
        CassandraFork c_fork = new CassandraFork(this, sim, String.format("c%d_fork", nodeId), nodeId, consistencyLevel, replicationFactor, c_join, networkList);

        CassandraNode c_initial = new CassandraInitialSwitch(this, sim, String.format("c%d_initial", nodeId), nodeId);
        switchList.add(c_initial);

        CassandraNode c_end = new CassandraEndSwitch(this, sim, String.format("c%d_end", nodeId), nodeId);

        Link<CassandraCustomer> c_net_link = new CassandraNetLink(this, nodeId, exit, c_initial, switchList);
        c_network.link(c_net_link);

        Link<CassandraCustomer> c_initial_link = new CassandraInitialLink(this, nodeId, c_join, c_CPU);
        c_initial.link(c_initial_link);

        Link<CassandraCustomer> c_CPU_link = new CassandraCPULink(this, nodeId, c_network, c_fork);
        c_CPU.link(c_CPU_link);

        Link<CassandraCustomer> c_fork_link = new CassandraLink(this, nodeId, c_router);
        c_fork.link(c_fork_link);

        Link<CassandraCustomer> c_router_link = new CassandraRouterLink(this, nodeId, c_network, c_disk);
        c_router.link(c_router_link);

        Link<CassandraCustomer> c_disk_link = new CassandraLink(this, nodeId, c_join);
        c_disk.link(c_disk_link);

        Link<CassandraCustomer> c_join_link = new CassandraJoinLink(this, nodeId, c_network, c_end);
        c_join.link(c_join_link);

        Link<CassandraCustomer> c_end_link = new CassandraLink(this, nodeId, c_initial);
        c_end.link(c_end_link);
    }

    public void schedule(long threadId, Invocation invocation, Param param) {
        CassandraRequestType rt;
        Integer foo = (Integer)param.getParameter("rows");
        if (foo != null) {
            System.out.println("Rows = " + foo);
        }

        /*
        int mid = rng.nextInt(this.numContendedRequests + 1);
        System.out.println("Mid point = " + (mid + 1));
        for (int i = 0; i < mid; i++) {
            rt = randomRequestType();
            CassandraCustomer c = new CassandraCustomer(this, sim, threadId, null, rt, next + 1);
            next = (next + 1) % networkList.size();
            loadBalancer.enter(c);
        }
        */

        rt = CassandraRequestType.READ_LOCAL;
        int nextNode = next.getAndIncrement();
        CassandraCustomer customer = new CassandraCustomer(this, sim, threadId, invocation, rt, map[nextNode % networkList.size()]);
        loadBalancer.enter(customer);

        /*
        for (int i = (mid + 1); i < (this.numContendedRequests + 1); i++) {
            rt = randomRequestType();
            CassandraCustomer c = new CassandraCustomer(this, sim, threadId, null, rt, next + 1);
            next = (next + 1) % networkList.size();
            loadBalancer.enter(c);
        }
        */
    }

    private CassandraRequestType randomRequestType() {
        // probability of it being a LOCAL or READ...
        // cluster size / replication factor
        CassandraRequestType rt;
        double rngValue = rng.nextDouble();
        if (rngValue <= (float)replicationFactor / networkList.size()) {
            rt = CassandraRequestType.READ_LOCAL;
        } else {
            rt = CassandraRequestType.READ_REMOTE;
        }
        return rt;
    }
}