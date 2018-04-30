package org.jmock.integration.junit4;

import org.jmock.internal.perf.distribution.Distribution;
import org.jmock.internal.perf.network.Network;
import org.jmock.internal.perf.network.SinglePSNetwork;
import org.jmock.internal.perf.network.SingleServiceNetwork;
import org.jmock.internal.perf.network.adt.CappedQueue;

public class PerformanceModels {
    public static Network singleServer(CappedQueue queueingDiscipline, Distribution serviceTime) {
        return new SingleServiceNetwork(PerformanceMockery.INSTANCE.sim(), serviceTime, queueingDiscipline);
    }
    
    public static Network processorSharing(Distribution serviceTime) {
        return new SinglePSNetwork(PerformanceMockery.INSTANCE.sim(), serviceTime);
    }
}