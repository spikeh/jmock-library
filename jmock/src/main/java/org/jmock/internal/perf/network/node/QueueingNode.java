package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.Event;
import org.jmock.internal.perf.Resource;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.adt.CappedQueue;
import org.jmock.internal.perf.network.adt.FIFOQueue;
import org.jmock.internal.perf.network.request.Customer;

/**
 *
 */
public class QueueingNode<T extends Customer> extends Node<T> {
    protected final Delay serviceTime;
    protected final int maxResources;
    protected final CappedQueue<T> queue;
    protected final Resource resources;

    public QueueingNode(Network network, Sim sim, String nodeName, Delay delay) {
        this(network, sim, nodeName, delay, 1);
    }

    public QueueingNode(Network network, Sim sim, String nodeName, Delay delay, int maxRes) {
        this(network, sim, nodeName, delay, maxRes, new FIFOQueue<T>());
    }

    public QueueingNode(Network network, Sim sim, String nodeName, Delay delay, int maxRes, CappedQueue<T> queue) {
        super(network, sim, nodeName);
        this.serviceTime = delay;
        this.maxResources = maxRes;
        this.queue = queue;
        this.resources = new Resource(sim, maxRes);
    }

    @Override
    public synchronized void enter(T customer) {
        customer.setServiceDemand(serviceTime.sample());
        super.enter(customer);
    }

    @Override
    public void accept(T customer) {
        if (resources.isAvailable()) {
            resources.claim();
            scheduleEvent(customer);
        } else {
            if (!queue.offer(customer)) {
                loseCustomer(customer);
            }
        }
    }

    protected void loseCustomer(T customer) {
        network.lossNode().enter(customer);
    }

    @Override
    public void forward(T customer) {
        super.forward(customer);
        releaseResource();
    }

    protected void scheduleEvent(T customer) {
        sim.schedule(new EndServiceEvent(customer, sim.now() + customer.serviceDemand()));
    }

    protected void releaseResource() {
        if (!queue.isEmpty()) {
            T next = queue.poll();
            scheduleEvent(next);
        } else {
            resources.release();
        }
    }

    private class EndServiceEvent extends Event<T> {
        private EndServiceEvent(T customer, double time) {
            super(time, customer);
        }

        public boolean invoke() {
            forward(eventObject);
            return false;
        }
    }
}