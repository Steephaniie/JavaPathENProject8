package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.model.User;

/**
 * Classe qui gère le suivi de la localisation des utilisateurs.
 * Cette classe s'exécute en arrière-plan et met à jour périodiquement
 * la position de tous les utilisateurs.
 */
public class Tracker extends Thread {

	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	/**
	 * Intervalle de temps entre chaque mise à jour des positions (5 minutes)
	 */
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	private final TourGuideService tourGuideService;

	private boolean stop = false;

	public Tracker(TourGuideService tourGuideService) {
		this.tourGuideService = tourGuideService;
		executorService.submit(this);
	}

	/**
	 * Assure l'arrêt du thread Tracker
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}

	/**
	 * Méthode principale du thread.
	 * Exécute une boucle continue qui :
	 * 1. Récupère tous les utilisateurs
	 * 2. Met à jour leurs positions
	 * 3. Attend l'intervalle défini avant la prochaine mise à jour
	 */
	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while (true) {
			if (Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}

			List<User> users = tourGuideService.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			tourGuideService.trackUserLocation(users);
			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}
		}

	}
}
