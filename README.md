# dev_mobile : application Android « Student Profile »

Application Android (Java) de **profil étudiant** : affichage d'un profil, gestion de notes, et suivi de position avec envoi des positions à un serveur.

## Fonctionnalités

- Profil étudiant chargé depuis un service REST, avec un fichier JSON local d'exemple
- Notes de l'étudiant (liste, ajout)
- Suivi de la position (service Android), stockage local SQLite et envoi différé des positions quand le réseau revient (`WorkManager`), vers un endpoint de test (httpbin)
- Interface adaptée aux modes portrait, paysage et thème sombre

## Technologies

Java, Android SDK, Gradle, SQLite, WorkManager

## Lancer le projet

1. Ouvrir le dossier dans Android Studio.
2. Laisser Gradle synchroniser le projet.
3. Lancer l'application sur un émulateur ou un appareil.

## Auteur

**Hamza Balla** : étudiant ingénieur à l'INSEA (Data and Software Engineering).
