package uk.davidwei.perfmock.internal.perf.network.node;

import uk.davidwei.perfmock.internal.perf.Delay;
import uk.davidwei.perfmock.internal.perf.Event;
import uk.davidwei.perfmock.internal.perf.Sim;
import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.request.Customer;

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