package org.jmock.models.tweeter;

import org.jmock.internal.perf.Event;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.node.Sink;

public class PaperTweeterSink extends Sink<TweeterCustomer> {
    private PaperTweeterModel model;

    public PaperTweeterSink(PaperTweeterModel network, Sim sim) {
        super(network, sim, "Sink");
        this.model = network;
    }

    public PaperTweeterSink(PaperTweeterModel network, Sim sim, String nodeName) {
        super(network, sim, nodeName);
        this.model = network;
    }

    @Override
    public void accept(TweeterCustomer customer) {
        sim.schedule(new ExitEvent(customer, sim.now() + customer.serviceDemand()));
    }

    private class ExitEvent extends Event<TweeterCustomer> {
        private ExitEvent(TweeterCustomer customer, double time) {
            super(time, customer);
        }

        public boolean invoke() {
            network.registerCompletion(eventObject);
            model.removeCustomer(eventObject);
            return eventObject.invocation() != null;
        }
    }
}