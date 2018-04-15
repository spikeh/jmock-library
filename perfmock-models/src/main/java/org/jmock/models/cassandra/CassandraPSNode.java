package org.jmock.models.cassandra;

import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.Event;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.distribution.Deterministic;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.adt.OrderedQueue;

import java.util.HashMap;
import java.util.Map;

public class CassandraPSNode extends CassandraQueueingNode {
    private double vtime = 0.0;
    private double timeServiceBegan = 0.0;
    private Event<CassandraCustomer> nextCompletionEvent;
    private Map<CassandraRequestType, Delay> delayMap;

    private static final Map<CassandraRequestType, Delay> CL_ONE_DELAYS = new HashMap<>();
    private static final Map<CassandraRequestType, Delay> CL_QUORUM_DELAYS = new HashMap<>();
    private static final Map<CassandraRequestType, Delay> CL_ALL_DELAYS = new HashMap<>();

    static {
        CL_ONE_DELAYS.put(CassandraRequestType.READ_LOCAL, new Delay(new Deterministic(0.8028)));
        CL_ONE_DELAYS.put(CassandraRequestType.READ_REMOTE, new Delay(new Deterministic(0.374)));
        CL_ONE_DELAYS.put(CassandraRequestType.READ_REMOTE_ID, new Delay(new Deterministic(0.6309)));
        CL_ONE_DELAYS.put(CassandraRequestType.READ_LOCAL_END, new Delay(new Deterministic(0)));
        CL_ONE_DELAYS.put(CassandraRequestType.READ_REMOTE_END, new Delay(new Deterministic(0.4994)));

        CL_QUORUM_DELAYS.put(CassandraRequestType.READ_LOCAL, new Delay(new Deterministic(0.4078)));
        CL_QUORUM_DELAYS.put(CassandraRequestType.READ_REMOTE, new Delay(new Deterministic(0.5712)));
        CL_QUORUM_DELAYS.put(CassandraRequestType.READ_REMOTE_ID, new Delay(new Deterministic(0.6423)));
        CL_QUORUM_DELAYS.put(CassandraRequestType.READ_LOCAL_END, new Delay(new Deterministic(0.4968)));
        CL_QUORUM_DELAYS.put(CassandraRequestType.READ_REMOTE_END, new Delay(new Deterministic(0.8917)));

        CL_ALL_DELAYS.put(CassandraRequestType.READ_LOCAL, new Delay(new Deterministic(0.5)));
        CL_ALL_DELAYS.put(CassandraRequestType.READ_REMOTE, new Delay(new Deterministic(0.6)));
        CL_ALL_DELAYS.put(CassandraRequestType.READ_REMOTE_ID, new Delay(new Deterministic(0.6)));
        CL_ALL_DELAYS.put(CassandraRequestType.READ_LOCAL_END, new Delay(new Deterministic(0.5)));
        CL_ALL_DELAYS.put(CassandraRequestType.READ_REMOTE_END, new Delay(new Deterministic(1)));
    }

        public CassandraPSNode(Network network, Sim sim, String nodeName, int nodeId, CassandraConsistencyLevel cLevel) {
        super(network, sim, nodeName, nodeId, null, 1, new OrderedQueue<CassandraCustomer>());
        if (cLevel == CassandraConsistencyLevel.ONE) {
            delayMap = CL_ONE_DELAYS;
        } else if (cLevel == CassandraConsistencyLevel.QUORUM) {
            delayMap = CL_QUORUM_DELAYS;
        } else {
            delayMap = CL_ALL_DELAYS;
        }
    }

    @Override
    public synchronized void enter(CassandraCustomer customer) {
        CassandraRequestType rt = customer.requestType();
        Delay serviceTime = delayMap.get(rt);
        customer.setServiceDemand(serviceTime.sample());
        super.enter(customer);
    }

    @Override
    public synchronized void accept(CassandraCustomer customer) {
        if (queue.canAccept(customer)) {
            double serviceTime = customer.serviceDemand();
            if (resources.isAvailable()) {
                resources.claim();
            } else {
                sim.deschedule(nextCompletionEvent);
                double inc = (sim.now() - timeServiceBegan) / queue.size();
                vtime += inc;
            }
            double t = vtime + serviceTime;
            customer.setaTime(t);
            queue.offer(customer);
            serviceNextCustomer();
        } else {
            loseCustomer(customer);
        }
    }

    private void serviceNextCustomer() {
        CassandraCustomer customer = queue.peek();
        double completionTime = (customer.aTime() - vtime) * queue.size();
        timeServiceBegan = sim.now();
        nextCompletionEvent = new Completion(sim.now() + completionTime, customer);
        sim.schedule(nextCompletionEvent);
    }

    @Override
    public void releaseResource() {
    }

    private class Completion extends Event<CassandraCustomer> {
        public Completion(double time, CassandraCustomer customer) {
            super(time, customer);
        }

        public boolean invoke() {
            vtime += (sim.now() - timeServiceBegan) / queue.size();
            CassandraCustomer customer = queue.poll();
            if (queue.size() == 0) {
                resources.release();
                vtime = 0.0;
            } else {
                serviceNextCustomer();
            }
            customer.setServiced();
            forward(customer);
            return false;
        }
    }
}