package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Event;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.request.Customer;

public class Sink<T extends Customer> extends Node<T> {
    public Sink(Network network, Sim sim) {
        super(network, sim, "Sink");
    }

    public Sink(Network network, Sim sim, String nodeName) {
        super(network, sim, nodeName);
    }

    @Override
    public void accept(T customer) {
        sim.schedule(new ExitEvent(customer, sim.now()));
    }

    private class ExitEvent extends Event<T> {
        private ExitEvent(T customer, double time) {
            super(time, customer);
        }

        public boolean invoke() {
            network.registerCompletion(eventObject);
            return eventObject.invocation() != null;
        }
    }
}