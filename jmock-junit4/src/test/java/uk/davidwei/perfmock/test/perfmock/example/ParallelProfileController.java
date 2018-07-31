package uk.davidwei.perfmock.test.perfmock.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class ParallelProfileController {
    private final SocialGraph socialGraph;
    private final UserDetailsService userDetailsService;
    public ParallelProfileController(SocialGraph socialGraph, UserDetailsService userDetailsService) {
        this.socialGraph = socialGraph;
        this.userDetailsService = userDetailsService;
    }

    public void lookUpFriends(long userId) {
        List<Long> friendIds = socialGraph.query(userId);

        ExecutorService es = Executors.newFixedThreadPool(2);

        List<Future<User>> userDetailsRequests = new ArrayList<>();

        for (final Long friend : friendIds) {
            userDetailsRequests.add(es.submit(new Callable<User>() {
                @Override
                public User call() throws Exception {
                    return userDetailsService.lookup(friend);
                }
            }));
        }
        es.shutdown();

        List<User> friends = new ArrayList<>();

        for (Future<User> userDetailsRequest : userDetailsRequests) {
            try {
                friends.add(userDetailsRequest.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void lookUpFriendsThread(long userId) {
        List<Long> friendIds = socialGraph.query(userId);

        CountDownLatch doneSignal = new CountDownLatch(4);
        List<User> friends = Collections.synchronizedList(new ArrayList<>());

        for (final Long friend : friendIds) {
            Thread t = new Thread(() -> {
                friends.add(userDetailsService.lookup(friend));
                doneSignal.countDown();
            });
            t.start();
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}