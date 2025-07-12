package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import rewardCentral.RewardCentral;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPerformance {

    /**
     * Note sur les améliorations de performance :
     * <p>
     * Le nombre d'utilisateurs générés pour les tests à haut volume peut être
     * facilement ajusté via cette méthode :
     * <p>
     * InternalTestHelper.setInternalUserNumber(100000);
     * <p>
     * <p>
     * Ces tests peuvent être modifiés pour s'adapter aux nouvelles solutions,
     * tant que les métriques de performance à la fin des tests restent cohérentes.
     * <p>
     * Voici les métriques de performance que nous essayons d'atteindre :
     * <p>
     * highVolumeTrackLocation : 100 000 utilisateurs en moins de 15 minutes :
     * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     * <p>
     * highVolumeGetRewards : 100 000 utilisateurs en moins de 20 minutes :
     * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     **/

    @Disabled
    @Test
    public void highVolumeTrackLocation() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        // Le nombre d'utilisateurs doit être augmenté jusqu'à 100 000, et le test
        // doit se terminer en moins de 15 minutes
        InternalTestHelper.setInternalUserNumber(100000);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        tourGuideService.trackUserLocation(allUsers);
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    @Disabled
    @Test
    public void highVolumeGetRewards() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

        // Le nombre d'utilisateurs doit être augmenté jusqu'à 100 000, et le test
        // doit se terminer en moins de 20 minutes
        InternalTestHelper.setInternalUserNumber(100000);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        Attraction attraction = gpsUtil.getAttractions().get(0);
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        rewardsService.calculateRewards(allUsers);

        for (User user : allUsers) {
            assertTrue(user.getUserRewards().size() > 0);
        }
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
                + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

}
