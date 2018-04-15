package org.jmock.models.tweeter;

import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.Event;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.distribution.Alias;
import org.jmock.internal.perf.distribution.Distribution;
import org.jmock.internal.perf.network.node.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class TweeterTableNode extends Node<TweeterCustomer> {
    private Map<Integer, Delay> delayMap = new HashMap<>();
    private TweeterModel tweeterModel;
    private String tableName;

    public TweeterTableNode(TweeterModel network, Sim sim, String tableName) {
        super(network, sim, tableName);
        this.tweeterModel = network;
        this.tableName = tableName;
        /*
        for (int i = 1; i <= 100; ++i) {
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
        */
    }

    @Override
    public synchronized void enter(TweeterCustomer customer) {
        /*
        int clients = customer.getClients();
        Delay delay = delayMap.get(clients);
        if (delay != null) {
            customer.setServiceDemand(delay.sample());
        }
        */
        super.enter(customer);
    }

    @Override
    public void accept(TweeterCustomer customer) {
        scheduleEvent(customer);
    }

    protected void scheduleEvent(TweeterCustomer customer) {
        sim.schedule(new EndServiceEvent(customer, sim.now()));
    }

    private class EndServiceEvent extends Event<TweeterCustomer> {
        private EndServiceEvent(TweeterCustomer customer, double time) {
            super(time, customer);
        }

        public boolean invoke() {
            int clients = tweeterModel.getRequests(eventObject.arrivalTime(), invokeTime);
            System.out.println("<!> clients = " + clients + " when invoking TweeterTableNode event");
            Delay delay = delayMap.get(clients);
            if (delay == null) {
                try {
                    InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream(String.format("/tweeter/%s/threads%d.txt", tableName, clients)));
                    try (BufferedReader br = new BufferedReader(isr)) {
                        Distribution alias = new Alias(br);
                        delay = new Delay(alias);
                        delayMap.put(clients, delay);
                    }
                    eventObject.setServiceDemand(delay.sample());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                eventObject.setServiceDemand(delay.sample());
            }
            forward(eventObject);
            return false;
        }
    }
}