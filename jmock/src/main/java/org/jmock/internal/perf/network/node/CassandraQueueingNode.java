package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.Event;
import org.jmock.internal.perf.Resource;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.adt.CappedQueue;
import org.jmock.internal.perf.network.request.CassandraCustomer;

public class CassandraQueueingNode extends CassandraNode {
    protected final Delay serviceTime;
    protected final int maxResources;
    protected final CappedQueue<CassandraCustomer> queue;
    protected final Resource resources;

    private int losses = 0;

    public CassandraQueueingNode(Network network, Sim sim, String nodeName, int nodeId, Delay delay, int maxRes, CappedQueue<CassandraCustomer> queue) {
        super(network, sim, nodeName, nodeId);
        this.serviceTime = delay;
        this.maxResources = maxRes;
        this.queue = queue;
        this.resources = new Resource(sim, maxRes);
    }

    @Override
    public synchronized void enter(CassandraCustomer customer) {
        if (serviceTime != null) {
            customer.setServiceDemand(serviceTime.sample());
        }
        super.enter(customer);
    }

    @Override
    public void accept(CassandraCustomer customer) {
        if (resources.isAvailable()) {
            resources.claim();
            System.out.println(name + " accept() customer.serviceDemand() = " + customer.serviceDemand());
            scheduleEvent(customer);
        } else {
            if (!queue.offer(customer)) {
                loseCustomer(customer);
            }
        }
    }

    protected void loseCustomer(CassandraCustomer customer) {
        losses++;
        network.lossNode().enter(customer);
    }

    @Override
    public void forward(CassandraCustomer customer) {
        super.forward(customer);
        releaseResource();
    }

    protected void scheduleEvent(CassandraCustomer customer) {
        sim.schedule(new EndServiceEvent(sim.now() + customer.serviceDemand(), customer));
    }

    protected void releaseResource() {
        if (!queue.isEmpty()) {
            CassandraCustomer next = queue.poll();
            System.out.println(name + " releaseResource() customer.serviceDemand() = " + next.serviceDemand());
            scheduleEvent(next);
        } else {
            resources.release();
        }
    }

    private class EndServiceEvent extends Event<CassandraCustomer> {
        private EndServiceEvent(double time, CassandraCustomer customer) {
            super(time, customer);
        }

        public boolean invoke() {
            forward(eventObject);
            return false;
        }
    }
}