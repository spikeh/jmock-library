package org.jmock.internal.perf.network.node;

import org.jmock.internal.perf.Event;
import org.jmock.internal.perf.Sim;
import org.jmock.internal.perf.network.request.Customer;
import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.adt.OrderedQueue;

/**
 *
 */
public class ProcessorSharingNode<T extends Customer> extends QueueingNode<T> {
    private double vtime = 0.0;
    private double timeServiceBegan = 0.0;
    private Event<T> nextCompletionEvent;

    public ProcessorSharingNode(Network network, Sim sim, String nodeName, Delay delay) {
        super(network, sim, nodeName, delay, 1, new OrderedQueue<T>());
    }

    public ProcessorSharingNode(Network network, Sim sim, String nodeName, Delay delay, int maxRes) {
        super(network, sim, nodeName, delay, maxRes, new OrderedQueue<T>());
    }

    @Override
    public synchronized void accept(T customer) {
        if (queue.canAccept(customer)) {
            double serviceTime = customer.serviceDemand();
            if (resources.isAvailable()) {
                resources.claim();
            } else {
                sim.deschedule(nextCompletionEvent);
                double inc = (sim.now() - timeServiceBegan) / queue.size();
                vtime += inc;
            }
            double t = vtime + serviceTime;
            customer.setaTime(t);
            queue.offer(customer);
            serviceNextCustomer();
        } else {
            loseCustomer(customer);
        }
    }

    private void serviceNextCustomer() {
        T customer = queue.peek();
        double completionTime = (customer.aTime() - vtime) * queue.size();
        timeServiceBegan = sim.now();
        nextCompletionEvent = new Completion(sim.now() + completionTime, customer);
        sim.schedule(nextCompletionEvent);
    }

    @Override
    public void releaseResource() {
    }

    private class Completion extends Event<T> {
        public Completion(double time, T customer) {
            super(time, customer);
        }

        public boolean invoke() {
            vtime += (sim.now() - timeServiceBegan) / queue.size();
            T customer = queue.poll();
            if (queue.size() == 0) {
                resources.release();
                vtime = 0.0;
            } else {
                serviceNextCustomer();
            }
            forward(customer);
            return false;
        }
    }
}