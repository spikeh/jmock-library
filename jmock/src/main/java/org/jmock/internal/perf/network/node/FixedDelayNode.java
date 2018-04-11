package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Event;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.request.Customer;

public class FixedDelayNode<T extends Customer> extends Node<T> {
    private final double serviceTime;

    public FixedDelayNode(Network network, Sim sim, String nodeName, double delay) {
        super(network, sim, nodeName);
        this.serviceTime = delay;
    }

    @Override
    public synchronized void enter(T customer) {
        customer.setServiceDemand(serviceTime);
        super.enter(customer);
    }

    @Override
    public void accept(T customer) {
        scheduleEvent(customer);
    }

    protected void scheduleEvent(T customer) {
        sim.schedule(new EndServiceEvent(customer, sim.now() + serviceTime));
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