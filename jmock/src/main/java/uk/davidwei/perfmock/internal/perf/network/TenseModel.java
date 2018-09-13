package uk.davidwei.perfmock.internal.perf.network;

import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.internal.perf.Delay;
import uk.davidwei.perfmock.internal.perf.Param;
import uk.davidwei.perfmock.internal.perf.PerformanceModel;
import uk.davidwei.perfmock.internal.perf.Sim;

public class TenseModel implements PerformanceModel {
    private final Sim sim;
    private final Delay delay;

    public TenseModel(Sim sim, Delay delay) {
        this.sim = sim;
        this.delay = delay;
    }

    @Override
    public void schedule(long threadId, Invocation invocation, Param param) {
        long jump = (long)(delay.sample() * 1000000);
        sim.schedule(jump);
    }
}