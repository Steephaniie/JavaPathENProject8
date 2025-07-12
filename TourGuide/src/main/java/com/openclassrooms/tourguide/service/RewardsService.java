package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    private Logger logger = LoggerFactory.getLogger(RewardsService.class);
    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    public void calculateRewards(List<User> users) {
        List<Attraction> attractions = gpsUtil.getAttractions();
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(5000);
        try {
            for (User user : users) {
                CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> {
                    if (user.isCalculateRewardEnCours()) return true;
                    user.startCalculateReward();
                    try {
                        calculateRewards(user, attractions);
                    } finally {
                        user.stopCalculateReward();
                    }
                    return true;
                }, executor);
                futures.add(completableFuture);
            }
            // Attente de la completion de tous les futures du lot
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            logger.info("calculateRewards - Lot de {} utilisateurs traité avec succès", users.size());
        } finally {
            // Arrêter proprement l'executor
            executor.shutdown();
        }
    }

    public void calculateRewards(User user) {
        List<Attraction> attractions = gpsUtil.getAttractions();
        calculateRewards(user, attractions);
    }

    public void calculateRewards(User user, List<Attraction> attractions) {
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        // utilisation d'un compteur a taille fixe
        for (int i = 0; i < userLocations.size(); i++) {
            //  Pour chaque attraction
            for (Attraction attraction : attractions) {
                // on vérifie que le user n'a pas déja eu la récompense
                if (user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
                    // s'il est assez proche, on lui ajoute la récompense
                    if (nearAttraction(userLocations.get(i), attraction)) {
                        user.addUserReward(new UserReward(userLocations.get(i), attraction, getRewardPoints(attraction, user)));
                    }
                }
            }
        }
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }

    public int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    /**
     * Calculates the distance in statute miles between two geographical locations
     * specified by their latitude and longitude.
     *
     * @param loc1 the first location containing latitude and longitude
     * @param loc2 the second location containing latitude and longitude
     * @return the distance between the two locations in statute miles
     */

    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }

}
