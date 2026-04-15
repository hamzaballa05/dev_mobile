# Projet Android Studio Java - Profil Étudiant

Ce dossier contient un squelette **Java** pour Android Studio basé sur les exercices fournis.

## Deux façons de le tester

### Option A — La plus simple
1. Ouvrez Android Studio.
2. Créez un projet **Empty Views Activity** en **Java**.
3. Donnez le package `com.example.studentprofile`.
4. Fermez Android Studio.
5. Remplacez le dossier `app/src/main` de votre projet par celui contenu ici.
6. Remplacez aussi le fichier `app/build.gradle` par celui fourni ici.
7. Rouvrez le projet et laissez Android Studio synchroniser Gradle.

### Option B — À partir de ce dossier
1. Ouvrez ce dossier dans Android Studio comme projet existant.
2. Si Android Studio demande de mettre à jour la version Gradle/AGP, acceptez l'ajustement automatique.

## Notes importantes
- L'URL de profil fournie dans l'énoncé contient `id=xxxx`. Le projet tente donc l'appel réseau, puis bascule sur un **JSON local de démonstration** si l'ID distant n'est pas valide.
- Pour tester un vrai profil distant, changez `DEFAULT_PROFILE_ID` dans `Constants.java`.
- Le projet charge les données par **HttpURLConnection** par défaut.
- Pour tester la version **Volley**, passez `USE_VOLLEY_BY_DEFAULT` à `true` dans `Constants.java`.
- Le bouton **Appel** utilise `ACTION_DIAL` (plus simple à tester que `ACTION_CALL`).
- Le suivi GPS démarre aussi depuis l'application après accord des permissions, afin d'éviter de redémarrer l'appareil à chaque test.
- Le service d'upload des positions envoie les données vers `https://httpbin.org/post` à titre de démonstration. Remplacez cette URL par votre vrai web service si nécessaire.

## Structure pédagogique couverte
- Cycle de vie de l'activité + logs Logcat
- UI ConstraintLayout + version paysage
- 2e écran équivalent en LinearLayout
- Consommation d'un web service en HttpURLConnection
- Variante Volley
- Téléchargement d'image
- ListView + Adapter personnalisé pour les notes
- Appel téléphonique via Intent
- 2e activité NoteActivity + retour de résultat
- BroadcastReceiver + Service de tracking GPS + SQLite + synchro réseau
