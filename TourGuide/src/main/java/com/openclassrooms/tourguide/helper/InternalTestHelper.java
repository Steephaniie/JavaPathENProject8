package com.openclassrooms.tourguide.helper;

/**
 * Classe utilitaire pour les tests internes de l'application TourGuide.
 * Permet de configurer le nombre d'utilisateurs internes pour les tests de performance.
 */
public class InternalTestHelper {

	// Nombre d'utilisateurs internes pour les tests. Cette valeur peut être augmentée jusqu'à 100 000 
	// pour les tests de performance, mais est initialisée par défaut à 100
	private static int internalUserNumber = 100;

	/**
	 * Définit le nombre d'utilisateurs internes pour les tests.
	 *
	 * @param internalUserNumber le nombre d'utilisateurs à définir
	 */
	public static void setInternalUserNumber(int internalUserNumber) {
		InternalTestHelper.internalUserNumber = internalUserNumber;
	}

	/**
	 * Récupère le nombre actuel d'utilisateurs internes configuré pour les tests.
	 *
	 * @return le nombre d'utilisateurs internes
	 */
	public static int getInternalUserNumber() {
		return internalUserNumber;
	}
}
