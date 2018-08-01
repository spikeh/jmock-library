package uk.davidwei.perfmock.test.perfmock.test;

import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.junit.Rule;
import org.junit.Test;
import uk.davidwei.perfmock.Expectations;
import uk.davidwei.perfmock.integration.junit4.PerformanceMockery;
import uk.davidwei.perfmock.internal.perf.stats.PerfStatistics;
import uk.davidwei.perfmock.test.perfmock.example.ProfileController;
import uk.davidwei.perfmock.test.perfmock.example.SocialGraph;
import uk.davidwei.perfmock.test.perfmock.example.User;
import uk.davidwei.perfmock.test.perfmock.example.UserDetailsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;
import static uk.davidwei.perfmock.integration.junit4.ServiceTimes.exponentialDist;
import static uk.davidwei.perfmock.integration.junit4.ServiceTimes.paretoDist;

public class RepeatTest {
    static final long USER_ID = 1111L;
    static final List<Long> FRIEND_IDS = Arrays.asList(2222L, 3333L, 4444L, 5555L);

    @Rule
    public PerformanceMockery context = new PerformanceMockery();

    @Test
    public void looksUpDetailsForEachFriend() {
        final SocialGraph socialGraph = context.mock(SocialGraph.class, exponentialDist(0.05));
        //context.mock(SocialGraph.class, Contract<SocialGraph> contract);
        final UserDetailsService userDetails = context.mock(UserDetailsService.class, exponentialDist(0.03));

        context.repeat(1000, new Runnable() {
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

        assertThat(context.runtimes(), PerfStatistics.hasPercentile(80, lessThan(800.0)));
        List<Double> test = new ArrayList<>();
        RealDistribution dist = new ParetoDistribution(29, 2);
        for (int i = 0; i < 5000; i++) {
            test.add(dist.sample());
        }
        //assertThat(test, PerfStatistics.matchDistribution(paretoDist(30, 2)));
    }
}