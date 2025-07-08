package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.dto.AttractionUserDTO;
import com.openclassrooms.tourguide.dto.NearbyAttractionsDTO;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;
import com.openclassrooms.tourguide.tracker.Tracker;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service principal de l'application TourGuide qui gère :
 * - Le suivi des utilisateurs et leurs localisations
 * - Les attractions à proximité
 * - Les récompenses pour les attractions visitées
 * - Les offres de voyage personnalisées
 */
@Service
public class TourGuideService {
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;

    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;

        Locale.setDefault(Locale.US);

        if (testMode) {
            logger.info("TestMode enabled");
            logger.info("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    /**
     * Récupère la liste des récompenses d'un utilisateur.
     *
     * @param user L'utilisateur dont on veut récupérer les récompenses
     * @return La liste des récompenses de l'utilisateur
     */
    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    /**
     * Obtient la dernière localisation d'un utilisateur ou le localise s'il n'a pas d'historique.
     *
     * @param user L'utilisateur dont on veut connaître la position
     * @return La dernière localisation visitée par l'utilisateur
     */
    public VisitedLocation getUserLocation(User user) {
        if (user.getVisitedLocations().isEmpty())
            return trackUserLocation(user);
        else
            return user.getLastVisitedLocation();
    }

    /**
     * Récupère un utilisateur par son nom d'utilisateur.
     *
     * @param userName Le nom d'utilisateur recherché
     * @return L'utilisateur correspondant au nom d'utilisateur
     */
    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    /**
     * Récupère la liste de tous les utilisateurs.
     *
     * @return La liste de tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    /**
     * Ajoute un nouvel utilisateur s'il n'existe pas déjà.
     *
     * @param user L'utilisateur à ajouter
     */
    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    /**
     * Obtient les offres de voyage pour un utilisateur en fonction de ses points de récompense.
     *
     * @param user L'utilisateur pour lequel on recherche des offres
     * @return La liste des offres de voyage disponibles
     */
    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }


    /**
     * Suit la localisation d'un utilisateur et calcule ses récompenses.
     *
     * @param user L'utilisateur à localiser
     * @return La localisation actuelle de l'utilisateur
     */
    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    /**
     * Trouve et renvoie les 5 attractions les plus proches d'une localisation donnée.
     * Pour chaque attraction, calcule la distance et les points de récompense associés.
     *
     * @param visitedLocation La localisation à partir de laquelle chercher les attractions
     * @param user L'utilisateur pour lequel calculer les points de récompense
     * @return Un objet DTO contenant la position de l'utilisateur et les 5 attractions les plus proches avec leurs détails
     */
    public NearbyAttractionsDTO getNearByAttractions(VisitedLocation visitedLocation, User user) {

        Location loc1 = new Location(visitedLocation.location.latitude, visitedLocation.location.longitude);
        List<AttractionDistance> attractionDistances = new ArrayList<>();


        for (Attraction attraction : gpsUtil.getAttractions()) {
            AttractionDistance attractionDistance = new AttractionDistance(attraction.attractionName,
                    attraction.city, attraction.state, attraction.latitude, attraction.longitude);
            Location loc2 = new Location(attraction.latitude, attraction.longitude);
            attractionDistance.distance = rewardsService.getDistance(loc1, loc2);
            attractionDistances.add(attractionDistance);
        }

        // Trier par distance et limiter aux 5 plus proches
        List<AttractionDistance> nearestFiveAttractions = attractionDistances.stream()
                .sorted(Comparator.comparingDouble(AttractionDistance::getDistance))
                .limit(5)
                .toList();


        NearbyAttractionsDTO nearbyAttractionsDTO = new NearbyAttractionsDTO();
        nearbyAttractionsDTO.setLongitudeUser(visitedLocation.location.longitude);
        nearbyAttractionsDTO.setLatitudeUser(visitedLocation.location.latitude);
        List<AttractionUserDTO> attractionUserDTOs = new ArrayList<>();
        for (AttractionDistance attraction : nearestFiveAttractions) {
            AttractionUserDTO attractionUserDTO = new AttractionUserDTO();
            attractionUserDTO.setAttractionName(attraction.attractionName);
            attractionUserDTO.setLatitude(attraction.latitude);
            attractionUserDTO.setLongitude(attraction.longitude);
            attractionUserDTO.setDistance(attraction.distance);
            attractionUserDTO.setRewardPoints(rewardsService.getRewardPoints(attraction,user));
            attractionUserDTOs.add(attractionUserDTO);
        }

        nearbyAttractionsDTO.setAttractionUsersDTO(attractionUserDTOs);

        return nearbyAttractionsDTO;
    }

    @Getter
    public static class AttractionDistance extends Attraction {
        private double distance;

        public AttractionDistance(String attractionName, String city, String state, double latitude, double longitude) {
            super(attractionName, city, state, latitude, longitude);
        }
    }


    /**
     * Ajoute un hook d'arrêt pour stopper le suivi lors de la fermeture de l'application.
     */
    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes
    // internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    /**
     * Initialise les utilisateurs internes pour les tests.
     */
    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    /**
     * Génère un historique de localisation aléatoire pour un utilisateur.
     *
     * @param user L'utilisateur pour lequel générer l'historique
     */
    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
                    new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    /**
     * Génère une longitude aléatoire entre -180 et 180 degrés.
     *
     * @return Une longitude aléatoire
     */
    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    /**
     * Génère une latitude aléatoire entre -85.05112878 et 85.05112878 degrés.
     *
     * @return Une latitude aléatoire
     */
    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    /**
     * Génère une date aléatoire dans les 30 derniers jours.
     *
     * @return Une date aléatoire
     */
    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
