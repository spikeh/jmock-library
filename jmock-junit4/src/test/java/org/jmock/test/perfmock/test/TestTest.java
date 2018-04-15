package org.jmock.test.perfmock.test;

import org.jmock.Expectations;

import org.jmock.integration.junit4.PerformanceMockery;
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
import static org.jmock.internal.perf.stats.PerfStatistics.hasMean;
import static org.junit.Assert.assertThat;

public class TestTest {
    static final long USER_ID = 1111L;
    static final List<Long> FRIEND_IDS = Arrays.asList(2222L, 3333L, 4444L, 5555L);

    @Rule
    public PerformanceMockery context = new PerformanceMockery();

    @Test
    public void looksUpDetailsForEachFriend() {

        final SocialGraph socialGraph = context.mock(SocialGraph.class, constant(200));
        final UserDetailsService userDetails = context.mock(UserDetailsService.class, constant(100));

        context.repeat(10, new Runnable() {
            @Override
            public void run() {
                context.checking(new Expectations() {{
                    exactly(1).of(socialGraph).query(USER_ID);
                    will(returnValue(FRIEND_IDS));
                    exactly(4).of(userDetails).lookup(with(any(Long.class)));
                    will(returnValue(new User()));
                }});

                new ProfileController(socialGraph, userDetails).lookUpFriends(USER_ID);
            }
        });

        assertThat(context.runtimes(), hasMean(lessThan(605.0)));
    }
}