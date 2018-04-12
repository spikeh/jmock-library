package org.jmock.internal.perf.network.link;

class DiscreteSampler {
    private double[] probs;

    DiscreteSampler(double[] probs) {
        this.probs = probs;
    }

    int next() {
        double acc = probs[0];
        int index = 0;
        double r = Math.random();
        while (acc < r) {
            index++;
            acc += probs[index];
        }
        return index;
    }
}