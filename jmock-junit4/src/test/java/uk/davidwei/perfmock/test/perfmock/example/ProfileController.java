package uk.davidwei.perfmock.test.perfmock.example;

import uk.davidwei.perfmock.internal.perf.network.JavaTense;

import java.util.ArrayList;
import java.util.List;

public class ProfileController {
    private final SocialGraph socialGraph;
    private final UserDetailsService userDetailsService;

    public ProfileController(SocialGraph socialGraph, UserDetailsService userDetailsService) {
        this.socialGraph = socialGraph;
        this.userDetailsService = userDetailsService;
    }

    public void lookUpFriends(long userId) {
        List<Long> friendIds = socialGraph.query(userId);
        List<User> friends = new ArrayList<>();

        //JavaTense.scale(400);
        //for (long i = 0; i < 1000000000L; i++) {
        //}
        //JavaTense.reset();

        for (Long friendId : friendIds) {
            friends.add(userDetailsService.lookup(friendId));
        }
    }
}