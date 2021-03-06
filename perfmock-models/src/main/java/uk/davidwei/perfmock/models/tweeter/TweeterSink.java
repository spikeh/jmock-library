package uk.davidwei.perfmock.models.tweeter;

import uk.davidwei.perfmock.internal.perf.Event;
import uk.davidwei.perfmock.internal.perf.Sim;
import uk.davidwei.perfmock.internal.perf.network.node.Sink;

public class TweeterSink extends Sink<TweeterCustomer> {
    private TweeterModel model;

    public TweeterSink(TweeterModel network, Sim sim) {
        super(network, sim, "Sink");
        this.model = network;
    }

    public TweeterSink(TweeterModel network, Sim sim, String nodeName) {
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