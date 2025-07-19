package com.openclassrooms.tourguide.controller;

import java.util.List;

import com.openclassrooms.tourguide.dto.NearbyAttractionsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;

import tripPricer.Provider;

/**
 * Contrôleur REST pour l'application TourGuide
 * Expose les endpoints permettant d'accéder aux fonctionnalités :
 * - Localisation des utilisateurs
 * - Attractions touristiques à proximité
 * - Gestion des récompenses
 * - Offres de voyage personnalisées
 */
@RestController
public class TourGuideController {

    /**
     * Service contenant la logique métier de l'application
     */
    @Autowired
	TourGuideService tourGuideService;

    /**
     * Page d'accueil de l'application
     *
     * @return Message de bienvenue
     */
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * Retourne la dernière position connue d'un utilisateur
     *
     * @param userName Nom de l'utilisateur à localiser
     * @return La dernière localisation enregistrée pour cet utilisateur
     */
    @RequestMapping("/getLocation") 
    public VisitedLocation getLocation(@RequestParam String userName) {
    	return tourGuideService.getUserLocation(getUser(userName));
    }

    /**
     * Retourne les 5 attractions touristiques les plus proches d'un utilisateur
     *
     * @param userName Nom de l'utilisateur
     * @return DTO contenant la liste des attractions avec leurs distances et points de récompense
     */
    @RequestMapping("/getNearbyAttractions") 
    public NearbyAttractionsDTO getNearbyAttractions(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    	return tourGuideService.getNearByAttractions(visitedLocation,getUser(userName));
    }

    /**
     * Récupère toutes les récompenses d'un utilisateur
     *
     * @param userName Nom de l'utilisateur
     * @return Liste des récompenses obtenues pour les attractions visitées
     */
    @RequestMapping("/getRewards") 
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(getUser(userName));
    }

    /**
     * Récupère les offres de voyage disponibles pour un utilisateur
     *
     * @param userName Nom de l'utilisateur
     * @return Liste des offres proposées par les différents fournisseurs
     */
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }

    /**
     * Méthode utilitaire pour récupérer un utilisateur par son nom
     *
     * @param userName Nom de l'utilisateur recherché
     * @return L'utilisateur correspondant au nom fourni
     */
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   

}