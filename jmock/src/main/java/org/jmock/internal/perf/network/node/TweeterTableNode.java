package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.Event;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.distribution.Alias;
import org.jmock.internal.perf.distribution.Distribution;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.request.TweeterCustomer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TweeterTableNode extends Node<TweeterCustomer> {
    private Map<Integer, Delay> delayMap = new HashMap<>();

    public TweeterTableNode(Network<TweeterCustomer> network, Sim sim, String tableName) throws IOException {
        super(network, sim, tableName);
        for (int i = 1; i <= 100; ++i)
            try {
                InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream(String.format("/tweeter/%s/threads%d.txt", tableName, i)));
                try (BufferedReader br = new BufferedReader(isr)) {
                    Distribution alias = new Alias(br);
                    Delay d = new Delay(alias);
                    delayMap.put(i, d);
                }
            } catch (IOException e) {
                System.out.println("IOException");
            }
    }

    @Override
    public synchronized void enter(TweeterCustomer customer) {
        int clients = customer.getClients();
        Delay delay = delayMap.get(clients);
        if (delay != null) {
            customer.setServiceDemand(delay.sample());
        }
        super.enter(customer);
    }

    @Override
    public void accept(TweeterCustomer customer) {
        scheduleEvent(customer);
    }

    protected void scheduleEvent(TweeterCustomer customer) {
        sim.schedule(new EndServiceEvent(customer, sim.now() + customer.serviceDemand()));
    }

    private class EndServiceEvent extends Event<TweeterCustomer> {
        private EndServiceEvent(TweeterCustomer customer, double time) {
            super(time, customer);
        }

        public boolean invoke() {
            forward(eventObject);
            return false;
        }
    }
}