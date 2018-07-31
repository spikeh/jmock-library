package uk.davidwei.perfmock.integration.junit4;

import uk.davidwei.perfmock.internal.perf.distribution.Distribution;
import uk.davidwei.perfmock.internal.perf.network.Network;
import uk.davidwei.perfmock.internal.perf.network.SinglePSNetwork;
import uk.davidwei.perfmock.internal.perf.network.SingleServiceNetwork;
import uk.davidwei.perfmock.internal.perf.network.adt.CappedQueue;

public class PerformanceModels {
    public static Network singleServer(CappedQueue queueingDiscipline, Distribution serviceTime) {
        return new SingleServiceNetwork(PerformanceMockery.INSTANCE.sim(), serviceTime, queueingDiscipline);
    }
    
    public static Network processorSharing(Distribution serviceTime) {
        return new SinglePSNetwork(PerformanceMockery.INSTANCE.sim(), serviceTime);
    }
}