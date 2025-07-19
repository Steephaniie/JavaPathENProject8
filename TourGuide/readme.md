# TourGuide
![img.png](img.png)
##  Description
TourGuide est une application Java Spring Boot permettant aux utilisateurs de planifier leurs voyages en accédant à des attractions touristiques proches et en recevant des offres partenaires. Le système repose sur deux services simulés : `gpsUtil` (géolocalisation) et `RewardCentral` (gestion des récompenses).

Cette version intègre des optimisations de performance, des corrections de bugs, une gestion de la montée en charge, ainsi qu'un pipeline d'intégration continue.

---

##  Fonctionnalités principales
- Récupération de la position des utilisateurs
- Recommandation des 5 attractions les plus proches (peu importe la distance)
- Attribution de récompenses via RewardCentral
- Exposition d’une API REST
- Simulation de charge utilisateur jusqu’à 100 000

# Technologies
> Java 17  
> Spring Boot 3.X  
> JUnit 5  
> Maven
> CompletableFuture (programmation asynchrone)
> GitHub Actions (CI/CD)
> Postman (tests API REST)

##  Installation & exécution

# How to have gpsUtil, rewardCentral and tripPricer dependencies available ?
> Run :
- mvn install:install-file -Dfile=/libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar
- mvn install:install-file -Dfile=/libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar
- mvn install:install-file -Dfile=/libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar

# Cloner le dépôt
$ git clone https://github.com/Steephaniie/JavaPathENProject8.git
$ cd JavaPathENProject8

# Compiler le projet
$ mvn clean install

# Lancer l'application
$ mvn spring-boot:run

L'application est accessible par défaut sur `http://localhost:8080`

## Endpoints REST
| Méthode | Endpoint                | Description |
|---------|-------------------------|-------------|
| GET     | /getNearbyAttractions   | Renvoie les 5 attractions les plus proches |
| GET     | /getRewards             | Renvoie les récompenses d’un utilisateur |
| GET     | /getTripDeals           | Donne des suggestions de voyage et offres |
| GET     | /getAllCurrentLocations | Coordonnées de tous les utilisateurs |


## Tests & Performance
- Classe `TestPerformance` pour évaluer les performances jusqu’à 100 000 utilisateurs
- Temps d'exécution :
    - gpsUtil : ≤ 15 min (100k utilisateurs)
    - RewardCentral : ≤ 20 min (100k utilisateurs)


## Intégration continue
Le pipeline GitHub Actions est défini dans `.github/workflows/ci.yml` :
- Compilation Maven
- Exécution des tests
- Génération du jar exécutable


## Documentation
La documentation fonctionnelle et technique est fournie dans le dossier `/docs`, incluant :
- Cahier des charges
- Schémas d'architecture
- Mesures de performance


## Bonnes pratiques
- Code commenté et structuré
- Gestion des erreurs et logs centralisés
- Tests automatisés et vérifiés


##  Auteure
Stéphanie — Développeuse Java Back-End


## Sécurité & API
- Aucun stockage de données sensibles
- Attention au niveau de logs en production
- Vérification manuelle des accès utilisateur possible à intégrer

