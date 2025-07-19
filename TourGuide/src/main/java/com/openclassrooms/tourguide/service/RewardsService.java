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

/**
 * Service gérant le système de récompenses pour les utilisateurs.
 * Ce service calcule les récompenses basées sur la proximité des attractions visitées.
 */
@Service
public class RewardsService {
    /**
     * Facteur de conversion des miles nautiques en miles statutaires
     */
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    private Logger logger = LoggerFactory.getLogger(RewardsService.class);
    /**
     * Distance de proximité par défaut en miles
     */
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    /**
     * Portée de proximité pour les attractions en miles
     */
    private int attractionProximityRange = 200;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    /**
     * Définit la distance de proximité personnalisée.
     *
     * @param proximityBuffer Distance en miles
     */
    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    /**
     * Réinitialise la distance de proximité à sa valeur par défaut.
     */
    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Calcule les récompenses pour une liste d'utilisateurs de manière asynchrone.
     *
     * @param users Liste des utilisateurs pour lesquels calculer les récompenses
     */
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

    /**
     * Calcule les récompenses pour un utilisateur spécifique.
     *
     * @param user Utilisateur pour lequel calculer les récompenses
     */
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

    /**
     * Vérifie si une localisation est dans la zone de proximité d'une attraction.
     *
     * @param attraction L'attraction à vérifier
     * @param location   La localisation à comparer
     * @return true si la localisation est dans la zone de proximité
     */
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }

    /**
     * Récupère le nombre de points de récompense pour une attraction et un utilisateur.
     *
     * @param attraction L'attraction visitée
     * @param user       L'utilisateur concerné
     * @return Le nombre de points de récompense
     */
    public int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    /**
     * Calcule la distance en miles statutaires entre deux points géographiques
     * spécifiés par leur latitude et longitude.
     *
     * @param loc1 première localisation avec latitude et longitude
     * @param loc2 deuxième localisation avec latitude et longitude
     * @return la distance entre les deux points en miles statutaires
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
