package org.jmock.test.perfmock.test;

import org.jmock.Expectations;
import org.jmock.integration.junit4.PerformanceMockery;
import org.jmock.internal.perf.Delay;
import org.jmock.internal.perf.distribution.Alias;
import org.jmock.internal.perf.distribution.Distribution;
import org.jmock.test.perfmock.example.ProfileController;
import org.jmock.test.perfmock.example.SocialGraph;
import org.jmock.test.perfmock.example.User;
import org.jmock.test.perfmock.example.UserDetailsService;
import org.junit.Rule;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.number.OrderingComparison.comparesEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.jmock.integration.junit4.ServiceTimes.alias;
import static org.jmock.integration.junit4.ServiceTimes.constant;
import static org.junit.Assert.assertThat;

public class EmpiricalTest {
    static final long USER_ID = 1111L;
    static final List<Long> FRIEND_IDS = Arrays.asList(2222L, 3333L, 4444L, 5555L);

    @Rule
    public PerformanceMockery context = new PerformanceMockery();

    @Test
    public void looksUpDetailsForEachFriend() throws IOException {
        InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream(String.format("/tweeter/%s/threads%d.txt", "messages", 5)));
        try (BufferedReader br = new BufferedReader(isr)) {
            Distribution alias = new Alias(br);
            final SocialGraph socialGraph = context.mock(SocialGraph.class, alias);
            final UserDetailsService userDetails = context.mock(UserDetailsService.class, constant(100));

            context.checking(new Expectations() {{
                exactly(1).of(socialGraph).query(USER_ID);
                will(returnValue(FRIEND_IDS));
                exactly(4).of(userDetails).lookup(with(any(Long.class)));
                will(returnValue(new User()));
            }});

            new ProfileController(socialGraph, userDetails).lookUpFriends(USER_ID);

            assertThat(context.runtime(), lessThan(1500.0));
        }

    }
}