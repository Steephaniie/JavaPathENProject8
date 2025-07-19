/**
 * Package contenant les tests de l'application TourGuide
 */
package com.openclassrooms.tourguide;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Classe de test pour vérifier le bon démarrage de l'application TourGuide
 * Cette classe s'assure que le contexte Spring se charge correctement
 */
@SpringBootTest
class TourguideApplicationTests {

	/**
	 * Test vérifiant que le contexte de l'application se charge correctement
	 * Ce test échoue si le contexte Spring ne peut pas démarrer
	 */
	@Test
	void contextLoads() {
		// Test du chargement du contexte Spring
	}

}
