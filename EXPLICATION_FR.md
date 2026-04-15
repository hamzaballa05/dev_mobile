# Contexte fonctionnel proposé

Comme la capture d'écran mentionnée dans l'énoncé n'était pas fournie, l'application a été modélisée comme une **application mobile de gestion de profil étudiant**.

## Scénario métier
- L'application affiche le profil d'un étudiant : photo, nom, email, téléphone, remarques.
- Les données arrivent depuis un web service REST.
- En mode paysage, la liste des notes est affichée avec un `ListView`.
- L'utilisateur peut appeler le numéro du profil.
- L'utilisateur peut ajouter une matière/note via une seconde activité.
- En arrière-plan, l'application peut enregistrer des positions GPS et les synchroniser dès qu'Internet revient.

## Enchaînement logique des exercices
1. **Fondations Android** : création du projet, cycle de vie, logs.
2. **Interface utilisateur** : `ConstraintLayout`, dégradé, toast, adaptation paysage.
3. **Variante UI** : même écran en `LinearLayout`.
4. **Client/serveur** : récupération du profil via `HttpURLConnection`, puis tâche asynchrone.
5. **Permissions et connectivité** : Internet, état réseau, téléchargement de l'image.
6. **Variante réseau** : refaire la récupération avec Volley.
7. **Données complexes** : récupération des notes, `ArrayList`, `ListView`, layout d'item, `Adapter`.
8. **Intents** : bouton appel, `NoteActivity`, retour de résultat vers l'activité principale.
9. **Composants système** : `BroadcastReceiver`, `Service`, GPS, SQLite, synchronisation lors du retour d'Internet.

Donc oui, ces exercices ont un **enchaînement pédagogique cohérent** : on part du plus simple (activité/UI) vers le plus complet (réseau, intents, services, capteurs et persistance).
