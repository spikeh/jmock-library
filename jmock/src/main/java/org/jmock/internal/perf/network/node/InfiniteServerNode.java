package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Event;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.request.Customer;
import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.network.Network;

public class InfiniteServerNode<T extends Customer> extends Node<T> {
    private final Delay serviceTime;

    public InfiniteServerNode(Network network, Sim sim, String nodeName, Delay delay) {
        super(network, sim, nodeName);
        this.serviceTime = delay;
    }

    @Override
    public synchronized void enter(T customer) {
        customer.setServiceDemand(serviceTime.sample());
        super.enter(customer);
    }

    @Override
    public void accept(T customer) {
        scheduleEvent(customer);
    }

    protected void scheduleEvent(T customer) {
        sim.schedule(new EndServiceEvent(customer, sim.now() + customer.serviceDemand()));
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