package org.jmock.test.perfmock.test;

import org.jmock.Expectations;
import org.jmock.integration.junit4.PerformanceMockery;
import org.jmock.internal.perf.PerformanceModel;
import org.jmock.internal.perf.network.CassandraNetwork;
import org.jmock.internal.perf.network.request.CassandraConsistencyLevel;
import org.jmock.test.perfmock.example.ProfileController;
import org.jmock.test.perfmock.example.SocialGraph;
import org.jmock.test.perfmock.example.User;
import org.jmock.test.perfmock.example.UserDetailsService;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.jmock.integration.junit4.ServiceTimes.constant;
import static org.junit.Assert.assertThat;

public class QNTest {
    static final long USER_ID = 1111L;
    static final List<Long> FRIEND_IDS = Arrays.asList(2222L, 3333L, 4444L, 5555L);

    @Rule
    public PerformanceMockery context = new PerformanceMockery();

    @Test
    public void looksUpDetailsForEachFriend() {
        PerformanceModel model = new CassandraNetwork(PerformanceMockery.INSTANCE.sim(), CassandraConsistencyLevel.ONE,
                2);
        final SocialGraph socialGraph = context.mock(SocialGraph.class, model);
        final UserDetailsService userDetails = context.mock(UserDetailsService.class, constant(100));

        context.checking(new Expectations() {{
            exactly(1).of(socialGraph).query(USER_ID);
            will(returnValue(FRIEND_IDS));
            pass(CassandraNetwork.param(4));
            exactly(4).of(userDetails).lookup(with(any(Long.class)));
            will(returnValue(new User()));
        }});

        new ProfileController(socialGraph, userDetails).lookUpFriends(USER_ID);

        assertThat(context.runtime(), lessThan(600.0));
    }
}