package uk.davidwei.perfmock.test.perfmock.test;

import org.junit.Rule;
import org.junit.Test;
import uk.davidwei.perfmock.Expectations;
import uk.davidwei.perfmock.integration.junit4.PerformanceMockery;
import uk.davidwei.perfmock.internal.perf.stats.PerfStatistics;
import uk.davidwei.perfmock.test.perfmock.example.ProfileController;
import uk.davidwei.perfmock.test.perfmock.example.SocialGraph;
import uk.davidwei.perfmock.test.perfmock.example.User;
import uk.davidwei.perfmock.test.perfmock.example.UserDetailsService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;
import static uk.davidwei.perfmock.integration.junit4.ServiceTimes.constant;

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

        assertThat(context.runtimes(), PerfStatistics.hasMean(lessThan(605.0)));
    }
}