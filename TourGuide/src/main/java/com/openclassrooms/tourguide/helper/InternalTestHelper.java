package com.openclassrooms.tourguide.helper;

public class InternalTestHelper {

	// Définir cette valeur par défaut jusqu'à 100 000 pour les tests
	private static int internalUserNumber = 100;
	
	public static void setInternalUserNumber(int internalUserNumber) {
		InternalTestHelper.internalUserNumber = internalUserNumber;
	}
	
	public static int getInternalUserNumber() {
		return internalUserNumber;
	}
}
